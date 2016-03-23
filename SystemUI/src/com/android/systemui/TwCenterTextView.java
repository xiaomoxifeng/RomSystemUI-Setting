// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.android.systemui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class TwCenterTextView extends TextView
{
    private Drawable drawableLeft;
    private int drawablePadding;

    public TwCenterTextView(Context context)
    {
        super(context);
        drawableLeft = null;
        drawablePadding = 0;
    }

    public TwCenterTextView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        drawableLeft = null;
        drawablePadding = 0;
    }

    public TwCenterTextView(Context context, AttributeSet attributeset, int i)
    {
        super(context, attributeset, i);
        drawableLeft = null;
        drawablePadding = 0;
    }

    protected void onDraw(Canvas canvas)
    {
        Drawable adrawable[] = getCompoundDrawables();
        int i = 0;
        if(adrawable != null && adrawable[0] != null)
        {
            drawableLeft = adrawable[0];
            drawablePadding = getCompoundDrawablePadding();
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        int j = mBottom - mTop;
        if(drawableLeft != null)
        {
            int ai[] = getDrawableState();
            drawableLeft.setState(ai);
            canvas.save();
            float f = getPaint().measureText(getText().toString());
            i = drawableLeft.getIntrinsicWidth();
            float f1 = f + (float)i + (float)drawablePadding;
            canvas.translate(((float)getWidth() - f1) / 2.0F, (j - drawableLeft.getIntrinsicHeight()) / 2);
            drawableLeft.draw(canvas);
            canvas.restore();
        }
        canvas.save();
        canvas.translate((i + drawablePadding) / 2, 0.0F);
        super.onDraw(canvas);
        canvas.restore();
    }


}
