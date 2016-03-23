// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.android.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import com.android.settings.search.Indexable;
import com.android.settings.R;
// Referenced classes of package com.android.settings:
//            SettingsPreferenceFragment, AirplaneModeEnabler

public class TwFlightModeSettings extends SettingsPreferenceFragment
    implements android.preference.Preference.OnPreferenceChangeListener, Indexable
{

    public TwFlightModeSettings()
    {
    }

    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.tw_flightmode_settings);
        android.app.Activity activity = getActivity();
        mAirplaneModePreference = (SwitchPreference)findPreference("toggle_airplane");
        mAirplaneModeEnabler = new AirplaneModeEnabler(activity, mAirplaneModePreference);
    }

    public void onDestroy()
    {
        super.onDestroy();
        mAirplaneModeEnabler.destroy();
    }

    public void onPause()
    {
        super.onPause();
        mAirplaneModeEnabler.pause();
    }

    public boolean onPreferenceChange(Preference preference, Object obj)
    {
        return false;
    }

    public void onResume()
    {
        super.onResume();
        mAirplaneModeEnabler.resume();
    }

    private AirplaneModeEnabler mAirplaneModeEnabler;
    private SwitchPreference mAirplaneModePreference;
}
