
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.android.settings.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.android.settings.R;

// Referenced classes of package com.android.settings.view:
//            TwCategoryTileView

public class TwCategoryContainerView extends ViewGroup
{

    private float mCellGapX;
    private float mCellGapY;
    private int mNumColumns;
    private int mNumRows;
    private float mPaddingBottom;

    public TwCategoryContainerView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        Resources resources = context.getResources();
        mCellGapX = resources.getDimension(R.dimen.dashboard_cell_gap_x);
        mCellGapY = resources.getDimension(R.dimen.dashboard_cell_gap_y);
        mNumColumns = resources.getInteger(R.integer.tw_container_num_columns);
        mPaddingBottom = resources.getDimension(R.dimen.tw_quick_container_padding_bottom);
    }

    protected void onLayout(boolean flag, int i, int j, int k, int l)
    {
        int i1 = getChildCount();
        boolean flag1 = isLayoutRtl();
        int j1 = getWidth();
        int k1 = getPaddingStart();
        int l1 = getPaddingTop();
        int i2 = 0;
        int j2 = 0;
        while(j2 < i1) 
        {
            TwCategoryTileView twcategorytileview = (TwCategoryTileView)getChildAt(j2);
            android.view.ViewGroup.LayoutParams layoutparams = twcategorytileview.getLayoutParams();
            if(twcategorytileview.getVisibility() != 8)
            {
                int k2 = i2 % mNumColumns;
                int l2 = twcategorytileview.getColumnSpan();
                int i3 = layoutparams.width;
                int j3 = layoutparams.height;
                int k3 = i2 / mNumColumns;
                if(k2 + l2 > mNumColumns)
                {
                    k1 = getPaddingStart();
                    l1 = (int)((float)l1 + ((float)j3 + mCellGapY));
                    k3++;
                }
                int l3;
                int i4;
                int j4;
                if(flag1)
                    l3 = j1 - k1 - i3;
                else
                    l3 = k1;
                i4 = l3 + i3;
                j4 = l1;
                twcategorytileview.layout(l3, j4, i4, j4 + j3);
                i2 += twcategorytileview.getColumnSpan();
                if(i2 < (k3 + 1) * mNumColumns)
                {
                    k1 = (int)((float)k1 + ((float)i3 + mCellGapX));
                } else
                {
                    k1 = getPaddingStart();
                    l1 = (int)((float)l1 + ((float)j3 + mCellGapY));
                }
            }
            j2++;
        }
    }

    protected void onMeasure(int i, int j)
    {
        int k = android.view.View.MeasureSpec.getSize(i);
        float f = (float)Math.ceil((float)(int)((float)(k - getPaddingLeft() - getPaddingRight()) - (float)(-1 + mNumColumns) * mCellGapX) / (float)mNumColumns);
        int l = getChildCount();
        int i1 = 0;
        int j1 = 0;
        int k1 = 0;
        while(k1 < l) 
        {
            TwCategoryTileView twcategorytileview = (TwCategoryTileView)getChildAt(k1);
            if(twcategorytileview.getVisibility() != 8)
            {
                android.view.ViewGroup.LayoutParams layoutparams = twcategorytileview.getLayoutParams();
                int l1 = twcategorytileview.getColumnSpan();
                layoutparams.width = (int)(f * (float)l1 + (float)(l1 - 1) * mCellGapX);
                twcategorytileview.measure(getChildMeasureSpec(i, 0, layoutparams.width), getChildMeasureSpec(j, 0, layoutparams.height));
                if(i1 <= 0)
                    i1 = twcategorytileview.getMeasuredHeight();
                layoutparams.height = i1;
                j1 += l1;
            }
            k1++;
        }
        mNumRows = (int)Math.ceil((float)j1 / (float)mNumColumns);
        setMeasuredDimension(k, (int)((float)(i1 * mNumRows) + (float)(-1 + mNumRows) * mCellGapY) + getPaddingTop() + getPaddingBottom() + (int)mPaddingBottom);
    }

}

