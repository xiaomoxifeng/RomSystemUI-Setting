// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.android.settings.instruct;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.internal.app.LocalePicker;
import java.util.Locale;
import com.android.settings.R;
import com.bird.logo.LogoJNI;
import android.util.Log;
import android.widget.EditText;
import android.app.AlertDialog;
import android.text.InputType;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.os.Handler;
import android.os.Build;

// Referenced classes of package com.android.settings.instruct:
//            RadioValueItemView, SingleValueItemView

public class DefaultValueActivity extends Activity   
{
    private Context mContext;
    private InputMethodManager imm;
    final Handler mHandler = new Handler();
    public DefaultValueActivity()
    {
        mContext = this;
    }

    private void initSingleItem(LinearLayout linearlayout)
    {
        String s = SystemProperties.get("ro.param.instruct.item", "LOGO:ROM:CPU:LTE");//"LOGO:ROM:RAM:CPU:LTE"
        final String[] LogoItems = {"default",       //now only max = 5
                              "G9200",
                              "G9250",
                              "G920F",
                              "G920I",
                              "G920T",
                              "G9208",
                              "G920A",
                              "G920S",
                              "G920V"
              }; 
//real HW version
         linearlayout.addView((new SingleValueItemView(this, null) {
        
                     protected String getButtonText()
             {
                 return Build.MAC_HW;
             }
        
             public void onClick(View view)
             {
             }
         }
        ).getView());




        //LOGO
        int  logotype = LogoJNI.getLogoType();

        if (logotype >= LogoItems.length)
        {
        	logotype = 0;
            LogoJNI.setLogoType(0);
        }
        
        Log.v("DefaultValueActivity", "logotype = "+logotype + ",length = "+LogoItems.length );
        Log.v("DefaultValueActivity", "getSdSize = "+LogoJNI.getSdSize() + ",getOperatorSig = "+LogoJNI.getOperatorSig());
        SystemProperties.set("persist.sys.logo_switch", LogoItems[logotype]);

        
        RadioValueItemView radiovalueitemview = new RadioValueItemView(this, "persist.sys.logo_switch", LogoItems[0]) {

            protected String getTitle()
            {
                return getString(R.string.instruct_value_boot_animation_title);
            }

            protected void onCreateSingleItem(RadioValueItemView.SingleItem singleitem)
            {
                for (int i = 0; i < LogoItems.length; i++ )
                {
                	if (0 == i)
                       singleitem.add("Android", LogoItems[i]); 
                    else    
                	   singleitem.add(LogoItems[i], LogoItems[i]);
                }
                /*singleitem.add(LogoItems[0], LogoItems[0]);
                             singleitem.add(LogoItems[1], LogoItems[1]);
                             singleitem.add(LogoItems[2], LogoItems[2]);
                             singleitem.add(LogoItems[3], LogoItems[3]);
                             singleitem.add(LogoItems[4], LogoItems[4]);*/
            }

	        protected void onValueChanged(String s1)
	        {
	            Log.v("DefaultValueActivity", "value = "+s1);


                for (int i = 0; i < LogoItems.length; i++)
                {
                	if (LogoItems[i].equals(s1))
                    {
	                    LogoJNI.setLogoType(i);
	            	    SystemProperties.set("persist.sys.logo_switch", LogoItems[i]);                    	
                    	return;
                    }                    
                }
                LogoJNI.setLogoType(0);
                SystemProperties.set("persist.sys.logo_switch", LogoItems[0]);//real grape_S6 fake

       /*         
	            if(LogoItems[0].equals(s1))
	            {
	                LogoJNI.setLogoType(0);
	            	SystemProperties.set("persist.sys.logo_switch", LogoItems[0]);
                }
	            else if(LogoItems[1].equals(s1))
	            {
	                LogoJNI.setLogoType(1);
	                SystemProperties.set("persist.sys.logo_switch", LogoItems[1]);
                }
	            else if(LogoItems[2].equals(s1))
	            {
	                LogoJNI.setLogoType(2);
	                SystemProperties.set("persist.sys.logo_switch", LogoItems[2]);
                }
	            else if(LogoItems[3].equals(s1))
	            {
	                LogoJNI.setLogoType(3);
	                SystemProperties.set("persist.sys.logo_switch", LogoItems[3]);
                } 
	            else if(LogoItems[4].equals(s1))
	            {
	                LogoJNI.setLogoType(4);
	                SystemProperties.set("persist.sys.logo_switch", LogoItems[4]);
                }                
	            else
                {
                    LogoJNI.setLogoType(0);
	                SystemProperties.set("persist.sys.logo_switch", LogoItems[0]);//real grape_S6 fake
	            }*/
	        }

            
        };

        
        if(s.contains("LOGO"))
            linearlayout.addView(radiovalueitemview.getView());

        //BOOT
        RadioValueItemView radiovalueitemview_boot = new RadioValueItemView(this, "persist.sys.boot_animation", "3X") {

            protected String getTitle()
            {
                return getString(R.string.instruct_value_bootanimation_title);
            }

            protected void onCreateSingleItem(RadioValueItemView.SingleItem singleitem)
            {
                singleitem.add("Sam-Default", "3X");
                singleitem.add("T-Mobile", "3X_T");
            }

            protected void onValueChanged(String s1)
            {
                if("3X_T".equals(s1))
                {
                	SystemProperties.set("persist.sys.boot_animation", "3X_T");
                    LogoJNI.setEnableBootAnimation(1);
                }
                else
                {
                	SystemProperties.set("persist.sys.boot_animation", "3X");
                    LogoJNI.setEnableBootAnimation(0);
                }
            }            

        };
        if(s.contains("BOOT"))
            linearlayout.addView(radiovalueitemview_boot.getView());


        //LANG
        RadioValueItemView radiovalueitemview1 = new RadioValueItemView(this, "persist.sys.language.switch", "full") {

            protected String getTitle()
            {
                return getString(R.string.instruct_value_language_title);
            }

            protected void onCreateSingleItem(RadioValueItemView.SingleItem singleitem)
            {
                singleitem.add(getString(R.string.instruct_value_language_other), "other");
                singleitem.add(getString(R.string.instruct_value_language_full), "full");
            }

        };
        if(s.contains("LANG"))
            linearlayout.addView(radiovalueitemview1.getView());

        //RAM
        RadioValueItemView radiovalueitemview2 = new RadioValueItemView(this, "persist.sys.set_ramsize", "0") {

            protected String getTitle()
            {
                return getString(R.string.instruct_value_ram_title);
            }

            protected void onCreateSingleItem(RadioValueItemView.SingleItem singleitem)
            {
                singleitem.add(getString(R.string.instruct_value_ram_default), "0");
                singleitem.add("1G", "1");
                singleitem.add("2G", "2");
                singleitem.add("3G", "3");
                singleitem.add("4G", "4");
            }

            protected void onValueChanged(String s1)
            {
                if("1".equals(s1))
                {
                    LogoJNI.setRamSize(1);
                    SystemProperties.set("persist.sys.set_ramsize", "1024");
                }
                else if("2".equals(s1))
                {
                    LogoJNI.setRamSize(2); 
                    SystemProperties.set("persist.sys.set_ramsize", "2048");
                }
                else if("3".equals(s1))
                {
                    LogoJNI.setRamSize(3); 
                    SystemProperties.set("persist.sys.set_ramsize", "3072");
                }
                else if("4".equals(s1))
                {
                    LogoJNI.setRamSize(4);
                    SystemProperties.set("persist.sys.set_ramsize", "4096");                
                }
                else
                {
                    LogoJNI.setRamSize(0); 
                    SystemProperties.set("persist.sys.set_ramsize", String.valueOf(Build.default_RamSize));//real grape_S6 fake
                }
            }
        };
        if(s.contains("RAM"))
            linearlayout.addView(radiovalueitemview2.getView());


        //ROM
        RadioValueItemView radiovalueitemview3 = new RadioValueItemView(this, "persist.sys.set_sdsize", "111") {

            protected String getTitle()
            {
                return getString(R.string.instruct_value_rom_title);
            }

            protected void onCreateSingleItem(RadioValueItemView.SingleItem singleitem)
            {
                singleitem.add(getString(R.string.instruct_value_rom_real), "111");
                singleitem.add("16G", "1600");
                singleitem.add("25.28G", "2528");
                singleitem.add("32G", "3200");
                singleitem.add("64G", "6400");
            }

            protected void onValueChanged(String s1)
            {
                if("800".equals(s1))
                {
                    LogoJNI.setSdSize(8);
                	SystemProperties.set("persist.sys.set_sdsize", "800");
                }
                else if("1600".equals(s1))
                {
                	LogoJNI.setSdSize(16);
                    SystemProperties.set("persist.sys.set_sdsize", "1600");
                }
                else if("2528".equals(s1))
                {
                	LogoJNI.setSdSize(25);
                    SystemProperties.set("persist.sys.set_sdsize", "2528");
                }                
                else if("3200".equals(s1))
                {
                	LogoJNI.setSdSize(32);
                    SystemProperties.set("persist.sys.set_sdsize", "3200");
                }
                else if("6400".equals(s1))
                {
                	LogoJNI.setSdSize(64);
                    SystemProperties.set("persist.sys.set_sdsize", "6400");
                }
                else if("12800".equals(s1))
                {
                	LogoJNI.setSdSize(128);
                    SystemProperties.set("persist.sys.set_sdsize", "12800");
                }
                else
                {
                    LogoJNI.setSdSize(111);
                    SystemProperties.set("persist.sys.set_sdsize", "111");//real grape_S6 fake
                }
            }
        };
        if(s.contains("ROM"))
            linearlayout.addView(radiovalueitemview3.getView());


        //SINGNAL
        RadioValueItemView radiovalueitemview4 = new RadioValueItemView(this, "persist.sys.operator", "0") {

            protected String getTitle()
            {
                return getString(R.string.instruct_value_signal_title);
            }

            protected void onCreateSingleItem(RadioValueItemView.SingleItem singleitem)
            {
                singleitem.add(getString(R.string.instruct_value_signal_normal), "0");
                singleitem.add(getString(R.string.instruct_value_signal_3g), "1");
                singleitem.add(getString(R.string.instruct_value_signal_4g), "2");
            }

	        protected void onValueChanged(String s1)
	        {
	            if("0".equals(s1))
	            {
	            	LogoJNI.setOperatorSig(0);
	            	SystemProperties.set("persist.sys.operator", "0");
                }
	            else if("1".equals(s1))
                {
                	LogoJNI.setOperatorSig(1);
	                SystemProperties.set("persist.sys.operator", "1");
                }
	            else if("2".equals(s1))
                {
                	LogoJNI.setOperatorSig(2);
	                SystemProperties.set("persist.sys.operator", "2");
                }
	            else
	            {
	            	LogoJNI.setOperatorSig(0);
	            	SystemProperties.set("persist.sys.operator", "0");//real grape_S6 fake
	            }
	        }

            

        };
        if(s.contains("SIGNAL"))
            linearlayout.addView(radiovalueitemview4.getView());


        //LTE
        RadioValueItemView radiovalueitemview_LTE = new RadioValueItemView(this, "persist.sys.4G_switch", "4G") {

            protected String getTitle()
            {
                return getString(R.string.instruct_value_4G_title);
            }

            protected void onCreateSingleItem(RadioValueItemView.SingleItem singleitem)
            {
                singleitem.add("4G", "4G");
                singleitem.add("LTE", "LTE");
            }

	        protected void onValueChanged(String s1)
	        {
	            if("LTE".equals(s1))
	            {
	            	SystemProperties.set("persist.sys.4G_switch", "LTE");
                }
	            else
	            {
	            	SystemProperties.set("persist.sys.4G_switch", "4G");//real grape_S6 fake
	            }
	        }

            

        };
        if(s.contains("LTE"))
            linearlayout.addView(radiovalueitemview_LTE.getView());


        //CPU
        RadioValueItemView radiovalueitemview5 = new RadioValueItemView(this, "persist.sys.cpuswitch", "0") {

            protected String getTitle()
            {
                return getString(R.string.instruct_value_cpu_title);
            }

            protected void onCreateSingleItem(RadioValueItemView.SingleItem singleitem)
            {
                singleitem.add(getString(R.string.instruct_value_cpu_8c), "0");
                singleitem.add(getString(R.string.instruct_value_cpu_4c), "1");
            }

	        protected void onValueChanged(String s1)
	        {
	            if("0".equals(s1))
	            {
	            	LogoJNI.setCpuType(0);
	            	SystemProperties.set("persist.sys.cpuswitch", "0");
                }
	            else
	            {
	            	LogoJNI.setCpuType(1);
	            	SystemProperties.set("persist.sys.cpuswitch", "1");
	            }
	        }

            

        };
        if(s.contains("CPU"))
            linearlayout.addView(radiovalueitemview5.getView());


        //FACTORY
        /*    linearlayout.addView((new SingleValueItemView(this, getString(R.string.instruct_value_factory_mode)) {

            protected String getButtonText()
            {
                return "*#0*#";
            }

            public void onClick(View view)
            {
                //Intent intent = new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://0"));
                //sendBroadcast(intent);
            }
           }
        ).getView());
        */

        
        linearlayout.addView((new SingleValueItemView(this, getString(R.string.instruct_value_imei)) {


        //IMEI
            protected String getButtonText()
            {
                return "IMEI";
            }

            public void onClick(View view)
            {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setFlags(0x10000000);
                intent.setClassName("com.mediatek.engineermode", "com.mediatek.engineermode.GPRS");
                startActivity(intent);
            }
        }
       ).getView());

       //SN
        linearlayout.addView((new SingleValueItemView(this, "Serial Name:"+ LogoJNI.getSearilNumber()) {
            protected String getButtonText()
            {
                return "Edit";
            }

            public void onClick(View view)
            {
                  final EditText inputEdit = new EditText(mContext);
                  inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
                  inputEdit.setFocusable(true);
                  inputEdit.requestFocus();

                  
                  AlertDialog.Builder builder = new AlertDialog.Builder(mContext);



                  
                  builder.setTitle("Serial Name").setView(inputEdit)
                          .setNegativeButton("Cancel", null);
                  builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                
                      public void onClick(DialogInterface dialog, int which) {
                         LogoJNI.setSearilNumber(inputEdit.getText().toString());
                         SystemProperties.set("persist.sys.serial_number", inputEdit.getText().toString());
                       }
                  });
                  AlertDialog dialog = builder.create();
                  dialog.show();

                  dialog.setInverseBackgroundForced(true);
                  //imm.showSoftInput(inputEdit, 0);
                  //imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
                  mHandler.postDelayed(new Runnable() {
                      @Override
                      public void run() {
                          imm.showSoftInput(inputEdit, 2);
                          //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                      }
                  }, 500);

                  

            }
        }
       ).getView());       
       
    }

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.default_value_activity_main);
        initSingleItem((LinearLayout)findViewById(R.id.default_value_activity_container));

        imm =(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    public void onDone(View view)
    {
        Toast.makeText(this, R.string.default_value_done_toast_message, 1).show();
        onBackPressed();
    }

    protected void onPause()
    {
        super.onPause();
        /*if("zh".equals(Locale.getDefault().getLanguage()) && "other".equals(SystemProperties.get("persist.sys.language.switch")))
        {
            ProgressDialog progressdialog = new ProgressDialog(this);
            progressdialog.setMessage(getString(0x7f090ad5));
            progressdialog.setCancelable(false);
            progressdialog.setCanceledOnTouchOutside(false);
            progressdialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {

                public void onShow(DialogInterface dialoginterface)
                {
                    (new Thread(dialoginterface. new Runnable() {

                        public void run()
                        {
                            LocalePicker.updateLocale(new Locale("en", "US"));
                            android.provider.Settings.Secure.putInt(getContentResolver(), "input_method_selector_visibility", 0);
                            dialog.dismiss();
                            finish();
                        }
                    }
                    )).start();
                }

            }
            );
            progressdialog.show();
        } 
        else*/
        {
            //Toast.makeText(this, R.string.default_value_done_toast_message, 1).show();
            //finish();
        }
    }
}
