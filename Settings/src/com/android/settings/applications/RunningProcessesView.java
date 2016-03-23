/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.settings.applications;

import android.content.res.Resources;
import android.text.BidiFormatter;
import com.android.internal.util.MemInfoReader;
import com.android.settings.R;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.RecyclerListener;
import com.android.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

//grape_memorydummy
import android.os.Build;
import com.mediatek.settings.FeatureOption;

public class RunningProcessesView extends FrameLayout
        implements AdapterView.OnItemClickListener, RecyclerListener,
        RunningState.OnRefreshUiListener {

    static final String TAG = "RunningProcessesView";
    
    final int mMyUserId;

    long SECONDARY_SERVER_MEM;
    /// M: Record foregroundAppThreshold {@
    long FOREGROUND_APP_MEM;
    /// @}
    final HashMap<View, ActiveItem> mActiveItems = new HashMap<View, ActiveItem>();

    ActivityManager mAm;
    
    RunningState mState;
    
    Fragment mOwner;
    
    Runnable mDataAvail;

    StringBuilder mBuilder = new StringBuilder(128);
    
    RunningState.BaseItem mCurSelected;
    
    ListView mListView;
    View mHeader;
    ServiceListAdapter mAdapter;
    LinearColorBar mColorBar;
    TextView mBackgroundProcessPrefix;
    TextView mAppsProcessPrefix;
    TextView mForegroundProcessPrefix;
    TextView mBackgroundProcessText;
    TextView mAppsProcessText;
    TextView mForegroundProcessText;

    long mCurTotalRam = -1;
    long mCurHighRam = -1;      // "System" or "Used"
    long mCurMedRam = -1;       // "Apps" or "Cached"
    long mCurLowRam = -1;       // "Free"
    boolean mCurShowCached = false;

    Dialog mCurDialog;

    MemInfoReader mMemInfoReader = new MemInfoReader();

    public static class ActiveItem {
        View mRootView;
        RunningState.BaseItem mItem;
        ActivityManager.RunningServiceInfo mService;
        ViewHolder mHolder;
        long mFirstRunTime;
        boolean mSetBackground;

        void updateTime(Context context, StringBuilder builder) {
            TextView uptimeView = null;
            
            if (mItem instanceof RunningState.ServiceItem) {
                // If we are displaying a service, then the service
                // uptime goes at the top.
                uptimeView = mHolder.size;
                
            } else {
                String size = mItem.mSizeStr != null ? mItem.mSizeStr : "";
                if (!size.equals(mItem.mCurSizeStr)) {
                    mItem.mCurSizeStr = size;
                    mHolder.size.setText(size);
                }
                
                if (mItem.mBackground) {
                    // This is a background process; no uptime.
                    if (!mSetBackground) {
                        mSetBackground = true;
                        mHolder.uptime.setText("");
                    }
                } else if (mItem instanceof RunningState.MergedItem) {
                    // This item represents both services and processes,
                    // so show the service uptime below.
                    uptimeView = mHolder.uptime;
                }
            }

            if (uptimeView != null) {
                mSetBackground = false;
                if (mFirstRunTime >= 0) {
                    //Log.i("foo", "Time for " + mItem.mDisplayLabel
                    //        + ": " + (SystemClock.uptimeMillis()-mFirstRunTime));
                    uptimeView.setText(DateUtils.formatElapsedTime(builder,
                            (SystemClock.elapsedRealtime()-mFirstRunTime)/1000));
                } else {
                    boolean isService = false;
                    if (mItem instanceof RunningState.MergedItem) {
                        isService = ((RunningState.MergedItem)mItem).mServices.size() > 0;
                    }
                    if (isService) {
                        uptimeView.setText(context.getResources().getText(
                                R.string.service_restarting));
                    } else {
                        uptimeView.setText("");
                    }
                }
            }
        }
    }
    
    public static class ViewHolder {
        public View rootView;
        public ImageView icon;
        public TextView name;
        public TextView description;
        public TextView size;
        public TextView uptime;
        
        public ViewHolder(View v) {
            rootView = v;
            icon = (ImageView)v.findViewById(R.id.icon);
            name = (TextView)v.findViewById(R.id.name);
            description = (TextView)v.findViewById(R.id.description);
            size = (TextView)v.findViewById(R.id.size);
            uptime = (TextView)v.findViewById(R.id.uptime);
            v.setTag(this);
        }
        
        public ActiveItem bind(RunningState state, RunningState.BaseItem item,
                StringBuilder builder) {
            synchronized (state.mLock) {
                PackageManager pm = rootView.getContext().getPackageManager();
                if (item.mPackageInfo == null && item instanceof RunningState.MergedItem) {
                    // Items for background processes don't normally load
                    // their labels for performance reasons.  Do it now.
                    RunningState.MergedItem mergedItem = (RunningState.MergedItem)item;
                    if (mergedItem.mProcess != null) {
                        ((RunningState.MergedItem)item).mProcess.ensureLabel(pm);
                        item.mPackageInfo = ((RunningState.MergedItem)item).mProcess.mPackageInfo;
                        item.mDisplayLabel = ((RunningState.MergedItem)item).mProcess.mDisplayLabel;
                    }
                }
                name.setText(item.mDisplayLabel);
                ActiveItem ai = new ActiveItem();
                ai.mRootView = rootView;
                ai.mItem = item;
                ai.mHolder = this;
                ai.mFirstRunTime = item.mActiveSince;
                if (item.mBackground) {
                    description.setText(rootView.getContext().getText(R.string.cached));
                } else {
                    description.setText(item.mDescription);
                }
                item.mCurSizeStr = null;
                icon.setImageDrawable(item.loadIcon(rootView.getContext(), state));
                icon.setVisibility(View.VISIBLE);
                ai.updateTime(rootView.getContext(), builder);
                return ai;
            }
        }
    }
    
    static class TimeTicker extends TextView {
        public TimeTicker(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }
    
    class ServiceListAdapter extends BaseAdapter {
        final RunningState mState;
        final LayoutInflater mInflater;
        boolean mShowBackground;
        ArrayList<RunningState.MergedItem> mOrigItems;
        final ArrayList<RunningState.MergedItem> mItems
                = new ArrayList<RunningState.MergedItem>();
        
        ServiceListAdapter(RunningState state) {
            mState = state;
            mInflater = (LayoutInflater)getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            refreshItems();
        }

        void setShowBackground(boolean showBackground) {
            if (mShowBackground != showBackground) {
                mShowBackground = showBackground;
                mState.setWatchingBackgroundItems(showBackground);
                refreshItems();
                refreshUi(true);
            }
        }

        boolean getShowBackground() {
            return mShowBackground;
        }

        void refreshItems() {
            ArrayList<RunningState.MergedItem> newItems =
                mShowBackground ? mState.getCurrentBackgroundItems()
                        : mState.getCurrentMergedItems();
            if (mOrigItems != newItems) {
                mOrigItems = newItems;
                if (newItems == null) {
                    mItems.clear();
                } else {
                    mItems.clear();
                    mItems.addAll(newItems);
                    if (mShowBackground) {
                        Collections.sort(mItems, mState.mBackgroundComparator);
                    }
                }
            }
        }
        
        public boolean hasStableIds() {
            return true;
        }
        
        public int getCount() {
            return mItems.size();
        }

        @Override
        public boolean isEmpty() {
            return mState.hasData() && mItems.size() == 0;
        }

        public Object getItem(int position) {
            return mItems.get(position);
        }

        public long getItemId(int position) {
            return mItems.get(position).hashCode();
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int position) {
            /// M: {@ fix JE when scolling to margin sometimes
            if (position >= mItems.size()) {
                return false;
            }
            /// @}
            return !mItems.get(position).mIsProcess;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                v = newView(parent);
            } else {
                v = convertView;
            }
            bindView(v, position);
            return v;
        }
        
        public View newView(ViewGroup parent) {
            View v = mInflater.inflate(R.layout.running_processes_item, parent, false);
            new ViewHolder(v);
            return v;
        }
        
        public void bindView(View view, int position) {
            synchronized (mState.mLock) {
                if (position >= mItems.size()) {
                    // List must have changed since we last reported its
                    // size...  ignore here, we will be doing a data changed
                    // to refresh the entire list.
                    return;
                }
                ViewHolder vh = (ViewHolder) view.getTag();
                RunningState.MergedItem item = mItems.get(position);
                ActiveItem ai = vh.bind(mState, item, mBuilder);
                mActiveItems.put(view, ai);
            }
        }
    }

    void refreshUi(boolean dataChanged) {
        if (dataChanged) {
            ServiceListAdapter adapter = mAdapter;
            adapter.refreshItems();
            adapter.notifyDataSetChanged();
        }
        
        if (mDataAvail != null) {
            mDataAvail.run();
            mDataAvail = null;
        }

        mMemInfoReader.readMemInfo();

        /*
                Cache_Free_without_Extra    =      Cache + Free + Background.PSS + (Background.PSwap * 1/ CompressRatio)
                Cache_Free_with_Extra         =      Cache Free_without_Extra +  ExtraAvailableSize 
                ExtraAvailableSize   = (LRU- AnonymousReserve - Background.PSS) (1-1/ CompressRatio)
                */


        /// M: get total size @{
        boolean isDefaultCacheFree = SystemProperties.getBoolean("ro.default_cache_free", false);
        Log.d(TAG, "isDefaultCacheFree = " + isDefaultCacheFree);
        /// @}

        synchronized (mState.mLock) {


            /// M: get zram extra available memry @{
            final float zramCompressRatio = Process.getZramCompressRatio();            
            long extraAvailableSize = 0;
            if (!isDefaultCacheFree) {
                final long anonReserve = 15 * 1024 * 1024; // 15MB for reserved annon memory
                long anonToCompress = Process.getLruAnonMemory() - anonReserve - mState.mBackgroundProcessMemory;
                Log.d(TAG, "Process.getLruAnonMemory() = " + Process.getLruAnonMemory());
                Log.d(TAG, "mState.mBackgroundProcessMemory = " + mState.mBackgroundProcessMemory);
                if (anonToCompress > 0) {
                    extraAvailableSize = (long)(anonToCompress * (1.0f - 1.0f/zramCompressRatio));
                }
                else {
                    Log.d(TAG, "!!!ERROR!!! annonToCompress = " + anonToCompress);
                }
                /// M: Consider following parts to show MemoryFree for foreground app @{
                /// FOREGROUND_APP_MEM: to consider LMK threshold
                /// Mapped, Buffer: Counting caches that are mapped in to processes.
                if (extraAvailableSize > 0) {
                    extraAvailableSize = extraAvailableSize
                                        + mMemInfoReader.getMappedSize()
                                        - mMemInfoReader.getBuffersSize()
                                        - FOREGROUND_APP_MEM;
                }
            }
            Log.d(TAG, "extraAvailableSize = " + extraAvailableSize);
            /// @}

            if (mCurShowCached != mAdapter.mShowBackground) {
                mCurShowCached = mAdapter.mShowBackground;
                if (mCurShowCached) {
                    mForegroundProcessPrefix.setText(getResources().getText(
                            R.string.running_processes_header_used_prefix));
                    mAppsProcessPrefix.setText(getResources().getText(
                            R.string.running_processes_header_cached_prefix));
                } else {
                    mForegroundProcessPrefix.setText(getResources().getText(
                            R.string.running_processes_header_system_prefix));
                    mAppsProcessPrefix.setText(getResources().getText(
                            R.string.running_processes_header_apps_prefix));
                }
            }

            final long totalRam;// = mMemInfoReader.getTotalSize(); grape_S6
            final long medRam;
            final long lowRam;            
            
            if (true == FeatureOption.MACLAND_MEMORY_DUMMY_FAKE_FLAG) //MACLAND_MEMORY_DUMMY_FAKE_FLAG
            {
                totalRam =  (long)(((float)Build.getDummyRamMemorySize()/1000f)*1024f*1024f*1024f);
            }
            else
                totalRam = mMemInfoReader.getTotalSize();

            /// M: Add pswap memory uasge @{
            Log.d(TAG, "getFreeSize = " + mMemInfoReader.getFreeSize());
            Log.d(TAG, "getCachedSize = " + mMemInfoReader.getCachedSize());
            Log.d(TAG, "mBackgroundProcessMemory = " + mState.mBackgroundProcessMemory);
            Log.d(TAG, "extraAvailableSize = " + extraAvailableSize);
            Log.d(TAG, "mBackgroundProcessSwapMemory = " + mState.mBackgroundProcessSwapMemory);
            Log.d(TAG, "Process.getZramCompressRatio() = " + zramCompressRatio);
            
            if (mCurShowCached) {
                lowRam = mMemInfoReader.getFreeSize() + mMemInfoReader.getCachedSize() + extraAvailableSize;
                medRam = (long)(mState.mBackgroundProcessMemory
                        + (float)(mState.mBackgroundProcessSwapMemory*1.0f)/zramCompressRatio);
            } else {                
                lowRam = (long)(mMemInfoReader.getFreeSize() + mMemInfoReader.getCachedSize()
                        + mState.mBackgroundProcessMemory + extraAvailableSize
                        + (float)(mState.mBackgroundProcessSwapMemory*1.0f)/zramCompressRatio);
                medRam = mState.mServiceProcessMemory;

            }
            Log.d(TAG, "lowRam = " + lowRam);
            Log.d(TAG, "medRam = " + medRam);
            /// @}            
            
            final long highRam = totalRam - medRam - lowRam;

            if (mCurTotalRam != totalRam || mCurHighRam != highRam || mCurMedRam != medRam
                    || mCurLowRam != lowRam) {
                mCurTotalRam = totalRam;
                mCurHighRam = highRam;
                mCurMedRam = medRam;
                mCurLowRam = lowRam;
                BidiFormatter bidiFormatter = BidiFormatter.getInstance();
                String sizeStr = bidiFormatter.unicodeWrap(
                        Formatter.formatShortFileSize(getContext(), lowRam));
                mBackgroundProcessText.setText(getResources().getString(
                        R.string.running_processes_header_ram, sizeStr));
                sizeStr = bidiFormatter.unicodeWrap(
                        Formatter.formatShortFileSize(getContext(), medRam));
                mAppsProcessText.setText(getResources().getString(
                        R.string.running_processes_header_ram, sizeStr));
                sizeStr = bidiFormatter.unicodeWrap(
                        Formatter.formatShortFileSize(getContext(), highRam));
                mForegroundProcessText.setText(getResources().getString(
                        R.string.running_processes_header_ram, sizeStr));
                mColorBar.setRatios(highRam/(float)totalRam,
                        medRam/(float)totalRam,
                        lowRam/(float)totalRam);
            }
        }
    }
    
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        ListView l = (ListView)parent;
        RunningState.MergedItem mi = (RunningState.MergedItem)l.getAdapter().getItem(position);
        mCurSelected = mi;
        startServiceDetailsActivity(mi);
    }

    // utility method used to start sub activity
    private void startServiceDetailsActivity(RunningState.MergedItem mi) {
    	/// ALPS01761331 M: {@
    	if (mi == null) {
    		Log.d(TAG,"mi is null");
    		return;
    	}
    	/// @}
    	
        if (mOwner != null) {
            // start new fragment to display extended information
            Bundle args = new Bundle();
            if (mi.mProcess != null) {
                args.putInt(RunningServiceDetails.KEY_UID, mi.mProcess.mUid);
                args.putString(RunningServiceDetails.KEY_PROCESS, mi.mProcess.mProcessName);
            }
            args.putInt(RunningServiceDetails.KEY_USER_ID, mi.mUserId);
            args.putBoolean(RunningServiceDetails.KEY_BACKGROUND, mAdapter.mShowBackground);

            SettingsActivity sa = (SettingsActivity) mOwner.getActivity();
            sa.startPreferencePanel(RunningServiceDetails.class.getName(), args,
                    R.string.runningservicedetails_settings_title, null, null, 0);
        }
    }
    
    public void onMovedToScrapHeap(View view) {
        mActiveItems.remove(view);
    }

    public RunningProcessesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMyUserId = UserHandle.myUserId();
    }
    
    public void doCreate(Bundle savedInstanceState) {
        mAm = (ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);
        mState = RunningState.getInstance(getContext());
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.running_processes_view, this);
        mListView = (ListView)findViewById(android.R.id.list);
        View emptyView = findViewById(com.android.internal.R.id.empty);
        if (emptyView != null) {
            mListView.setEmptyView(emptyView);
        }
        mListView.setOnItemClickListener(this);
        mListView.setRecyclerListener(this);
        mAdapter = new ServiceListAdapter(mState);
        mListView.setAdapter(mAdapter);
        mHeader = inflater.inflate(R.layout.running_processes_header, null);
        mListView.addHeaderView(mHeader, null, false /* set as not selectable */);
        mColorBar = (LinearColorBar)mHeader.findViewById(R.id.color_bar);
        Resources res = getResources();
        mColorBar.setColors(res.getColor(R.color.running_processes_system_ram),
                res.getColor(R.color.running_processes_apps_ram),
                res.getColor(R.color.running_processes_free_ram));
        mBackgroundProcessPrefix = (TextView)mHeader.findViewById(R.id.freeSizePrefix);
        mAppsProcessPrefix = (TextView)mHeader.findViewById(R.id.appsSizePrefix);
        mForegroundProcessPrefix = (TextView)mHeader.findViewById(R.id.systemSizePrefix);
        mBackgroundProcessText = (TextView)mHeader.findViewById(R.id.freeSize);
        mAppsProcessText = (TextView)mHeader.findViewById(R.id.appsSize);
        mForegroundProcessText = (TextView)mHeader.findViewById(R.id.systemSize);

        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        mAm.getMemoryInfo(memInfo);
        SECONDARY_SERVER_MEM = memInfo.secondaryServerThreshold;
        /// M: Record foregroundAppThreshold {@
        FOREGROUND_APP_MEM = memInfo.foregroundAppThreshold;;
        /// @}
    }
    
    public void doPause() {
        mState.pause();
        mDataAvail = null;
        mOwner = null;
    }

    public boolean doResume(Fragment owner, Runnable dataAvail) {
        mOwner = owner;
        mState.resume(this);
        if (mState.hasData()) {
            // If the state already has its data, then let's populate our
            // list right now to avoid flicker.
            refreshUi(true);
            return true;
        }
        mDataAvail = dataAvail;
        return false;
    }

    void updateTimes() {
        Iterator<ActiveItem> it = mActiveItems.values().iterator();
        while (it.hasNext()) {
            ActiveItem ai = it.next();
            if (ai.mRootView.getWindowToken() == null) {
                // Clean out any dead views, just in case.
                it.remove();
                continue;
            }
            ai.updateTime(getContext(), mBuilder);
        }
    }

    @Override
    public void onRefreshUi(int what) {
        switch (what) {
            case REFRESH_TIME:
                updateTimes();
                break;
            case REFRESH_DATA:
                refreshUi(false);
                updateTimes();
                break;
            case REFRESH_STRUCTURE:
                refreshUi(true);
                updateTimes();
                break;
        }
    }
}