// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.android.settings.instruct;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;


public abstract class SingleValueItemView
    implements android.view.View.OnClickListener
{

    public SingleValueItemView(Context context, String s)
    {
        mView = LayoutInflater.from(context).inflate(R.layout.default_single_value_item_view, null);
        Button button = (Button)mView.findViewById(R.id.default_single_button);
        button.setOnClickListener(this);
        button.setText(getButtonText());
        ((TextView)mView.findViewById(R.id.default_single_text_view)).setText(s);
        if (null == s)
           ((TextView)mView.findViewById(R.id.default_single_text_view)).setVisibility(View.GONE);
    }

    protected abstract String getButtonText();

    public View getView()
    {
        return mView;
    }

    public void setVisibility(int i)
    {
        if(mView != null)
            mView.setVisibility(i);
    }

    private View mView;
}
