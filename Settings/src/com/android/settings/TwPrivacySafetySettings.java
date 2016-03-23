// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.android.settings;

import android.os.Bundle;
import com.android.settings.R;
// Referenced classes of package com.android.settings:
//            SettingsPreferenceFragment

public class TwPrivacySafetySettings extends SettingsPreferenceFragment
{

    public TwPrivacySafetySettings()
    {
    }

    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.tw_privacy_safety_settings);
    }
}
