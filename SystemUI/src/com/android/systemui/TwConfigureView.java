// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.android.systemui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.*;
import android.widget.LinearLayout;
import com.android.systemui.R;

public class TwConfigureView extends LinearLayout
{
    private int cellHeight;
    private int cellWidth;
    private int mCellGap;
    private Context mContext;
    private int mNumColumns;
    private float scale;

    public TwConfigureView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        scale = 1.0F;
        mContext = context;
        updateResources();
    }

    protected void dispatchDraw(Canvas canvas)
    {
        getChildCount();
        getTop();
        super.dispatchDraw(canvas);
    }

    protected void onFinishInflate()
    {
        super.onFinishInflate();
    }

    protected void onLayout(boolean flag, int i, int j, int k, int l)
    {
        int i1 = getPaddingLeft();
        int j1 = getPaddingTop();
        int k1 = getChildCount();
        for(int l1 = 0; l1 < k1; l1++)
        {
            View view = getChildAt(l1);
            android.view.ViewGroup.LayoutParams layoutparams = view.getLayoutParams();
            view.layout(i1, j1, i1 + cellWidth, j1 + layoutparams.height);
            i1 += cellWidth + mCellGap;
        }

    }

    protected void onMeasure(int i, int j)
    {
        android.view.View.MeasureSpec.getSize(i);
        android.view.View.MeasureSpec.getSize(j);
        int k = 0;
        int l = getChildCount();
        for(int i1 = 0; i1 < l; i1++)
        {
            View view = getChildAt(i1);
            if(view.getVisibility() != 8)
            {
                android.view.ViewGroup.MarginLayoutParams marginlayoutparams = (android.view.ViewGroup.MarginLayoutParams)view.getLayoutParams();
                marginlayoutparams.width = cellWidth;
                marginlayoutparams.height = cellHeight;
                k += cellWidth + mCellGap;
                view.measure(android.view.View.MeasureSpec.makeMeasureSpec(marginlayoutparams.width, 0x40000000), android.view.View.MeasureSpec.makeMeasureSpec(marginlayoutparams.height, 0x40000000));
            }
        }

        setMeasuredDimension(k, cellHeight);
    }

    void updateResources()
    {
        Resources resources = getContext().getResources();
        mCellGap = (int)resources.getDimension(R.dimen.tw_quick_settings_cell_gap);
        mNumColumns = resources.getInteger(R.integer.tw_quick_settings_num_columns);
        cellHeight = (int)resources.getDimension(R.dimen.tw_quick_settings_cell_height);
        int i = ((WindowManager)mContext.getSystemService("window")).getDefaultDisplay().getWidth();
        try
        {
            cellWidth = (i - mCellGap * (-1 + mNumColumns)) / mNumColumns;
        }
        catch(Exception exception)
        {
            cellWidth = cellHeight;
        }
        requestLayout();
    }


}
