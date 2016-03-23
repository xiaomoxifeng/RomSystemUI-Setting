/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
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

package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;

import android.view.WindowManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.app.AlertDialog;
import android.widget.ListView;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.preference.SwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
//import com.android.settings.SwitchPreferenceScreen;
import com.android.settings.DialogCreatable;

public class MotionSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "MotionSettings";

//grape:Add GESTURE gesture
    private static final String KEY_GESTURE_ACTIVE = "gesture_activation";
    private static final String KEY_GESTURE_AIR_CALL_ACCEPT = "air_call_accept";
    private static final String KEY_GESTURE_AIR_LAUNCHER_ACCEPT = "air_launcher_accept";
    private static final String KEY_GESTURE_AIR_TURN_ACCEPT = "air_turn_accept";
    private static final String KEY_GESTURE_AIR_BROWSER_ACCEPT = "air_browser_accept";
    private CheckBoxPreference mGestureActive;
    private SwitchPreferenceScreen mGestureAirCallAccept;
    private SwitchPreferenceScreen mGestureAirLauncherAccept;
    private SwitchPreferenceScreen mGestureAirTurnAccept;
    private SwitchPreferenceScreen mGestureAirBrowserAccept;


    private ContentObserver mAirGestureChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
               Log.w(TAG, "mAirGestureChangeObserver---onChange:" + Settings.System.getInt(getContentResolver(), Settings.System.GESTURE_ACTIVE_SETTING, 0)); 
	        if (Settings.System.getInt(getContentResolver(), Settings.System.GESTURE_ACTIVE_SETTING, 0) != 0) {
	            mGestureActive.setChecked(true);
	        }else {
	            mGestureActive.setChecked(false); 
	        }
        }
    };

    public MotionSettings() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();
        addPreferencesFromResource(R.xml.motion_settings);

	 //grape:add
       mGestureActive = (CheckBoxPreference)findPreference(KEY_GESTURE_ACTIVE);

        mGestureAirCallAccept = (SwitchPreferenceScreen)findPreference(KEY_GESTURE_AIR_CALL_ACCEPT);
		mGestureAirCallAccept.setOnPreferenceChangeListener(this);
		
        mGestureAirLauncherAccept = (SwitchPreferenceScreen)findPreference(KEY_GESTURE_AIR_LAUNCHER_ACCEPT);
        mGestureAirLauncherAccept.setOnPreferenceChangeListener(this);
        
        mGestureAirTurnAccept = (SwitchPreferenceScreen)findPreference(KEY_GESTURE_AIR_TURN_ACCEPT);
        mGestureAirTurnAccept.setOnPreferenceChangeListener(this);

        mGestureAirBrowserAccept = (SwitchPreferenceScreen)findPreference(KEY_GESTURE_AIR_BROWSER_ACCEPT);
        mGestureAirBrowserAccept.setOnPreferenceChangeListener(this);
    }
	

    @Override
    public void onResume() {
        super.onResume();

        ContentResolver localContentResolver = getActivity().getContentResolver();
        Log.w(TAG, "onResume---charging:" + Settings.System.getInt(localContentResolver, Settings.System.MOTION_ACTIVE_SETTING, 0));		

//grape:add 
        if (Settings.System.getInt(localContentResolver, Settings.System.GESTURE_ACTIVE_SETTING, 0) != 0) {
            mGestureActive.setChecked(true);
        }else {
            mGestureActive.setChecked(false); 
        }

    	if (Settings.System.getInt(localContentResolver, Settings.System.GESTURE_AIR_CALL_ACCEPT_SETTING, 0) != 0) {
    		mGestureAirCallAccept.setChecked(true);
    	}else {
    		mGestureAirCallAccept.setChecked(false); 
    	}		

        if (Settings.System.getInt(localContentResolver, Settings.System.GESTURE_AIR_LAUNCHER_ACCEPT_SETTING, 0) != 0) {
            mGestureAirLauncherAccept.setChecked(true);
        }else {
            mGestureAirLauncherAccept.setChecked(false); 
        }		
        
        if (Settings.System.getInt(localContentResolver, Settings.System.GESTURE_AIR_TURN_ACCEPT_SETTING, 0) != 0) {
            mGestureAirTurnAccept.setChecked(true);
        }else {
            mGestureAirTurnAccept.setChecked(false); 
        }		

        if (Settings.System.getInt(localContentResolver, Settings.System.GESTURE_AIR_BROWSER_ACCEPT_SETTING, 0) != 0) {
            mGestureAirBrowserAccept.setChecked(true);
        }else {
            mGestureAirBrowserAccept.setChecked(false); 
        }		
		
        getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.GESTURE_ACTIVE_SETTING),
                        true, mAirGestureChangeObserver);		
    }

    @Override
    public void onPause(){
        super.onPause();
        getContentResolver().unregisterContentObserver(mAirGestureChangeObserver);		
    }	

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Log.d(TAG, "onPreferenceTreeClick----preference=" + preference +", key="+preference.getKey() + " preferenceScreen=" + preferenceScreen);
        ContentResolver localContentResolver = getActivity().getContentResolver();

//grape:add
        if (preference == mGestureActive) {
            if (mGestureActive.isChecked()) {
                Settings.System.putInt(localContentResolver, Settings.System.GESTURE_ACTIVE_SETTING, 1);
            }
            else {
                Settings.System.putInt(localContentResolver, Settings.System.GESTURE_ACTIVE_SETTING, 0);
            }        		
        }else if(preference.getKey().equals(KEY_GESTURE_AIR_CALL_ACCEPT) ){
                //XXXYYY just for test
		    Log.v(TAG, "onPreferenceTreeClick---KEY_GESTURE_AIR_CALL_ACCEPT");		
        }else if(preference.getKey().equals(KEY_GESTURE_AIR_LAUNCHER_ACCEPT) ){
                //XXXYYY just for test
		    Log.v(TAG, "onPreferenceTreeClick---KEY_GESTURE_AIR_LAUNCHER_ACCEPT");		
        }else if(preference.getKey().equals(KEY_GESTURE_AIR_TURN_ACCEPT) ){
                //XXXYYY just for test
		    Log.v(TAG, "onPreferenceTreeClick---KEY_GESTURE_AIR_TURN_ACCEPT");		
        }else if(preference.getKey().equals(KEY_GESTURE_AIR_BROWSER_ACCEPT) ){
                //XXXYYY just for test
		    Log.v(TAG, "onPreferenceTreeClick---KEY_GESTURE_AIR_BROWSER_ACCEPT");		
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
		int newValue = (Boolean)objValue ? 1 : 0;
        ContentResolver localContentResolver = getActivity().getContentResolver();
        Log.d(TAG, "onPreferenceChange---preference=" + preference + " newValue=" + newValue);
		
//grape:add
        if(preference == mGestureAirCallAccept) {
            Settings.System.putInt(localContentResolver, Settings.System.GESTURE_AIR_CALL_ACCEPT_SETTING, newValue);
        }
        if(preference == mGestureAirLauncherAccept) {
            Settings.System.putInt(localContentResolver, Settings.System.GESTURE_AIR_LAUNCHER_ACCEPT_SETTING, newValue);
        }
        if(preference == mGestureAirTurnAccept) {
            Settings.System.putInt(localContentResolver, Settings.System.GESTURE_AIR_TURN_ACCEPT_SETTING, newValue);
        }
        if(preference == mGestureAirBrowserAccept) {
            Settings.System.putInt(localContentResolver, Settings.System.GESTURE_AIR_BROWSER_ACCEPT_SETTING, newValue);
        }
		
        return true;
    }
}
