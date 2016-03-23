package com.android.settings.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardTile;
import com.android.settings.R;

public class TwCategoryTileView extends FrameLayout
  implements View.OnClickListener
{
  private int mColSpan = 1;
  private ImageView mImageView;
  private DashboardTile mTile;
  private TextView mTitleTextView;

  public TwCategoryTileView(Context paramContext)
  {
    this(paramContext, null);
  }

  public TwCategoryTileView(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    View localView = LayoutInflater.from(paramContext).inflate(R.layout.tw_category_view, this);
    mImageView = ((ImageView)localView.findViewById(R.id.icon));
    mTitleTextView = ((TextView)localView.findViewById(R.id.title));
    setOnClickListener(this);
    setBackgroundResource(R.drawable.dashboard_tile_background);
    setFocusable(true);
  }

  int getColumnSpan()
  {
    return mColSpan;
  }

  public ImageView getImageView()
  {
    return mImageView;
  }

  public TextView getTitleTextView()
  {
    return mTitleTextView;
  }

  public void onClick(View paramView)
  {
    if (mTile.id == R.id.help_settings)
    {
      Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://downloadcenter.samsung.com/content/MC/201504/20150403192510053/C3/Sch/start_here.html"));
      getContext().startActivity(localIntent);
    }
    else 
    {
      if (mTile.fragment != null)
      {
        Utils.startWithFragment(getContext(), mTile.fragment, mTile.fragmentArguments, null, 0, mTile.titleRes, mTile.getTitle(getResources()));
      }
      else if (mTile.intent != null)
        getContext().startActivity(mTile.intent);
    }
  }

  public void setTile(DashboardTile paramDashboardTile)
  {
    mTile = paramDashboardTile;
  }
}