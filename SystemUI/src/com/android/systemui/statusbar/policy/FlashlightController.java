/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Size;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import com.mac.gesture.GestureJNI;

/**
 * Manages the flashlight.
 */
public class FlashlightController {

    private static final String TAG = "FlashlightController";
    private static final boolean DEBUG = false;//Log.isLoggable(TAG, Log.DEBUG);

    private static final int DISPATCH_ERROR = 0;
    private static final int DISPATCH_OFF = 1;
    private static final int DISPATCH_AVAILABILITY_CHANGED = 2;
    
    private static final String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
    
    /** Call {@link #ensureHandler()} before using */
    private Handler mHandler;

    /** Lock on mListeners when accessing */
    private final ArrayList<WeakReference<FlashlightListener>> mListeners = new ArrayList<>(1);

    /** Lock on {@code this} when accessing */
    private boolean mFlashlightEnabled;

    public FlashlightController(Context mContext) {
        Log.i(TAG, "[FlashlightController]");
        initialize();
        // M: Turn off the flashlight when shutdown.
        initializeReceiver(mContext);
    }

    public void initialize() {
        Log.i(TAG, "[initialize]");
            ensureHandler();
    }

    public synchronized void setFlashlight(boolean enabled) {
        Log.i(TAG, "[setFlashlight]enabled=" + enabled);
        if (mFlashlightEnabled != enabled) {
            mFlashlightEnabled = enabled;
            postUpdateFlashlight();
        }
    }

    public void killFlashlight() {
        Log.i(TAG, "[killFlashlight]mFlashlightEnabled=" + mFlashlightEnabled);
        boolean enabled;
        synchronized (this) {
            enabled = mFlashlightEnabled;
        }
        if (enabled) {
            mHandler.post(mKillFlashlightRunnable);
        }
    }

    public synchronized boolean isAvailable() {
        return true;
    }

    public void addListener(FlashlightListener l) {
        synchronized (mListeners) {
            cleanUpListenersLocked(l);
            mListeners.add(new WeakReference<>(l));
        }
    }

    public void removeListener(FlashlightListener l) {
        synchronized (mListeners) {
            cleanUpListenersLocked(l);
        }
    }

    private synchronized void ensureHandler() {
        if (mHandler == null) {
            HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            mHandler = new Handler(thread.getLooper());
        }
    }

    private void postUpdateFlashlight() {
        ensureHandler();
        mHandler.post(mUpdateFlashlightRunnable);
    }

    private void updateFlashlight(boolean forceDisable) {
        try {
            boolean enabled;
            synchronized (this) {
                enabled = mFlashlightEnabled && !forceDisable;
            }
            if (enabled) {
				GestureJNI.setPhoneFlashLightPinValue(1);
            } else {
				GestureJNI.setPhoneFlashLightPinValue(0);
            }

        } catch (IllegalStateException|UnsupportedOperationException e) {
            Log.e(TAG, "Error in updateFlashlight", e);
            handleError();
        }
    }

    private void handleError() {
        synchronized (this) {
            mFlashlightEnabled = false;
        }
        updateFlashlight(true /* forceDisable */);
    }

    private void dispatchOff() {
        dispatchListeners(DISPATCH_OFF, false /* argument (ignored) */);
    }

    private void dispatchError() {
        dispatchListeners(DISPATCH_ERROR, false /* argument (ignored) */);
    }

    private void dispatchAvailabilityChanged(boolean available) {
        dispatchListeners(DISPATCH_AVAILABILITY_CHANGED, available);
    }

    private void dispatchListeners(int message, boolean argument) {
        synchronized (mListeners) {
            final int N = mListeners.size();
            boolean cleanup = false;
            for (int i = 0; i < N; i++) {
                FlashlightListener l = mListeners.get(i).get();
                if (l != null) {
                    if (message == DISPATCH_ERROR) {
                        l.onFlashlightError();
                    } else if (message == DISPATCH_OFF) {
                        l.onFlashlightOff();
                    } else if (message == DISPATCH_AVAILABILITY_CHANGED) {
                        l.onFlashlightAvailabilityChanged(argument);
                    }
                } else {
                    cleanup = true;
                }
            }
            if (cleanup) {
                cleanUpListenersLocked(null);
            }
        }
    }

    private void cleanUpListenersLocked(FlashlightListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            FlashlightListener found = mListeners.get(i).get();
            if (found == null || found == listener) {
                mListeners.remove(i);
            }
        }
    }

    private final Runnable mUpdateFlashlightRunnable = new Runnable() {
        @Override
        public void run() {
            updateFlashlight(false /* forceDisable */);
        }
    };

    private final Runnable mKillFlashlightRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                mFlashlightEnabled = false;
            }
            updateFlashlight(true /* forceDisable */);
            dispatchOff();
        }
    };

    public interface FlashlightListener {

        /**
         * Called when the flashlight turns off unexpectedly.
         */
        void onFlashlightOff();

        /**
         * Called when there is an error that turns the flashlight off.
         */
        void onFlashlightError();

        /**
         * Called when there is a change in availability of the flashlight functionality
         * @param available true if the flashlight is currently available.
         */
        void onFlashlightAvailabilityChanged(boolean available);
    }

    // M: Turn off the flashlight when shutdown.
    private void initializeReceiver(Context context) {
        Log.i(TAG, "[initializeReceiver]");

        IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);
        filter.addAction(ACTION_SHUTDOWN_IPO);
        context.registerReceiver(mShutdownReceiver, filter);
    }

    private final BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "[onReceive]intent=" + intent);
            killFlashlight();
        }
    };
}
