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

package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.settings.BrightnessController;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;
import java.util.Collection;
import com.android.systemui.TwConfigureView;
import com.android.systemui.statusbar.phone.ActivityStarter;
import android.content.ComponentName;
import android.os.Debug;

/** View that represents the quick settings tile panel. **/
public class QSPanel extends ViewGroup {
    /// M: For Debug
    private static final String TAG = "QSPanel";
    private static final boolean DEBUG = true;

    private static final float TILE_ASPECT = 1.2f;

    private final Context mContext;
    private final ArrayList<TileRecord> mRecords = new ArrayList<TileRecord>();
    private final View mDetail;
    private final ViewGroup mDetailContent;
    private final TextView mDetailSettingsButton;
    private final TextView mDetailDoneButton;
    private final View mBrightnessView;
    private final QSDetailClipper mClipper;
    private final H mHandler = new H();

    private int mColumns;
    private int mCellWidth;
    private int mCellHeight;
    private int mLargeCellWidth;
    private int mLargeCellHeight;
    private int mPanelPaddingBottom;
    private int mDualTileUnderlap;
    private int mBrightnessPaddingTop;
    private int mGridHeight;
    private boolean mExpanded;
    private boolean mListening;
    private boolean mClosingDetail;

    private Record mDetailRecord;
    private Callback mCallback;
    private BrightnessController mBrightnessController;
    private QSTileHost mHost;

    private QSFooter mFooter;
    private boolean mGridContentVisible = true;
//grape_S6
    private final View mQuickSettingSroll;
	private final TwConfigureView mTwConfigureContainer;
	private final TextView mTwConnectView;
	private final View mTwSfinderContent;
	private final TextView mTwSfinderView;
    private ActivityStarter mActivityStarter;
    private int mQuickSettingsHeight;
    private int mSfinderContentHeight;    
//end

    public QSPanel(Context context) {
        this(context, null);
    }

    public QSPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mDetail = LayoutInflater.from(context).inflate(R.layout.qs_detail, this, false);
        mDetailContent = (ViewGroup) mDetail.findViewById(android.R.id.content);
        mDetailSettingsButton = (TextView) mDetail.findViewById(android.R.id.button2);
        mDetailDoneButton = (TextView) mDetail.findViewById(android.R.id.button1);
        updateDetailText();
        mDetail.setVisibility(GONE);
        mDetail.setClickable(true);
        mBrightnessView = LayoutInflater.from(context).inflate(
                R.layout.quick_settings_brightness_dialog, this, false);
//grape_S6
		mQuickSettingSroll = LayoutInflater.from(context).inflate(R.layout.tw_quick_settings_scroll, this, false);
		mTwConfigureContainer = (TwConfigureView)mQuickSettingSroll.findViewById(R.id.tw_configure_container);
		mTwSfinderContent = LayoutInflater.from(context).inflate(R.layout.tw_sfinder_quick_connect_panel, this, false);
		mTwSfinderView = (TextView)mTwSfinderContent.findViewById(R.id.tw_sfinder_view);
		mTwConnectView = (TextView)mTwSfinderContent.findViewById(R.id.tw_quick_connect_view);
	    mTwSfinderView.setOnClickListener(new View.OnClickListener()
	    {
	      public void onClick(View paramView)
	      {
	        try
	        {
	          Intent localIntent = new Intent();
	          localIntent.setFlags(0x10000000);
	          localIntent.setComponent(new ComponentName("com.android.quicksearchbox", "com.android.quicksearchbox.SearchActivity"));
	          mActivityStarter.startActivity(localIntent, true);
	          return;
	        }
	        catch (Exception localException)
	        {
	            localException.printStackTrace();
	        }
	      }
	    });
	    mTwConnectView.setOnClickListener(new View.OnClickListener()
	    {
	      public void onClick(View paramView)
	      {
	        Intent localIntent = new Intent();
	        localIntent.setFlags(0x10000000);
	        localIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity"));
	        mActivityStarter.startActivity(localIntent, true);
	      }
	    });        
//end

        
        mFooter = new QSFooter(this, context);
        addView(mDetail);
        addView(mQuickSettingSroll);//grape_S6
        addView(mBrightnessView);
        addView(mTwSfinderContent);//grape_S6
        addView(mFooter.getView());
        mClipper = new QSDetailClipper(mDetail);
        updateResources();

        mBrightnessController = new BrightnessController(getContext(),
                (ImageView) findViewById(R.id.brightness_icon),
                (ToggleSlider) findViewById(R.id.brightness_slider));

        mDetailDoneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDetail();
            }
        });
    }

//grape_S6
    public void setActivityStarter(ActivityStarter activitystarter)
    {
        mActivityStarter = activitystarter;
    }  
//end

    private void updateDetailText() {
        mDetailDoneButton.setText(R.string.quick_settings_done);
        mDetailSettingsButton.setText(R.string.quick_settings_more_settings);
//grape_S6
		if(mTwSfinderView != null)
		    mTwSfinderView.setText(R.string.tw_status_s_finder);
		if(mTwConnectView != null)
		    mTwConnectView.setText(R.string.tw_status_quick_connect);
//end
    }

    public void setBrightnessMirror(BrightnessMirrorController c) {
        super.onFinishInflate();
        ToggleSlider brightnessSlider = (ToggleSlider) findViewById(R.id.brightness_slider);
        ToggleSlider mirror = (ToggleSlider) c.getMirror().findViewById(R.id.brightness_slider);
        brightnessSlider.setMirror(mirror);
        brightnessSlider.setMirrorController(c);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setHost(QSTileHost host) {
        mHost = host;
        mFooter.setHost(host);
    }

    public QSTileHost getHost() {
        return mHost;
    }

    public void updateResources() {
        final Resources res = mContext.getResources();
        final int columns = Math.max(1, res.getInteger(R.integer.quick_settings_num_columns));
        mCellHeight = res.getDimensionPixelSize(R.dimen.qs_tile_height);
        mCellWidth = (int)(mCellHeight * TILE_ASPECT);
        mLargeCellHeight = res.getDimensionPixelSize(R.dimen.qs_dual_tile_height);
        mLargeCellWidth = (int)(mLargeCellHeight * TILE_ASPECT);
        mPanelPaddingBottom = res.getDimensionPixelSize(R.dimen.qs_panel_padding_bottom);
        mDualTileUnderlap = res.getDimensionPixelSize(R.dimen.qs_dual_tile_padding_vertical);
        mBrightnessPaddingTop = res.getDimensionPixelSize(R.dimen.tw_qs_brightness_padding_top); //grape_S6
        if (mColumns != columns) {
            mColumns = columns;
            postInvalidate();
        }
        if (mListening) {
            refreshAllTiles();
        }
        updateDetailText();
        mQuickSettingsHeight = (int)res.getDimension(R.dimen.tw_quick_settings_cell_height);//grape_S6
        mSfinderContentHeight = (int)res.getDimension(R.dimen.tw_sfinder_connect_panel_height);        
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FontSizeUtils.updateFontSize(mDetailDoneButton, R.dimen.qs_detail_button_text_size);
        FontSizeUtils.updateFontSize(mDetailSettingsButton, R.dimen.qs_detail_button_text_size);
//grape
        ToggleSlider slider = (ToggleSlider) findViewById(R.id.brightness_slider);
        slider.updateLabel();
//end
//grape_S6
        if(mTwSfinderView != null)
            mTwSfinderView.setText(R.string.tw_status_s_finder);
        if(mTwConnectView != null)
            mTwConnectView.setText(R.string.tw_status_quick_connect);
//end

        // We need to poke the detail views as well as they might not be attached to the view
        // hierarchy but reused at a later point.
        int count = mRecords.size();
        for (int i = 0; i < count; i++) {
            View detailView = mRecords.get(i).detailView;
            if (detailView != null) {
                detailView.dispatchConfigurationChanged(newConfig);
            }
        }
        mFooter.onConfigurationChanged();
    }

    public void setExpanded(boolean expanded) {
        if (DEBUG) {
            Xlog.d(TAG, "setExpanded: expanded: " + expanded + ", caller: " + Debug.getCallers(12));
        }


        if (mExpanded == expanded) return;
        mExpanded = expanded;
        if (!mExpanded) {
            closeDetail();
        }
    }

    public void setListening(boolean listening) {
        if (DEBUG) {
            Xlog.d(TAG, "setListening: old: " + mListening + ", new: " + listening);
        }

        if (mListening == listening) return;
        mListening = listening;
        for (TileRecord r : mRecords) {
            r.tile.setListening(mListening);
        }
        mFooter.setListening(mListening);
        if (mListening) {
            refreshAllTiles();
        }
        if (listening) {
            mBrightnessController.registerCallbacks();
        } else {
            mBrightnessController.unregisterCallbacks();
        }
    }

    public void refreshAllTiles() {
        for (TileRecord r : mRecords) {
            r.tile.refreshState();
        }
        mFooter.refreshState();
    }

    public void showDetailAdapter(boolean show, DetailAdapter adapter) {
        Record r = new Record();
        r.detailAdapter = adapter;
        showDetail(show, r);
    }

    private void showDetail(boolean show, Record r) {
        mHandler.obtainMessage(H.SHOW_DETAIL, show ? 1 : 0, 0, r).sendToTarget();
    }

    private void setTileVisibility(View v, int visibility) {
        mHandler.obtainMessage(H.SET_TILE_VISIBILITY, visibility, 0, v).sendToTarget();
    }

    private void handleSetTileVisibility(View v, int visibility) {
        if (visibility == v.getVisibility()) return;
        v.setVisibility(visibility);

        if (DEBUG) {
            Xlog.d(TAG, "handleSetTileVisibility END: " + visibility + ", " + mGridContentVisible);
        }
    }

    public void setTiles(Collection<QSTile<?>> tiles) {
        for (TileRecord record : mRecords) {
            if (DEBUG) {
                Xlog.d(TAG, "setTiles: removeView: " + record.tile.toString());
            }

            mTwConfigureContainer.removeView(record.tileView); //grape_S6
        }
        mRecords.clear();
        for (QSTile<?> tile : tiles) {
            addTile(tile);
        }
        if (isShowingDetail()) {
            mDetail.bringToFront();
        }
    }

    private void addTile(final QSTile<?> tile) {
        final TileRecord r = new TileRecord();
        r.tile = tile;
        r.tileView = tile.createTileView(mContext);
        r.tileView.setVisibility(View.GONE);
        final QSTile.Callback callback = new QSTile.Callback() {
            @Override
            public void onStateChanged(QSTile.State state) {
                int visibility = state.visible ? VISIBLE : GONE;

                /// M: Fix timing issue of setting tile visibility @{
                if (isShowingDetail() || isClosingDetail()) {
                     if (DEBUG) {
                         Xlog.d(TAG, "onStateChanged: SKIP " + r.tile.toString() +
                             ", v: " + visibility +
                             ", gv: " + mGridContentVisible);
                     }
                     r.tileView.onStateChanged(state);
                     return;
                }
                /// M: Fix timing issue of setting tile visibility @}

                if (state.visible && !mGridContentVisible) {

                    // We don't want to show it if the content is hidden,
                    // then we just set it to invisible, to ensure that it gets visible again
                    visibility = INVISIBLE; //grape_attention
                }

                if (DEBUG) {
                    Xlog.d(TAG, "onStateChanged: " + r.tile.toString() +
                        ", v: " + visibility +
                        ", gv: " + mGridContentVisible);
                }
                setTileVisibility(r.tileView, visibility);
                r.tileView.onStateChanged(state);
            }
            @Override
            public void onShowDetail(boolean show) {
                QSPanel.this.showDetail(show, r);
            }
            @Override
            public void onToggleStateChanged(boolean state) {
                if (mDetailRecord == r) {
                    fireToggleStateChanged(state);
                }
            }
            @Override
            public void onScanStateChanged(boolean state) {
                r.scanState = state;
                if (mDetailRecord == r) {
                    fireScanStateChanged(r.scanState);
                }
            }

            @Override
            public void onAnnouncementRequested(CharSequence announcement) {
                announceForAccessibility(announcement);
            }
        };
        r.tile.setCallback(callback);
        final View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r.tile.click();
            }
        };
        final View.OnClickListener clickSecondary = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r.tile.secondaryClick();
            }
        };
        final View.OnLongClickListener longClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                r.tile.longClick();
                return true;
            }
        };
        r.tileView.init(click, clickSecondary, longClick);
        r.tile.setListening(mListening);

        // M: Fix timing issue of changing visibility with different threads
        // refreshState() of QSTile will invoke onStateChanged() with Handler thread
        // callback.onStateChanged(r.tile.getState());

        r.tile.refreshState();
        mRecords.add(r);

        if (DEBUG) {
            Xlog.d(TAG, "addTile: addView: " + r.tile.toString());
        }

        mTwConfigureContainer.addView(r.tileView); //grape_S6
    }

    public boolean isShowingDetail() {
        return mDetailRecord != null;
    }

    public void closeDetail() {
        showDetail(false, mDetailRecord);
    }

    public boolean isClosingDetail() {
        return mClosingDetail;
    }

    public int getGridHeight() {
        return mGridHeight;
    }

    private void handleShowDetail(Record r, boolean show) {
        if (r instanceof TileRecord) {
            handleShowDetailTile((TileRecord) r, show);
        } else {
            handleShowDetailImpl(r, show, getWidth() /* x */, 0/* y */);
        }
    }

    private void handleShowDetailTile(TileRecord r, boolean show) {
        if ((mDetailRecord != null) == show) return;

        if (show) {
            r.detailAdapter = r.tile.getDetailAdapter();
            if (r.detailAdapter == null) return;
        }
        int x = r.tileView.getLeft() + r.tileView.getWidth() / 2;
        int y = r.tileView.getTop() + r.tileView.getHeight() / 2;
        handleShowDetailImpl(r, show, x, y);
    }

    private void handleShowDetailImpl(Record r, boolean show, int x, int y) {
        if ((mDetailRecord != null) == show) return;  // already in right state
        DetailAdapter detailAdapter = null;
        AnimatorListener listener = null;
        if (show) {
            detailAdapter = r.detailAdapter;
            r.detailView = detailAdapter.createDetailView(mContext, r.detailView, mDetailContent);
            if (r.detailView == null) throw new IllegalStateException("Must return detail view");

            final Intent settingsIntent = detailAdapter.getSettingsIntent();
            mDetailSettingsButton.setVisibility(settingsIntent != null ? VISIBLE : GONE);
            mDetailSettingsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHost.startSettingsActivity(settingsIntent);
                }
            });

            mDetailContent.removeAllViews();
            mDetail.bringToFront();
            mDetailContent.addView(r.detailView);
            setDetailRecord(r);
            listener = mHideGridContentWhenDone;
        } else {
            mClosingDetail = true;

            if (DEBUG) {
                Xlog.d(TAG, "It's ClosingDetail");
            }

            setGridContentVisibility(true);
            listener = mTeardownDetailWhenDone;
            fireScanStateChanged(false);
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        fireShowingDetail(show ? detailAdapter : null);
        mClipper.animateCircularClip(x, y, show, listener);
    }

    private void setGridContentVisibility(boolean visible) {
        int newVis = visible ? VISIBLE : INVISIBLE;
        for (int i = 0; i < mRecords.size(); i++) {
            TileRecord tileRecord = mRecords.get(i);
            if (tileRecord.tileView.getVisibility() != GONE) {
                tileRecord.tileView.setVisibility(newVis);
            }
        }
        mBrightnessView.setVisibility(newVis);
        mGridContentVisible = visible;

        if (DEBUG) {
            Xlog.d(TAG, "setGridContentVisibility END: " + visible);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        mBrightnessView.measure(exactly(width), MeasureSpec.UNSPECIFIED);        
//grape_S6
        mTwConfigureContainer.measure(exactly(width), 0);
        mTwSfinderContent.measure(exactly(width), 0);
//end
        
        final int brightnessHeight = mBrightnessView.getMeasuredHeight() + mBrightnessPaddingTop;
        mFooter.getView().measure(exactly(width), MeasureSpec.UNSPECIFIED);
        /* //grape_S6
        int r = -1;
        int c = -1;
        int rows = 0;
        boolean rowIsDual = false;
        for (TileRecord record : mRecords) {
            if (record.tileView.getVisibility() == GONE) continue;
            // wrap to next column if we've reached the max # of columns
            // also don't allow dual + single tiles on the same row
            if (r == -1 || c == (mColumns - 1) || rowIsDual != record.tile.supportsDualTargets()) {
                r++;
                c = 0;
                rowIsDual = record.tile.supportsDualTargets();
            } else {
                c++;
            }
            record.row = r;
            record.col = c;
            rows = r + 1;
        }

        for (TileRecord record : mRecords) {
            if (record.tileView.setDual(record.tile.supportsDualTargets())) {
                record.tileView.handleStateChanged(record.tile.getState());
            }
            if (record.tileView.getVisibility() == GONE) continue;
            final int cw = record.row == 0 ? mLargeCellWidth : mCellWidth;
            final int ch = record.row == 0 ? mLargeCellHeight : mCellHeight;
            record.tileView.measure(exactly(cw), exactly(ch));
        }
        int h = rows == 0 ? brightnessHeight : (getRowTop(rows) + mPanelPaddingBottom);
        */
//grape_S6        
        for (TileRecord record : mRecords)
        {
        	record.tileView.setDual(record.tile.supportsDualTargets());
            record.tileView.handleStateChanged(record.tile.getState());
        }
        /*TileRecord tilerecord;
        for(Iterator iterator = mRecords.iterator(); iterator.hasNext(); tilerecord.tileView.setDual(tilerecord.tile.supportsDualTargets()))
            tilerecord = (TileRecord)iterator.next();*/
        
        int h = brightnessHeight + mQuickSettingsHeight + mSfinderContentHeight;
//end

        
        if (mFooter.hasFooter()) {
            h += mFooter.getView().getMeasuredHeight();
        }
        mDetail.measure(exactly(width), MeasureSpec.UNSPECIFIED);
        if (mDetail.getMeasuredHeight() < h) {
            mDetail.measure(exactly(width), exactly(h));
        }
        mGridHeight = h;
        setMeasuredDimension(width, Math.max(h, mDetail.getMeasuredHeight()));
    }

    private static int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int w = getWidth();
        mQuickSettingSroll.layout(0, 0, w, mQuickSettingsHeight); //grape_S6
        mTwSfinderContent.layout(0, mBrightnessView.getMeasuredHeight() + mQuickSettingsHeight, w, mBrightnessView.getMeasuredHeight() + mQuickSettingsHeight + mSfinderContentHeight);//grape_S6
        mBrightnessView.layout(0, mBrightnessPaddingTop + mQuickSettingsHeight,//grape_S6
                mBrightnessView.getMeasuredWidth(),
                mBrightnessPaddingTop + mQuickSettingsHeight+ + mBrightnessView.getMeasuredHeight());
        boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        /*for (TileRecord record : mRecords) {   //grape_S6
            if (record.tileView.getVisibility() == GONE) continue;
            final int cols = getColumnCount(record.row);
            final int cw = record.row == 0 ? mLargeCellWidth : mCellWidth;
            final int extra = (w - cw * cols) / (cols + 1);
            int left = record.col * cw + (record.col + 1) * extra;
            final int top = getRowTop(record.row);
            int right;
            int tileWith = record.tileView.getMeasuredWidth();
            if (isRtl) {
                right = w - left;
                left = right - tileWith;
            } else {
                right = left + tileWith;
            }
            record.tileView.layout(left, top, right, top + record.tileView.getMeasuredHeight());
        }
        */
        final int dh = Math.max(mDetail.getMeasuredHeight(), getMeasuredHeight());
        mDetail.layout(0, 0, mDetail.getMeasuredWidth(), dh);
        if (mFooter.hasFooter()) {
            View footer = mFooter.getView();
            footer.layout(0, getMeasuredHeight() - footer.getMeasuredHeight(),
                    footer.getMeasuredWidth(), getMeasuredHeight());
        }
    }

    private int getRowTop(int row) {
        if (row <= 0) return mBrightnessView.getMeasuredHeight() + mBrightnessPaddingTop;
        return mBrightnessView.getMeasuredHeight() + mBrightnessPaddingTop
                + mLargeCellHeight - mDualTileUnderlap + (row - 1) * mCellHeight;
    }

    private int getColumnCount(int row) {
        int cols = 0;
        for (TileRecord record : mRecords) {
            if (record.tileView.getVisibility() == GONE) continue;
            if (record.row == row) cols++;
        }
        return cols;
    }

    private void fireShowingDetail(QSTile.DetailAdapter detail) {
        if (mCallback != null) {
            mCallback.onShowingDetail(detail);
        }
    }

    private void fireToggleStateChanged(boolean state) {
        if (mCallback != null) {
            mCallback.onToggleStateChanged(state);
        }
    }

    private void fireScanStateChanged(boolean state) {
        if (mCallback != null) {
            mCallback.onScanStateChanged(state);
        }
    }

    private void setDetailRecord(Record r) {
        if (r == mDetailRecord) return;
        mDetailRecord = r;

        if (DEBUG) {
            if (mDetailRecord == null) {
                Xlog.d(TAG, "It's NOT ShowingDetail");
            } else {
                Xlog.d(TAG, "It's ShowingDetail");
            }
        }

        final boolean scanState = mDetailRecord instanceof TileRecord
                && ((TileRecord) mDetailRecord).scanState;
        fireScanStateChanged(scanState);
    }

    private class H extends Handler {
        private static final int SHOW_DETAIL = 1;
        private static final int SET_TILE_VISIBILITY = 2;
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SHOW_DETAIL) {
                handleShowDetail((Record)msg.obj, msg.arg1 != 0);
            } else if (msg.what == SET_TILE_VISIBILITY) {
                handleSetTileVisibility((View)msg.obj, msg.arg1);
            }
        }
    }

    private static class Record {
        View detailView;
        DetailAdapter detailAdapter;
    }

    private static final class TileRecord extends Record {
        QSTile<?> tile;
        QSTileView tileView;
        int row;
        int col;
        boolean scanState;
    }

    private final AnimatorListenerAdapter mTeardownDetailWhenDone = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            mDetailContent.removeAllViews();
            setDetailRecord(null);
            mClosingDetail = false;

            if (DEBUG) {
                Xlog.d(TAG, "It's NOT ClosingDetail");
            }
        };
    };

    private final AnimatorListenerAdapter mHideGridContentWhenDone = new AnimatorListenerAdapter() {
        public void onAnimationCancel(Animator animation) {
            // If we have been cancelled, remove the listener so that onAnimationEnd doesn't get
            // called, this will avoid accidentally turning off the grid when we don't want to.
            animation.removeListener(this);
        };

        @Override
        public void onAnimationEnd(Animator animation) {
            // Only hide content if still in detail state.
            if (mDetailRecord != null) {
                //setGridContentVisibility(false); //grape_sttention
            }
        }
    };

    public interface Callback {
        void onShowingDetail(QSTile.DetailAdapter detail);
        void onToggleStateChanged(boolean state);
        void onScanStateChanged(boolean state);
    }
}
