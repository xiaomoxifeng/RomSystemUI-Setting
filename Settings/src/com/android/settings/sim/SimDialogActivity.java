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

package com.android.settings.sim;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.Utils;
import com.mediatek.internal.telephony.DefaultSmsSimSettings;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.ISettingsMiscExt;
import com.mediatek.settings.ext.ISimManagementExt;
import com.mediatek.settings.sim.Log;
import com.mediatek.settings.sim.SimHotSwapHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimDialogActivity extends Activity {
    private static String TAG = "SimDialogActivity";

    public static String PREFERRED_SIM = "preferred_sim";
    public static String DIALOG_TYPE_KEY = "dialog_type";
    public static final int INVALID_PICK = -1;
    public static final int DATA_PICK = 0;
    public static final int CALLS_PICK = 1;
    public static final int SMS_PICK = 2;
    public static final int PREFERRED_PICK = 3;
    public static final int PREFERRED_DATA_PICK = 4;

    public static final String IS_DUAL_SIM = "is_dual_sim";
    private boolean mIsDualSim;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle extras = getIntent().getExtras();
        /// M: Add for MTK customization @{
        init(extras);
        /// @}
        /**
         * M: bug fix for ALPS01932260, in some cases extra is null, but not checked clearly in
           what cases caused. @{
         */
        if (extras != null) {
            final int dialogType = extras.getInt(DIALOG_TYPE_KEY, INVALID_PICK);
            switch (dialogType) {
                case DATA_PICK:
                case CALLS_PICK:
                case SMS_PICK:
                    createDialog(this, dialogType).show();
                    break;
                case PREFERRED_PICK:
                    //displayPreferredDialog(extras.getInt(PREFERRED_SIM));
                    setPreferredSIM(extras.getInt(PREFERRED_SIM));
                    break;
                case PREFERRED_DATA_PICK:
                    setPreferredDataSIM(extras.getInt(PREFERRED_SIM));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid dialog type " + dialogType + " sent.");
            }
            mIsDualSim = extras.getBoolean(IS_DUAL_SIM, false);
            Log.d(TAG, "onCreate---mIsDualSim=" + mIsDualSim + " dialogType=" + dialogType);
        } else {
            Log.e(TAG, "unexpect happend");
            finish();
        }
        /// @}
    }

    private void setPreferredSIM(final int slotId) {
        Log.d(TAG, "setPreferredSIM----slotId=" + slotId);
        final Context context = getApplicationContext();
        final SubscriptionInfo sir = Utils.findRecordBySlotId(context, slotId);
        if (sir != null) {
            final int subId = sir.getSubscriptionId();
            PhoneAccountHandle phoneAccountHandle =
                    subscriptionIdToPhoneAccountHandle(subId);
            setDefaultDataSubId(context, subId);
            setDefaultSmsSubId(context, subId);
            setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
        }
    }

    private void setPreferredDataSIM(final int slotId) {
        Log.d(TAG, "setPreferredDataSIM----slotId=" + slotId);
        final Context context = getApplicationContext();
        final SubscriptionInfo sir = Utils.findRecordBySlotId(context, slotId);
        if (sir != null) {
            final int subId = sir.getSubscriptionId();
            setDefaultDataSubId(context, subId);
        }
    }

    private void displayPreferredDialog(final int slotId) {
        final Resources res = getResources();
        final Context context = getApplicationContext();
        final SubscriptionInfo sir = Utils.findRecordBySlotId(context, slotId);

        if (sir != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            String title = res.getString(R.string.sim_preferred_title);
            String text = res.getString(R.string.sim_preferred_message, sir.getDisplayName());
            ISettingsMiscExt miscExt = UtilsExt.getMiscPlugin(context);
            int subId = SubscriptionManager.getSubIdUsingPhoneId(slotId);
            title = miscExt.customizeSimDisplayString(title, subId);
            text = miscExt.customizeSimDisplayString(text, subId);
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(text);

            alertDialogBuilder.setPositiveButton(R.string.yes, new
                    DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    final int subId = sir.getSubscriptionId();
                    PhoneAccountHandle phoneAccountHandle =
                            subscriptionIdToPhoneAccountHandle(subId);
                    setDefaultDataSubId(context, subId);
                    setDefaultSmsSubId(context, subId);
                    setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
                    finish();
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.no, new
                    DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,int id) {
                    finish();
                }
            });

            Dialog dialog = alertDialogBuilder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);			
            dialog.show();
//            alertDialogBuilder.create().show();
        } else {
            finish();
        }
    }

    private static void setDefaultDataSubId(final Context context, final int subId) {
        ISettingsMiscExt miscExt = UtilsExt.getMiscPlugin(context);
        if (TelecomManager.from(context).isInCall()) {
            String textErr =
                    context.getResources().getString(R.string.default_data_switch_err_msg1);
            textErr = miscExt.customizeSimDisplayString(textErr, subId);
            Toast.makeText(context, textErr, Toast.LENGTH_SHORT).show();
            return;
        }
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        miscExt.setDefaultDataEnable(context, subId);
        subscriptionManager.setDefaultDataSubId(subId);
        String text = context.getResources().getString(R.string.data_switch_started);
        text = miscExt.customizeSimDisplayString(text, subId);
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    private static void setDefaultSmsSubId(final Context context, final int subId) {
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        subscriptionManager.setDefaultSmsSubId(subId);
    }

    private void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle phoneAccount) {
        final TelecomManager telecomManager = TelecomManager.from(this);
        telecomManager.setUserSelectedOutgoingPhoneAccount(phoneAccount);
    }

    private PhoneAccountHandle subscriptionIdToPhoneAccountHandle(final int subId) {
        final TelecomManager telecomManager = TelecomManager.from(this);
        final Iterator<PhoneAccountHandle> phoneAccounts =
                telecomManager.getCallCapablePhoneAccounts().listIterator();

        while (phoneAccounts.hasNext()) {
            final PhoneAccountHandle phoneAccountHandle = phoneAccounts.next();
            final PhoneAccount phoneAccount = telecomManager.getPhoneAccount(phoneAccountHandle);
            final String phoneAccountId = phoneAccountHandle.getId();

            if (phoneAccount.hasCapabilities(PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION)
                    && TextUtils.isDigitsOnly(phoneAccountId)
                    && Integer.parseInt(phoneAccountId) == subId){
                return phoneAccountHandle;
            }
        }

        return null;
    }

    public Dialog createDialog(final Context context, final int id) {
        final ArrayList<String> list = new ArrayList<String>();
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        final List<SubscriptionInfo> subInfoList =
            subscriptionManager.getActiveSubscriptionInfoList();
        final int selectableSubInfoLength = subInfoList == null ? 0 : subInfoList.size();

        final DialogInterface.OnClickListener selectionListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int value) {

                        final SubscriptionInfo sir;

                        switch (id) {
                            case DATA_PICK:
                                sir = subInfoList.get(value);
                                if (!mExt.switchDefaultDataSub(context, sir.getSubscriptionId())) {
                                    setDefaultDataSubId(context, sir.getSubscriptionId());
                                }
                                break;
                            case CALLS_PICK:
                                final TelecomManager telecomManager =
                                        TelecomManager.from(context);
                                final List<PhoneAccountHandle> phoneAccountsList =
                                        telecomManager.getCallCapablePhoneAccounts();
                                if (phoneAccountsList.size() == 1) {
                                    setUserSelectedOutgoingPhoneAccount(
                                            phoneAccountsList.get(value));
                                } else {
                                	value = mExt.customizeValue(value);
                                    setUserSelectedOutgoingPhoneAccount(
                                            value < 1 ? null : phoneAccountsList.get(value - 1));
                                }
                                break;
                            case SMS_PICK:
                                /// M: add for SMS "first item" @{
                                int subId = getDefaultSmsClickContent(subInfoList, value);
                                setDefaultSmsSubId(context, subId);
                                /// @}
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid dialog type "
                                        + id + " in SIM dialog.");
                        }

                        finish();
                    }

                };

        Dialog.OnKeyListener keyListener = new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                    KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        finish();
                    }
                    return true;
                }
            };

        ArrayList<SubscriptionInfo> callsSubInfoList = new ArrayList<SubscriptionInfo>();
        /// M: add for add for SMS "first item" @{
        ArrayList<SubscriptionInfo> smsSubInfoList = new ArrayList<SubscriptionInfo>();
        //// @}
        if (id == CALLS_PICK) {
            final TelecomManager telecomManager = TelecomManager.from(context);
            final Iterator<PhoneAccountHandle> phoneAccounts =
                    telecomManager.getCallCapablePhoneAccounts().listIterator();

            /// M: @{
            /// Google default
            /*
            list.add(getResources().getString(R.string.sim_calls_ask_first_prefs_title));
            callsSubInfoList.add(null);
             */
            if (telecomManager.getCallCapablePhoneAccounts().size() > 1) {
                list.add(getResources().getString(R.string.sim_calls_ask_first_prefs_title));
                callsSubInfoList.add(null);
            }
            /// @}
            while (phoneAccounts.hasNext()) {
                final PhoneAccount phoneAccount =
                        telecomManager.getPhoneAccount(phoneAccounts.next());
                list.add((String)phoneAccount.getLabel());
                // Added check to add entry into callsSubInforList only if phoneAccountId is int
                // Todo : Might have to change it later based on b/18904714
                if (phoneAccount.hasCapabilities(PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION) &&
                        TextUtils.isDigitsOnly(phoneAccount.getAccountHandle().getId())) {
                    final String phoneAccountId = phoneAccount.getAccountHandle().getId();
                    final SubscriptionInfo sir = Utils.findRecordBySubId(context,
                            Integer.parseInt(phoneAccountId));
                    callsSubInfoList.add(sir);
                } else {
                    callsSubInfoList.add(null);
                }
            }
            mExt.customizeListArray(list);
            mExt.customizeSubscriptionInfoArray(callsSubInfoList);
        } else if (id == SMS_PICK) {
            /// M: add for add for SMS "first item" @{
            initSmsData(list, subInfoList, selectableSubInfoLength, smsSubInfoList);
            /// @}
        } else {
            for (int i = 0; i < selectableSubInfoLength; ++i) {
                final SubscriptionInfo sir = subInfoList.get(i);
                CharSequence displayName = sir.getDisplayName();
                if (displayName == null) {
                    displayName = "";
                }
                list.add(displayName.toString());
            }
        }

        String[] arr = list.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        ListAdapter adapter = new SelectAccountListAdapter(
                /// M: add for add for SMS "first item" @{
                getAdapterData(id, subInfoList, callsSubInfoList, smsSubInfoList),
                /// @}
                builder.getContext(),
                R.layout.select_account_list_item,
                arr, id);

        switch (id) {
            case DATA_PICK:
                builder.setTitle(R.string.select_sim_for_data);
                break;
            case CALLS_PICK:
                builder.setTitle(R.string.select_sim_for_calls);
                break;
            case SMS_PICK:
                builder.setTitle(R.string.sim_card_select_title);
                break;
            default:
                throw new IllegalArgumentException("Invalid dialog type "
                        + id + " in SIM dialog.");
        }

        /// M: replace SIM with UIM.
        changeDialogTitle(builder, id);
        Dialog dialog = builder.setAdapter(adapter, selectionListener).create();
        dialog.setOnKeyListener(keyListener);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });

        return dialog;

    }

    private class SelectAccountListAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private int mResId;
        private int mDialogId;
        private final float OPACITY = 0.54f;
        private List<SubscriptionInfo> mSubInfoList;

        public SelectAccountListAdapter(List<SubscriptionInfo> subInfoList,
                Context context, int resource, String[] arr, int dialogId) {
            super(context, resource, arr);
            mContext = context;
            mResId = resource;
            mDialogId = dialogId;
            mSubInfoList = subInfoList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView;
            final ViewHolder holder;

            if (convertView == null) {
                // Cache views for faster scrolling
                rowView = inflater.inflate(mResId, null);
                holder = new ViewHolder();
                holder.title = (TextView) rowView.findViewById(R.id.title);
                holder.summary = (TextView) rowView.findViewById(R.id.summary);
                holder.icon = (ImageView) rowView.findViewById(R.id.icon);
                rowView.setTag(holder);
            } else {
                rowView = convertView;
                holder = (ViewHolder) rowView.getTag();
            }

            final SubscriptionInfo sir = mSubInfoList.get(position);
            if (sir == null) {
                holder.title.setText(getItem(position));
                holder.summary.setText("");
                /// M:
                /// Google Default
                /*
                holder.icon.setImageDrawable(getResources()
                        .getDrawable(R.drawable.ic_live_help));
                 */
                if (mIsDualSim) {
                    if (mDialogId == SMS_PICK &&
                        position > SubscriptionManager.from(mContext)
                                                  .getActiveSubscriptionInfoCount()) {
                        Drawable drawable = mExt.getSmsAutoItemIcon(mContext);
                        holder.icon.setImageDrawable(drawable);
                        Log.d(TAG, "mDialogId: " + mDialogId + ", position: " + position);
                    } else if (mDialogId == CALLS_PICK) {
                        setAccountBitmap(holder, position);
                    } else {
                        holder.icon.setImageDrawable(getResources()
                                .getDrawable(R.drawable.ic_live_help));
                    }
                    holder.icon.setAlpha(OPACITY);
                } else {
                    holder.icon.setImageResource(R.drawable.contacts_icon_sim);
                }
            } else {
                holder.title.setText(sir.getDisplayName());
                holder.summary.setText(sir.getNumber());
                if (mIsDualSim) {				
                    holder.icon.setImageBitmap(sir.createIconBitmap(mContext));
                } else {
                    holder.icon.setImageResource(R.drawable.contacts_icon_sim);
                }
            }
            return rowView;
        }

        private class ViewHolder {
            TextView title;
            TextView summary;
            ImageView icon;
        }

        private void setAccountBitmap(ViewHolder holder, int location) {
            Log.d(TAG, "setSipAccountBitmap()... location: " + location);
            String askFirst = getResources().getString(R.string.sim_calls_ask_first_prefs_title); 
            String lableString = getItem(location);
            final TelecomManager telecomManager = TelecomManager.from(mContext);
            List<PhoneAccountHandle> phoneAccountHandles= 
                    telecomManager.getCallCapablePhoneAccounts();
            if (!askFirst.equals(lableString)) {
                if (phoneAccountHandles.size() > 1) {
                    location = location - 1;
                }
                PhoneAccount phoneAccount = 
                        telecomManager.getPhoneAccount(phoneAccountHandles.get(location));
                Log.d(TAG, "setSipAccountBitmap()... position: " + location
                        + " account: "  + phoneAccount);
                if (phoneAccount != null) {
                    Log.d(TAG, "bitmap: " + phoneAccount.getIconBitmap());
                    holder.icon.setImageDrawable(phoneAccount.createIconDrawable(mContext));
                }
            } else {
                holder.icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_live_help));
            }
        }
    }

    //............................MTK...........................

    private SimHotSwapHandler mSimHotSwapHandler;
    private IntentFilter mIntentFilter;
    private ISimManagementExt mExt;

    // Receiver to handle different actions
    private BroadcastReceiver mSubReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mSubReceiver action = " + action);
            finish();
        }
    };

    private void init(Bundle extras) {
        mExt = UtilsExt.getSimManagmentExtPlugin(this);
        mSimHotSwapHandler = SimHotSwapHandler.newInstance(this);
        mSimHotSwapHandler.registerOnSubscriptionsChangedListener();
        mIntentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mSubReceiver, mIntentFilter);
    }

    private void initSmsData(final ArrayList<String> list,
            final List<SubscriptionInfo> subInfoList, final int selectableSubInfoLength,
            ArrayList<SubscriptionInfo> smsSubInfoList) {
        if (selectableSubInfoLength > 1) {
            list.add(getResources().getString(R.string.sim_calls_ask_first_prefs_title));
            smsSubInfoList.add(null);
        }
        for (int i = 0; i < selectableSubInfoLength; ++i) {
            final SubscriptionInfo sir = subInfoList.get(i);
            smsSubInfoList.add(sir);
            CharSequence displayName = sir.getDisplayName();
            if (displayName == null) {
                displayName = "";
            }
            list.add(displayName.toString());
        }
        mExt.customizeListArray(list);
        mExt.customizeSubscriptionInfoArray(smsSubInfoList);
        mExt.initAutoItemForSms(list, smsSubInfoList);
    }

    private List<SubscriptionInfo> getAdapterData(final int id,
            final List<SubscriptionInfo> subInfoList, ArrayList<SubscriptionInfo> callsSubInfoList,
            ArrayList<SubscriptionInfo> smsSubInfoList) {
        List<SubscriptionInfo> listForAdpter = null;
        switch (id) {
            case DATA_PICK:
                listForAdpter = subInfoList;
                break;
            case CALLS_PICK:
                listForAdpter = callsSubInfoList;
                break;
            case SMS_PICK:
                listForAdpter = smsSubInfoList;
                break;
            default:
                listForAdpter = null;
                throw new IllegalArgumentException("Invalid dialog type "
                        + id + " in SIM dialog.");
        }
        return listForAdpter;
    }

    private int getDefaultSmsClickContent(final List<SubscriptionInfo> subInfoList,
            int value) {
        int subId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    	value = mExt.customizeValue(value);
        if (value < 1) {
            int length = subInfoList == null ? 0 : subInfoList.size();
            if (length == 1) {
                subId = subInfoList.get(value).getSubscriptionId();
            } else {
                subId = DefaultSmsSimSettings.ASK_USER_SUB_ID;
            }
        } else if (value >= 1 && value < subInfoList.size() + 1) {
            subId = subInfoList.get(value - 1).getSubscriptionId();
        } else {
            subId = mExt.getDefaultSmsSubIdForAuto();
        }
        Log.d(TAG, "value: " + value + ", subId: " + subId);
        return subId;
    }

    @Override
    protected void onDestroy() {
        mSimHotSwapHandler.unregisterOnSubscriptionsChangedListener();
        unregisterReceiver(mSubReceiver);
        super.onDestroy();
    }

    /**
     * Replace SIM with UIM.
     *
     * @param builder the dialog builder need to modify.
     * @param id the dialog id.
     */
    private void changeDialogTitle(AlertDialog.Builder builder, int id) {
        ISettingsMiscExt miscExt = UtilsExt.getMiscPlugin(builder.getContext());
        switch (id) {
            case DATA_PICK:
                builder.setTitle(miscExt.customizeSimDisplayString(
                                    getResources().getString(R.string.select_sim_for_data),
                                    SubscriptionManager.INVALID_SUBSCRIPTION_ID));
                break;
            case CALLS_PICK:
                builder.setTitle(miscExt.customizeSimDisplayString(
                                    getResources().getString(R.string.select_sim_for_calls),
                                    SubscriptionManager.INVALID_SUBSCRIPTION_ID));
                break;
            case SMS_PICK:
                builder.setTitle(miscExt.customizeSimDisplayString(
                                    getResources().getString(R.string.sim_card_select_title),
                                    SubscriptionManager.INVALID_SUBSCRIPTION_ID));
                break;
            default:
                throw new IllegalArgumentException("Invalid dialog type "
                        + id + " in SIM dialog.");
        }
    }
}
