package com.lewa.systemuiext.widgets;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.lewa.systemuiext.switchwidget.SwitchWidget;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.lewa.systemuiext.R;
import com.lewa.systemuiext.adapter.StatusBarServiceAdapter;
import com.lewa.systemuiext.net.NetUsageView;

public class SlidingUpPanel extends SlidingUpPanelLayout implements
        StatusBarServiceAdapter.onExpandedChangeListener {
    private ImageView mDragImageView;
    private ViewGroup mSlideView;
    public static final String TAG = "SlidingUpPanel";
    public static final boolean DEBUG_VIEW = false;
    private View mSinglePagePanel;
    private View mDoublePagePanel;
    private boolean mPortrait = true;
    private boolean mSinglePage;

    //private View mBrightnessView;
    private View mMainView;
    //private int mBrightnessViewHeight = -1;
    private int mCollapseHeight;
    private SwitchWidget mSwitchWidget;
    private int mPanelAnchorHeight;

    private View mSwitchTabView;
    private NetUsageView mNetUsageView;
    private int mSwitchTabViewHeight = -1;
    public static final boolean DEBUG = false;

    public SlidingUpPanel(Context context) {
        this(context, null);
    }

    public SlidingUpPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    public void onExpandedChanged(boolean visible) {
        if(DEBUG)
        Log.d(TAG, "onExpandedVisible  collapsePanel visible = " + visible);
        if (visible) {
            mPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            resetStates();
        }
        if (mNetUsageView != null) {
            mNetUsageView.onExpandedChanged(visible);
        }
    }

    private void resetStates() {
        mFirstLayout = true;
        mSlideState = SlideState.COLLAPSED;
        mDragImageView.setImageResource(R.drawable.stat_switch_on);
        mMainView.setTranslationY(0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (DEBUG_LAYOUT) {
            Log.d(TAG, "onSizeChanged : " + w + ", " + h);
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        mMainView = (ViewGroup) getChildAt(0);
        mSlideView = (ViewGroup) getChildAt(1);
        mDragImageView = (ImageView) mSlideView.findViewById(android.R.id.icon);
        mSinglePagePanel = mSlideView.getChildAt(0);
        mDoublePagePanel = mSlideView.getChildAt(1);
        //mBrightnessView = mSlideView.findViewById(R.id.brightnessView);
        mSwitchWidget = (SwitchWidget) mSlideView
                .findViewById(R.id.switch_widget);
        mSwitchTabView = mSlideView.findViewById(R.id.tabs_linear);
        mNetUsageView = (NetUsageView) mMainView.findViewById(R.id.netusage);

        if (DEBUG_VIEW) {
            mSinglePagePanel.setBackgroundColor(Color.RED);
            mDoublePagePanel.setBackgroundColor(Color.GREEN);
        }
        resetStates();
        setPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelCollapsed(View panel) {
                mDragImageView.setImageResource(R.drawable.stat_switch_on);
            }

            @Override
            public void onPanelExpanded(View panel) {
            }

            @Override
            public void onPanelAnchored(View panel) {
                mDragImageView.setImageResource(R.drawable.stat_switch_off);
            }

            @Override
            public void onPanelHidden(View panel) {
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void onModeChanged(boolean singlePage) {
        mSinglePage = singlePage;
        if(DEBUG)
        Log.d(TAG, "singlePage " + singlePage);
        if (singlePage) {
            mSinglePagePanel.setVisibility(View.VISIBLE);
            mDoublePagePanel.setVisibility(View.GONE);
        } else {
            mSinglePagePanel.setVisibility(View.GONE);
            mDoublePagePanel.setVisibility(View.VISIBLE);
            setPanelHeight(getSwitchTabViewHeight());
            collapsePanel();
        }
        setEnabled(singlePage);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (mSinglePage) {
            int rowCount = SwitchWidget.BUTTONS_SIZE
                    / (mPortrait ? SwitchWidget.PORTRAIT_COLUMN_COUNT
                            : SwitchWidget.LANDSCAPE_COLUMN_COUNT);
            mCollapseHeight = getDragviewHeight() + getSwitchWidgetItemHeight()
                    - getPanelHeightHeightOffset();
            mPanelAnchorHeight = getDragviewHeight()
                    /*+ getBrightnessViewHeight() */+ getSwitchWidgetItemHeight()
                    * rowCount + 164;
            double anchorPoint = (1.0 * mPanelAnchorHeight - mCollapseHeight)
                    / (heightSize - mCollapseHeight);
            setAnchorPoint((float) (mPortrait ? 0.58 : 0.80)/* 6anchorPoint */);
            setPanelHeightInner(mCollapseHeight);// use setPanelHeightInner
                                                 // instead of setPanelHeight to
                                                 // avoid dead loop
            if(DEBUG)
            Log.d(TAG, " mCollapseHeight : " + mCollapseHeight
                    + " mPanelAnchoreight  : " + mPanelAnchorHeight
                    + " anchorPoint: " + anchorPoint + " heightSize : "
                    + heightSize + " mPortrait : " + mPortrait);
        } else {
            setPanelHeightInner(getSwitchTabViewHeight());
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int getSwitchWidgetItemHeight() {
        return getContext().getResources().getDimensionPixelSize(
                R.dimen.switch_widget_item_height);
    }

    private int getPanelHeightHeightOffset() {
        return getContext().getResources().getDimensionPixelSize(
                R.dimen.switch_widget_panel_offset);
    }

    private int getSwitchTabViewHeight() {
        if (mSwitchTabViewHeight < 0) {
            mSwitchTabView.measure(MeasureSpec.UNSPECIFIED,
                    MeasureSpec.UNSPECIFIED);
            mSwitchTabViewHeight = mSwitchTabView.getMeasuredHeight();
        }
        if(DEBUG)
        Log.d(TAG, "mSwitchTabViewHeight  :  " + mSwitchTabViewHeight);
        return mSwitchTabViewHeight;
    }

    /*private int getBrightnessViewHeight() {
        if (mBrightnessViewHeight < 0) {
            mBrightnessView.measure(MeasureSpec.UNSPECIFIED,
                    MeasureSpec.UNSPECIFIED);
            mBrightnessViewHeight = mBrightnessView.getMeasuredHeight();
        }
        if(DEBUG)
        Log.d(TAG, "mBrightnessViewHeight  :  " + mBrightnessViewHeight);
        return mBrightnessViewHeight;
    }*/

    private int getDragviewHeight() {
        if(DEBUG)
        Log.d(TAG,
                "dragView height  :  "
                        + getContext().getResources().getDimensionPixelSize(
                                R.dimen.drag_view_height));
        return getContext().getResources().getDimensionPixelSize(
                R.dimen.drag_view_height);
    }

    public SlidingUpPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
