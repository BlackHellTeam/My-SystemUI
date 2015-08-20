package com.lewa.systemuiext.widgets;
 
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.android.internal.statusbar.IStatusBarService;
import com.lewa.systemuiext.R;
import com.lewa.systemuiext.SystemUIExtApplication;
import com.lewa.systemuiext.adapter.StatusBarServiceAdapter;
import com.lewa.systemuiext.switchwidget.SwitchWidget;

import android.view.ViewGroup;


public class StatusBarSwitchLayout extends android.widget.RelativeLayout {

    private float lastmotionX;
    private float lastmotionY;
    private int mTouchSlop;
    private int mTouchflag = 0;
    private final static int VELOCITY_UNITS = 1000;
    private final static int SNAP_VELOCITY = 1800;
    private VelocityTracker mVelocityTracker;

    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int LEFT_SWIPE = 1;
    private static final int RIGHT_SWIPE = 2;
    private static final int UP_SWIPE = 3;
    public final boolean DEBUG = true;
    public static final String TAG = "StatusBarSwitchLayout";
    
    private TextView mTabSwitches;
    private TextView mTabNotifications;
    
    private View mTabNotificationsParent;
    private View mTabSwitchesParent;
    private View mScrollView;
    private View mSettingsButton;
    private View mClearButton;
    private View mQuickSettings;
    private View mNotifilterButton;

    private boolean mSinglePage;
    private IStatusBarService mBarService;

    private View mTabButtons;
    private SwitchWidget mSwitchWidget;
    private View mDragView;
    private View mSettingButton;
    private boolean mNotifilterFlag;

    public StatusBarSwitchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVelocityTracker = VelocityTracker.obtain();
    }
    
    public void setDragiew(View dragView) {
        mDragView = dragView;
    }

    public void onExpandVisible(boolean hasNotification) {
        int tab;
        if(hasNotification || mSinglePage) {
//            mSettingsButton.setVisibility(View.GONE);
            tab = TAB_NOTIFICATIONS;
         }else {
             tab = TAB_SWITCHES;
        }
        setTab(tab);
    }
    
    public void onModeChanged(boolean singlePage) {
        Log.d(TAG, "singlePage : " + singlePage);
        mSinglePage = singlePage;
        //TODO: remove the buttons in single mode
        mTabButtons.setVisibility(mSinglePage ? View.GONE : View.VISIBLE);
        if(mSinglePage) {
            setTab(TAB_NOTIFICATIONS);
        }
    }

    public void setTabButtons(View v) {
        mTabButtons = v;
        mTabNotifications = (TextView) mTabButtons.findViewById(R.id.image_tab_notifications);
        mTabNotificationsParent = mTabButtons.findViewById(R.id.tab_notifications_parent);
        mTabSwitches = (TextView) mTabButtons.findViewById(R.id.image_tab_switches);
        mTabSwitchesParent =  mTabButtons.findViewById(R.id.tab_switches_parent);
         
        mTabSwitchesParent.setOnClickListener(mOnClickListener);
        mTabNotificationsParent.setOnClickListener(mOnClickListener);
        onTabChanged(TAB_NOTIFICATIONS);

    }

    public void setNotifilterButton(View v) {
        mNotifilterButton = v;
    }
    public void setClearButton(View v) {
        mClearButton = v;
    }
    public void setSettingButton(View v) {
        mSettingButton = v;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //mClearButton  = rootView.findViewById(R.id.clear_all_button);
        mScrollView = getChildAt(0);
        //mSettingsButton = rootView.findViewById(R.id.settings_button);
        mQuickSettings = getChildCount() > 1 ?  getChildAt(1) : null;
        if(mQuickSettings != null) {
            mQuickSettings.setVisibility(View.GONE);
            mSwitchWidget = (SwitchWidget)mQuickSettings.findViewById(R.id.switch_widget);
        }
        mBarService = IStatusBarService.Stub.asInterface(ServiceManager
                        .getService(Context.STATUS_BAR_SERVICE));
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == mTabSwitchesParent && mTab != TAB_SWITCHES) {
                setTab(TAB_SWITCHES);
            }
            else if(v == mTabNotificationsParent && mTab != TAB_NOTIFICATIONS) {
                setTab(TAB_NOTIFICATIONS);
            }
            updateNotificationButton();
        }
    };
    
    public final static int TAB_NOTIFICATIONS = 0;
    public final static int TAB_SWITCHES = 1;
    
    public final static int SWITCH_WIDGET_STYLE = 2;
    private int mTab = 0;
    //private int mBrightnessViewHeight;
    //private View mBrightnessView;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if(DEBUG) {
            Log.d(TAG, "onInterceptTouchEvent : " + MotionEvent.actionToString(action));
        }
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchflag > 0)) {
            return true;
        }
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_MOVE:
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            mTouchflag = 0;
            break;
        default:
            break;
        }
        return mTouchflag > 0 ? true : false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mDragView == null) {
            mDragView = getRootView().findViewById(R.id.slidingPanel);
        }
        final float x = ev.getX();
        final float y = ev.getY();
        final int action = ev.getAction();
        if(DEBUG) {
            Log.d(TAG, "onTouchEvent : " + MotionEvent.actionToString(action));
        }
        boolean handled = false;
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            lastmotionX = x;
            lastmotionY = y;
            handled = true;
            break;
        case MotionEvent.ACTION_MOVE:
            final int xDiff = (int) Math.abs(x - lastmotionX);
            acquireVelocityTrackerIfNeeded();
            mVelocityTracker.addMovement(ev);
            mVelocityTracker.computeCurrentVelocity(VELOCITY_UNITS);
            float xVelocity = mVelocityTracker.getXVelocity();
            float yVelocity = mVelocityTracker.getYVelocity();
            if (lastmotionX - x > SWIPE_MIN_DISTANCE
                    && Math.abs(xVelocity) > SWIPE_THRESHOLD_VELOCITY ) {
                mTouchflag = LEFT_SWIPE;
                handled = true;
            }
            else if (x - lastmotionX  > SWIPE_MIN_DISTANCE
                    && Math.abs(xVelocity) > SWIPE_THRESHOLD_VELOCITY ) {
                mTouchflag = RIGHT_SWIPE;
                handled = true;
            }
            else if(lastmotionY - y > mTouchSlop * 2
                    && yVelocity < -SNAP_VELOCITY && xDiff < 200) {
                mTouchflag = UP_SWIPE;
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mTouchflag == LEFT_SWIPE && !mSinglePage) {
                if(DEBUG)Log.d(TAG, "LEFT_SWIPE");
                setTab(TAB_SWITCHES);
                handled = true;
            } else if (mTouchflag == RIGHT_SWIPE && !mSinglePage) {
                if(DEBUG)Log.d(TAG, "RIGHT_SWIPE");
                setTab(TAB_NOTIFICATIONS);
                handled = true;
            } 
            else if(mTouchflag == UP_SWIPE) {
                if(DEBUG)Log.d(TAG, "UP_SWIPE");
                StatusBarServiceAdapter.collapsePanels(mBarService);
            }
            mDragView.onTouchEvent(ev);
            mTouchflag = 0;
            releaseVelocityTracker();
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchflag = 0;
            releaseVelocityTracker();
            break;
        default:
            break;
        }
        final int rawX = (int)ev.getRawX();
        final int rawY = (int)ev.getRawY();
        int[] centerLocation = new int[2];
        getCenterLocation(mDragView, centerLocation);
        Log.d(TAG, "rawLocation : " + rawX+  " , " +  rawY );
        ev.offsetLocation(centerLocation[0] - rawX, centerLocation[1] - rawY);
        return mDragView.onTouchEvent(ev) || handled ;
    }

    private void getCenterLocation(View v, int[] center) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        int[] centerPoint = new int[2];
        centerPoint[0] = location[0] + (v.getRight() - v.getLeft())/2;
        centerPoint[1] = location[1] + (v.getBottom() - v.getTop())/2;
        Log.d(TAG, "centerPoint : " + centerPoint[0] +  " , " + centerPoint[1]);
    }

    private void acquireVelocityTrackerIfNeeded() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    
    public void setTab(int tab) {
        if(mTab != tab) {
            mTab = tab;
            changeTab();
        }
    }
    public int getTab(){
      return mTab;
    }
    
    // private int getBrightnessViewHeight() {
    //     if(mBrightnessViewHeight < 0 ) {
    //         mBrightnessView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    //         mBrightnessViewHeight = mBrightnessView.getMeasuredHeight();
    //     }
    //     Log.d(TAG, "mBrightnessViewHeight  :  " +  mBrightnessViewHeight);
    //     return mBrightnessViewHeight;
    // }

    @Override
    protected void onMeasure(int widthMeasureSpec,  int heightMeasureSpec) { 
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        final int rowCount = SwitchWidget.BUTTONS_SIZE / mSwitchWidget.getNumColumns();

        Log.d(TAG, "mSwitchWidget height  : " + (mSwitchWidget == null ? "null " :  mSwitchWidget.getMeasuredHeight()));
        final int switchItemHeight  = getSwitchWidgetItemHeight(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT, widthSize); 
        final int spacing = (heightSize - /*getBrightnessViewHeight() - */ switchItemHeight * rowCount ) / (rowCount + 1);
        updateSwitchWidget();
        super.onMeasure(widthMeasureSpec,  heightMeasureSpec);

        Log.d(TAG, "heightSize  : " +   getMeasuredHeight() + "   " + heightSize);
      }
    
    private void updateSwitchWidget() {
        int newMargin = mContext.getResources().getDimensionPixelSize(
                R.dimen.switch_widget_top_margin);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mSwitchWidget
                .getLayoutParams();
        if (newMargin != lp.topMargin) {
            lp.topMargin = newMargin;
            mSwitchWidget.setLayoutParams(lp);
        }
    }
    private int getSwitchWidgetItemHeight(boolean portrait, int screenWidth) {
        Point itemDimen = mSwitchWidget.getItemDimen(portrait, screenWidth);
        return itemDimen.y;
    }

    private  void onTabChanged(int tab) {
        int color_select = mContext.getResources().getColor(R.color.white);
        int color_alpha = mContext.getResources().getColor(R.color.alpha_white_45_percent_color);
        if (mTab == TAB_NOTIFICATIONS) {
            mTabSwitches.setTextColor(color_alpha);
            mTabNotifications.setTextColor(color_select);
        } else {
            mTabSwitches.setTextColor(color_select);
            mTabNotifications.setTextColor(color_alpha);
        }
    }
    public void changeTab() {
        changeTab(true);
    }
    public void changeTab(boolean animate) {
        if (mTab == TAB_NOTIFICATIONS) {
            if(animate) {
                Animation inAnim = AnimationUtils.loadAnimation(mContext,  R.anim.slide_in_left);
                Animation outAnim = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right);
                outAnim.setDuration(300);
                inAnim.setDuration(300);
                AnimationListener animlis = new AnimationListener(){
                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        mQuickSettings.setVisibility(View.GONE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation arg0) {

                    }
                    @Override
                    public void onAnimationStart(Animation arg0) {

                    }
                };
                outAnim.setAnimationListener(animlis);
                mScrollView.setAnimation(inAnim);
                mScrollView.startAnimation(inAnim);
                mQuickSettings.setAnimation(outAnim);
                mQuickSettings.startAnimation(outAnim);
            }

            mScrollView.setVisibility(View.VISIBLE);
            mQuickSettings.setVisibility(View.GONE);
        } else {
            if(animate) { 
                Animation inAnim = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
                Animation outAnim = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_left);
                outAnim.setDuration(300);
                inAnim.setDuration(300);
                AnimationListener animlis = new AnimationListener(){
                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        mScrollView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }

                    @Override
                    public void onAnimationStart(Animation arg0) {
                    }
                };
                outAnim.setAnimationListener(animlis);
                mQuickSettings.setAnimation(inAnim);
                mQuickSettings.startAnimation(inAnim);
                mScrollView.setAnimation(outAnim);
                mScrollView.startAnimation(outAnim);
            }
            mQuickSettings.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        }
        //updateButtonVisible(mTab == TAB_SWITCHES);
        updateNotificationButton();
        onTabChanged(mTab);
    }
    
    private void updateButtonVisible(boolean isSwitchTab) {
        int visible_notify = View.INVISIBLE;
        int visible_clear = View.INVISIBLE;
        if (isSwitchTab) {
            visible_notify = View.VISIBLE;
            // visible_clear = View.INVISIBLE;
        } else {
            // visible_notify = View.INVISIBLE;
            visible_clear = View.VISIBLE;
        }
        if (mNotifilterButton != null)
            mNotifilterButton.setVisibility(visible_notify);
        if (mClearButton != null)
            mClearButton.setVisibility(visible_clear);
    }
    // LEWA BEGIN
    public void updateNotificationButton() {
        if (mSinglePage) {
            if (mSettingButton != null) {
                mSettingButton.setVisibility(View.GONE);
            }
                mSettingButton
                        .setVisibility(mClearButton.getVisibility() != View.VISIBLE ? View.VISIBLE
                                : View.GONE);
        } else {
            if (mTab == TAB_NOTIFICATIONS) {
                if (mClearButton != null) {
                    mClearButton
                            .setVisibility(mClearButton.isEnabled() ? View.VISIBLE
                                    : View.GONE);
                }
                if (mSettingButton != null) {
                    mSettingButton.setVisibility(mClearButton
                            .getVisibility() != View.VISIBLE ? View.VISIBLE
                            : View.GONE);
                }
            } else {
                if (mClearButton != null) {
                    mClearButton.setVisibility(View.GONE);
                }
                if (mSettingButton != null) {
                    mSettingButton.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    //added for bug:#67540
    public void changeTabNoAnimate(boolean hasNotification) {
        int tab;
        if (hasNotification || mSinglePage) {
            tab = TAB_NOTIFICATIONS;
        } else {
            tab = TAB_SWITCHES;
        }
        if (mTab != tab) {
            mTab = tab;
            changeTab(false);
        }
    }
}
