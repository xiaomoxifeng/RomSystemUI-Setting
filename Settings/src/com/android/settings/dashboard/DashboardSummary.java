/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.dashboard;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.ISettingsMiscExt;

import java.util.List;
import com.android.settings.view.TwCategoryTileView;

public class DashboardSummary extends Fragment {
    private static final String LOG_TAG = "DashboardSummary";
	private static final String CUSTOMIZE_ITEM_INDEX = "customize_item_index";
    private ISettingsMiscExt mExt;

    private LayoutInflater mLayoutInflater;
    private ViewGroup mDashboard;

    private static final int MSG_REBUILD_UI = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REBUILD_UI: {
                    final Context context = getActivity();
                    rebuildUI(context);
                } break;
            }
        }
    };

    private class HomePackageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            rebuildUI(context);
        }
    }
    private HomePackageReceiver mHomePackageReceiver = new HomePackageReceiver();

    @Override
    public void onResume() {
        super.onResume();

        sendRebuildUI();

        final IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        getActivity().registerReceiver(mHomePackageReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(mHomePackageReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mLayoutInflater = inflater;
        mExt = UtilsExt.getMiscPlugin(this.getActivity());

        final View rootView = inflater.inflate(R.layout.dashboard, container, false);
        mDashboard = (ViewGroup) rootView.findViewById(R.id.dashboard_container);

        return rootView;
    }

    private void rebuildUI(Context context) {
        if (!isAdded()) {
            Log.w(LOG_TAG, "Cannot build the DashboardSummary UI yet as the Fragment is not added");
            return;
        }

        long start = System.currentTimeMillis();
        final Resources res = getResources();

        mDashboard.removeAllViews();

        List<DashboardCategory> categories =
                ((SettingsActivity) context).getDashboardCategories(true);
        //grape_S6 settings
        DashboardCategory dashboardcategory = (DashboardCategory)((SettingsActivity)context).getTwShortCutCategories(true).get(0);
        View view = mLayoutInflater.inflate(R.layout.tw_category_container, mDashboard, false);
        ((TextView)view.findViewById(R.id.tw_category_title)).setText(dashboardcategory.getTitle(res));
        ViewGroup viewgroup = (ViewGroup)view.findViewById(R.id.tw_category_content);
        int nCount = dashboardcategory.getTilesCount();
        for (int j = 0; j < nCount ; j++) 
        {
            DashboardTile dashboardtile1 = dashboardcategory.getTile(j);
            TwCategoryTileView twcategorytileview = new TwCategoryTileView(context);
            ImageView imageview = twcategorytileview.getImageView();
            TextView textview = twcategorytileview.getTitleTextView();
            if(dashboardtile1.iconRes > 0)
            {
                imageview.setImageResource(dashboardtile1.iconRes);
            } else
            {
                imageview.setImageDrawable(null);
                imageview.setBackground(null);
            }
            textview.setText(dashboardtile1.getTitle(res));
            twcategorytileview.setTile(dashboardtile1);
            viewgroup.addView(twcategorytileview);
        }
        //mDashboard.addView(view);
        //end
        


        final int count = categories.size();

        for (int n = 0; n < count; n++) {
            DashboardCategory category = categories.get(n);

            View categoryView = mLayoutInflater.inflate(R.layout.dashboard_category, mDashboard,
                    false);

            TextView categoryLabel = (TextView) categoryView.findViewById(R.id.category_title);
            categoryLabel.setText(category.getTitle(res));

            ViewGroup categoryContent =
                    (ViewGroup) categoryView.findViewById(R.id.category_content);

            final int tilesCount = category.getTilesCount();
            for (int i = 0; i < tilesCount; i++) {
                DashboardTile tile = category.getTile(i);

                DashboardTileView tileView = new DashboardTileView(context);
                updateTileView(context, res, tile, tileView.getImageView(),
                        tileView.getTitleTextView(), tileView.getStatusTextView());

                tileView.setTile(tile);
                if(tile != null && tile.extras != null && tile.extras.containsKey(CUSTOMIZE_ITEM_INDEX)){
                    int index = tile.extras.getInt(CUSTOMIZE_ITEM_INDEX, -1);
                    categoryContent.addView(tileView, index);
                } else {
                    categoryContent.addView(tileView);
                }
            }

            // Add the category
            mDashboard.addView(categoryView);
        }
        long delta = System.currentTimeMillis() - start;
        Log.d(LOG_TAG, "rebuildUI took: " + delta + " ms");
    }

    private void updateTileView(Context context, Resources res, DashboardTile tile,
            ImageView tileIcon, TextView tileTextView, TextView statusTextView) {

        if (tile.iconRes > 0) {
            tileIcon.setImageResource(tile.iconRes);
        } else {
            //TODO:: Ask HIll to re-do this part
            mExt.customizeDashboardTile(tile, null, null, tileIcon, 2);
        }

        ///M: feature replace sim to uim
        tileTextView.setText(mExt.customizeSimDisplayString(
            tile.getTitle(res).toString(), SubscriptionManager.INVALID_SUBSCRIPTION_ID));

        CharSequence summary = tile.getSummary(res);
        if (!TextUtils.isEmpty(summary)) {
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText(summary);
        } else {
            statusTextView.setVisibility(View.GONE);
        }
    }

    private void sendRebuildUI() {
        if (!mHandler.hasMessages(MSG_REBUILD_UI)) {
            mHandler.sendEmptyMessage(MSG_REBUILD_UI);
        }
    }
}
