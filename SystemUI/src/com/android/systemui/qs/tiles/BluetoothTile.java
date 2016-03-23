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
 * limitations under the License.
 */

package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.systemui.R;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSDetailItems.Item;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.BluetoothController.PairedDevice;

import java.util.Set;

/** Quick settings tile: Bluetooth **/
public class BluetoothTile extends QSTile<QSTile.BooleanState>  {
    private static final Intent BLUETOOTH_SETTINGS = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
    private static final String TAG = "BluetoothController";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private final BluetoothController mController;
    private final BluetoothDetailAdapter mDetailAdapter;

    /// M: Avoid access ArrayList with Multi-threads @{
    private boolean mListening;
    /// M: Avoid access ArrayList with Multi-threads @}

    public BluetoothTile(Host host) {
        super(host);

        mController = host.getBluetoothController();

        /// M: Avoid access ArrayList with Multi-threads @{
        mListening = false;
        mController.addStateChangedCallback(mCallback);
        /// M: Avoid access ArrayList with Multi-threads @}

        mDetailAdapter = new BluetoothDetailAdapter();
    }

    /// M: Avoid access ArrayList with Multi-threads @{
    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        mController.removeStateChangedCallback(mCallback);
    }
    /// M: Avoid access ArrayList with Multi-threads @}

    @Override
    public boolean supportsDualTargets() {
        return false; //grape_S6
    }

    @Override
    public DetailAdapter getDetailAdapter() {
        return mDetailAdapter;
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        mListening = listening;
    }

    @Override
    protected void handleClick() {
        final boolean isEnabled = (Boolean)mState.value;
        mController.setBluetoothEnabled(!isEnabled);
    }

    @Override
    protected void handleSecondaryClick() {
        if (!mState.value) {
            mState.value = true;
            mController.setBluetoothEnabled(true);
        }
        showDetail(true);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final boolean supported = mController.isBluetoothSupported();
        final boolean enabled = mController.isBluetoothEnabled();
        final boolean connected = mController.isBluetoothConnected();
        final boolean connecting = mController.isBluetoothConnecting();
        final int numOfDevices = getNumOfConnectedDevices();
        state.visible = supported;
        state.value = enabled;
        state.autoMirrorDrawable = false;
        if (enabled) {
            if (DEBUG) Log.d(TAG, "handleUpdateState enable");
            state.label = null;
            if (connected) {
                state.icon = ResourceIcon.get(R.drawable.ic_qs_bluetooth_connected);
                state.iconId = R.drawable.tw_quick_panel_icon_bluetooth_on;
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_bluetooth_connected);
                /// M: [ALPS01910195] For multiple bluetooth devices @{
                if (numOfDevices > 1) {
                    state.label = mContext.getString(
                        R.string.quick_settings_bluetooth_multiple_devices_label,
                        numOfDevices);
                } else {
                    state.label = mController.getLastDeviceName();
                }
                /// M: [ALPS01910195] For multiple bluetooth devices @}
            } else if (connecting) {
                state.iconId = R.drawable.tw_quick_panel_icon_bluetooth_on;
                state.icon = ResourceIcon.get(R.drawable.ic_qs_bluetooth_connecting);
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_bluetooth_connecting);
                state.label = mContext.getString(R.string.quick_settings_bluetooth_label);
            } else {
                state.iconId = R.drawable.tw_quick_panel_icon_bluetooth_on; //grape_S6
                state.icon = ResourceIcon.get(R.drawable.ic_qs_bluetooth_on);
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_bluetooth_on);
            }
            if (TextUtils.isEmpty(state.label)) {
                state.label = mContext.getString(R.string.quick_settings_bluetooth_label);
            }
        } else {
            state.iconId = R.drawable.tw_quick_panel_icon_bluetooth_off;
            state.icon = ResourceIcon.get(R.drawable.ic_qs_bluetooth_off);
            state.label = mContext.getString(R.string.quick_settings_bluetooth_label);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_bluetooth_off);
        }

        String bluetoothName = state.label;
        if (connected) {
            bluetoothName = state.dualLabelContentDescription = mContext.getString(
                    R.string.accessibility_bluetooth_name, state.label);
            if (DEBUG) Log.d(TAG, "handleUpdateState bluetoothName=" + bluetoothName);
        }
        state.dualLabelContentDescription = bluetoothName;
        Log.d(TAG, "state.contentDescription=" + state.contentDescription
            + " state.label=" + state.label
            + " state.dualLabelContentDescription=" + state.dualLabelContentDescription
            + " enabled=" + enabled
            + " connected=" + connected
            + " connecting=" + connecting
            + " numOfDevices=" + numOfDevices);
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (DEBUG) Log.d(TAG, "composeChangeAnnouncement mState.value=" + mState.value);
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_bluetooth_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_bluetooth_changed_off);
        }
    }

    private final BluetoothController.Callback mCallback = new BluetoothController.Callback() {
        @Override
        public void onBluetoothStateChange(boolean enabled, boolean connecting) {
            /// M: Avoid access ArrayList with Multi-threads @{
            if (mListening == true) {
                refreshState();
            }
            /// M: Avoid access ArrayList with Multi-threads @}
        }
        @Override
        public void onBluetoothPairedDevicesChanged() {
            /// M: Avoid access ArrayList with Multi-threads @{
            if (mListening == true) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (DEBUG) {
                            Log.d(TAG, "onBluetoothPairedDevicesChanged");
                        }
                        mDetailAdapter.updateItems();
                    }
                });
                refreshState();
            }
            /// M: Avoid access ArrayList with Multi-threads @}
        }
    };

    /// M: [ALPS01910195] For multiple bluetooth devices @{
    private int getNumOfConnectedDevices() {
        int count = 0;
        final Set<PairedDevice> devices = mController.getPairedDevices();
        for (PairedDevice device : devices) {
            if (device.state == PairedDevice.STATE_CONNECTED) {
                ++count;
            }
        }
        return count;
    }
    /// M: [ALPS01910195] For multiple bluetooth devices @}

    private final class BluetoothDetailAdapter implements DetailAdapter, QSDetailItems.Callback {
        private QSDetailItems mItems;

        @Override
        public int getTitle() {
            return R.string.quick_settings_bluetooth_label;
        }

        @Override
        public Boolean getToggleState() {
            return mState.value;
        }

        @Override
        public Intent getSettingsIntent() {
            return BLUETOOTH_SETTINGS;
        }

        @Override
        public void setToggleState(boolean state) {
            mController.setBluetoothEnabled(state);
            if (DEBUG) Log.d(TAG, "setToggleState");
            showDetail(false);
        }

        @Override
        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            if (DEBUG) Log.d(TAG, "createDetailView");
            mItems = QSDetailItems.convertOrInflate(context, convertView, parent);
            mItems.setTagSuffix("Bluetooth");
            mItems.setEmptyState(R.drawable.ic_qs_bluetooth_detail_empty,
                    R.string.quick_settings_bluetooth_detail_empty_text);
            mItems.setCallback(this);
            mItems.setMinHeightInItems(0);
            updateItems();
            setItemsVisible(mState.value);
            return mItems;
        }

        public void setItemsVisible(boolean visible) {
            if (mItems == null) return;
            mItems.setItemsVisible(visible);
        }

        private void updateItems() {
            if (DEBUG) Log.d(TAG, "updateItems");
            if (mItems == null) return;
            Item[] items = null;
            final Set<PairedDevice> devices = mController.getPairedDevices();
            if (devices != null) {
                items = new Item[devices.size()];
                int i = 0;
                for (PairedDevice device : devices) {
                    final Item item = new Item();
                    item.icon = R.drawable.ic_qs_bluetooth_on;
                    item.line1 = device.name;
                    if (device.state == PairedDevice.STATE_CONNECTED) {
                        item.icon = R.drawable.ic_qs_bluetooth_connected;
                        item.line2 = mContext.getString(R.string.quick_settings_connected);
                        item.canDisconnect = true;
                    } else if (device.state == PairedDevice.STATE_CONNECTING) {
                        item.icon = R.drawable.ic_qs_bluetooth_connecting;
                        item.line2 = mContext.getString(R.string.quick_settings_connecting);
                    }
                    item.tag = device;
                    items[i++] = item;
                    Log.d(TAG, "updateItems item=" + item);
                }
            }
            mItems.setItems(items);
        }

        @Override
        public void onDetailItemClick(Item item) {
            if (item == null || item.tag == null) return;
            final PairedDevice device = (PairedDevice) item.tag;
            if (device != null && device.state == PairedDevice.STATE_DISCONNECTED) {
                mController.connect(device);
                if (DEBUG) Log.d(TAG, "onDetailItemClick device=" + device);
            }
        }

        @Override
        public void onDetailItemDisconnect(Item item) {
            if (item == null || item.tag == null) return;
            final PairedDevice device = (PairedDevice) item.tag;
            if (device != null) {
                mController.disconnect(device);
                if (DEBUG) Log.d(TAG, "onDetailItemDisconnect device=" + device);
            }
        }
    }
}
