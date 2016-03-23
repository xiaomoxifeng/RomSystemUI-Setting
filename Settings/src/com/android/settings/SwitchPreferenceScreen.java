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

/**
 * 
 */
package com.android.settings;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.content.res.TypedArray;

import com.android.settings.R;
import com.android.settings.Utils;
import com.mediatek.xlog.Xlog;
import android.widget.Checkable;
import android.widget.Switch;
import android.widget.ImageView;
/**
 * @author mtk80800
 *
 */
public class SwitchPreferenceScreen extends Preference implements CompoundButton.OnCheckedChangeListener{
	
    private static final String XLogTAG = "MotionSettings/motion";
    private static final String TAG = "MotionSettings/motion";
    
//grape:add
    private static final String KEY_GESTURE_ACTIVE = "gesture_activation";
    private static final String KEY_GESTURE_AIR_CALL_ACCEPT = "air_call_accept";      
    private static final String KEY_GESTURE_AIR_LAUNCHER_ACCEPT = "air_launcher_accept";      
    private static final String KEY_GESTURE_AIR_TURN_ACCEPT = "air_turn_accept";      
    private static final String KEY_GESTURE_AIR_BROWSER_ACCEPT = "air_browser_accept";


    private static CompoundButton mCurrentChecked = null;
    private static String activeKey = null;
    
    private String mPreferenceTitle = null;
    private String mPreferenceSummary = null;
    
    private TextView mTextView = null;
    private TextView mSummary = null;
    private Switch mSwitch = null;
    private ImageView mImage = null;
    private boolean mEnable;
    
    private Context             mContext;
    private String              mKey;
	
    public SwitchPreferenceScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mContext = context;
        
        //get the title from motion_settings.xml
        if(super.getTitle() != null){
        	mPreferenceTitle = super.getTitle().toString();
        }
        
        //get the summary from motion_settings.xml
        if(super.getSummary() != null){
        	mPreferenceSummary = super.getSummary().toString();
        }
        
        
        mKey = getKey();
        
        setLayoutResource(R.layout.switchpreferencescreen_header_switch_item);	 
	  
        Xlog.v(XLogTAG, "SwitchPreferenceScreen setLayoutResource mSwitch="+mSwitch+",mKey="+mKey);
        Xlog.v(XLogTAG, "SwitchPreferenceScreen mPreferenceTitle ="+mPreferenceTitle+",mPreferenceSummary="+mPreferenceSummary);	
    }  
    
    public SwitchPreferenceScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }    
    
    public SwitchPreferenceScreen(Context context) {
        this(context, null);
    }

    @Override
    public void onBindView(View view) {

        super.onBindView(view);
	  ContentResolver localContentResolver = mContext.getContentResolver();	
       
	  mSwitch = (Switch) view.findViewById(R.id.switchWidget);
	  mSwitch.setOnCheckedChangeListener(this);
//	  mSwitch.setChecked(mEnable);
//        Xlog.v(XLogTAG, "onBindView mEnable="+mEnable + " mSwitch=" + mSwitch);
	  
        mImage = (ImageView)view.findViewById(R.id.icon);
        mImage.setImageResource(0/*R.drawable.wifi_signal*/);	
        mTextView = (TextView)view.findViewById(com.android.internal.R.id.title);
        mTextView.setText(mPreferenceTitle);
        mSummary = (TextView)view.findViewById(com.android.internal.R.id.summary);
        mSummary.setText(mPreferenceSummary);
        
        Xlog.v(XLogTAG, "onBindView mKey="+mKey + " mEnable=" + mEnable);
  
	  if (null == mKey)
	  {
	  	Xlog.v(XLogTAG, "onBindView onBindView  key=null");
	  }
//grape:add
        else if(mKey.equals(KEY_GESTURE_AIR_CALL_ACCEPT)) {
            if (Settings.System.getInt(localContentResolver, Settings.System.GESTURE_AIR_CALL_ACCEPT_SETTING, 0) != 0) {
                setChecked(true);
            }else {
                setChecked(false); 
            } 
        }
        else if(mKey.equals(KEY_GESTURE_AIR_LAUNCHER_ACCEPT)) {
            if (Settings.System.getInt(localContentResolver, Settings.System.GESTURE_AIR_LAUNCHER_ACCEPT_SETTING, 0) != 0) {
                setChecked(true);
            }else {
                setChecked(false); 
            } 
        }
        else if(mKey.equals(KEY_GESTURE_AIR_TURN_ACCEPT)) {
            if (Settings.System.getInt(localContentResolver, Settings.System.GESTURE_AIR_TURN_ACCEPT_SETTING, 0) != 0) {
                setChecked(true);
            }else {
                setChecked(false); 
            } 
        }		
        else if(mKey.equals(KEY_GESTURE_AIR_BROWSER_ACCEPT)) {
            if (Settings.System.getInt(localContentResolver, Settings.System.GESTURE_AIR_BROWSER_ACCEPT_SETTING, 0) != 0) {
                setChecked(true);
            }else {
                setChecked(false); 
            } 
        }		
    }


    public void setChecked(boolean isChecked) {
		if (null != mSwitch) {
		mSwitch.setChecked(isChecked);
		}
		mEnable = isChecked;
		Xlog.v(XLogTAG, "setChecked isChecked="+isChecked+",mEnable="+mEnable);
    }
	
    public void onClick() {
    	Xlog.v(XLogTAG, "onClick");
    }
    
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Xlog.v(XLogTAG, "onCheckedChanged isChecked=" + isChecked +", key="+mKey);

        
        ContentResolver localContentResolver = mContext.getContentResolver();
	  if (null == mKey)
	  {
	  	Xlog.v(XLogTAG, "onCheckedChanged isChecked=" + isChecked +", key=null");
	  }
//grape:add 
        else if(mKey.equals(KEY_GESTURE_AIR_CALL_ACCEPT)) {
            Settings.System.putInt(localContentResolver, Settings.System.GESTURE_AIR_CALL_ACCEPT_SETTING, (isChecked ? 1 :0));        
        }else if(mKey.equals(KEY_GESTURE_AIR_LAUNCHER_ACCEPT)) {
            Settings.System.putInt(localContentResolver, Settings.System.GESTURE_AIR_LAUNCHER_ACCEPT_SETTING, (isChecked ? 1 :0));        
        }else if(mKey.equals(KEY_GESTURE_AIR_TURN_ACCEPT)) {
            Settings.System.putInt(localContentResolver, Settings.System.GESTURE_AIR_TURN_ACCEPT_SETTING, (isChecked ? 1 :0));        
        }else if(mKey.equals(KEY_GESTURE_AIR_BROWSER_ACCEPT)) {
            Settings.System.putInt(localContentResolver, Settings.System.GESTURE_AIR_BROWSER_ACCEPT_SETTING, (isChecked ? 1 :0));        
        }		
		
        mSwitch.setChecked(isChecked);
    }

     public void setPreferenceKey(String key) {
    	 setKey(key);
    	 mKey = key;
     }

}
