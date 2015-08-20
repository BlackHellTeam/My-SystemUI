/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.systemui.statusbar.phone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.lang.reflect.Field;
import java.util.List;

import android.view.View;
import android.view.MotionEvent;
import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.annotation.LewaHook;
import android.content.pm.PackageManager;
import android.view.View.OnClickListener;

import com.lewa.systemuiext.widgets.Clock;
import com.android.systemui.R;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.CloseDragHandle;
import com.lewa.systemuiext.utils.PhoneStatusbarSettings;

import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;

import com.android.systemui.statusbar.StatusBarIconView;

import android.util.Log;
import lewa.telephony.LewaSimInfo;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.content.res.Configuration;
import android.widget.ScrollView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import android.os.Build;
//import com.android.systemui.statusbar.policy.LewaBatteryController;
import android.widget.ImageView;
import android.os.Vibrator;

import com.lewa.systemuiext.utils.ShakeListener;

import android.os.Bundle;

import com.lewa.systemuiext.SystemUIExtApplication;
import com.lewa.systemuiext.widgets.StatusBarSwitchLayout;
import com.lewa.systemuiext.Constants;
import com.lewa.systemuiext.widgets.SlidingUpPanel;

import android.preference.PreferenceManager;
import android.os.PatternMatcher;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Resources;
import android.view.ViewGroup;

import com.lewa.systemuiext.switchwidget.SwitchButtonFactory;

import android.view.animation.TranslateAnimation;
import android.graphics.drawable.Drawable;

import com.android.systemui.statusbar.phone.CloseDragHandle;

import android.widget.ImageButton;

import com.android.keyguard.AlphaOptimizedImageButton;
import com.lewa.systemuiext.widgets.DateView;

/**
 * @author juude.song@gmail.com 1. add birdview functions 2. add date and time
 *         view listener 3. override interceptTouchEvent to prevent collapsing
 *         4. add netusage 6. inject statusbar transparency ability
 */

public class LewaPhoneStatusBar extends PhoneStatusBar implements
        ShakeListener.CallBack {
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefChangedListener;

    private SharedPreferences mStatusbar_settings;
    private KeyguardManager mKM;
    private StatusBarWindowView mStatusBarWindowView;
    private boolean mHasNetMgr = false;
    private StatusBarSwitchLayout mSwtichLayout;
    private Display mDisplay;
    private DisplayMetrics mDisplayMetrics;
    private LinearLayout mExpandViewLayout;
    private TextView mTabSwitches;
    private TextView mTabNotifications;
    private boolean mLastScrollViewIsMaxHeight = false;
    private boolean mScrollViewIsMaxHeight = true;
    private ShakeListener mShakeListener;
    public static final String TAG = "LewaPhoneStatusBar";
    public static boolean LEWA_SYSTEMUI = true;
    private boolean mSinglePage;
    private boolean mShakeClean;
    private SlidingUpPanel mSlidingUpPanel;
    public static boolean CONFIG_HIDE_NETWORK_TYPE;
    private IntentFilter mIntentFilter;
    public static final String ACTION_THEME_CHANGED = "com.lewa.intent.action.THEME_CHANGED";
    private RelativeLayout mSwitchWidgetContainer;
    private RelativeLayout mlayoutTab;
    protected RelativeLayout mNotificationHeader;
    protected DateView mDateView;
    protected Clock mClock;
    private  SharedPreferences prefs;

    // end
    public LewaPhoneStatusBar() {
        sInstance = this;
    }
    private static LewaPhoneStatusBar sInstance = null;

    public static LewaPhoneStatusBar getInstance() {
        return sInstance;
    }

    @Override
    public boolean interceptTouchEvent(MotionEvent event) {
        int lockexpanded = mStatusbar_settings.getInt(
                Constants.PREF_LOCKSCREEN_EXPANDABLE, 1);
        if (lockexpanded == 0) {
            if (mKM.inKeyguardRestrictedInputMode()) {
                return true;
            }
        }else {
            if (mKM.inKeyguardRestrictedInputMode() && mKM.isKeyguardSecure()) {
                return true;
            }
        }
        return super.interceptTouchEvent(event);
    }

    public void directToAlarm(Context context, String packageName) {
        Intent intent = new Intent();
        try {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(packageName, "com.android.deskclock.AlarmClock");
            intent.setPackage(packageName);
        } catch (Exception e) {
        }
        context.startActivity(intent);
    }

    public void directToCalendar(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent();
        try {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent = packageManager.getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
        } catch (Exception e) {
        }

    }

    @Override
    public void onShake() {
        if ((mClearButton == null) || !mExpandedVisible) {
            return;
        }
        if (mClearButton.isEnabled()) {
            Vibrator vibrator = (Vibrator) mContext
                    .getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
            Log.d(TAG, "shaked ");
            mClearButton.performClick();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateConfiguration();
    }

    private void updateConfiguration() {

        Resources resources = mContext.getResources();

        if (mSwitchWidgetContainer != null) {
            int newMargin = resources
                    .getDimensionPixelSize(mSinglePage ? R.dimen.status_bar_switchwidget_margin_onpage
                            : R.dimen.status_bar_switchwidget_margin);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mSwitchWidgetContainer
                    .getLayoutParams();
            if (newMargin != lp.bottomMargin) {
                lp.bottomMargin = newMargin;
                mSwitchWidgetContainer.setLayoutParams(lp);
            }
        }

        if (mlayoutTab != null) {
            int newHeight = resources
                    .getDimensionPixelSize(R.dimen.swtichtabheight);
            ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mlayoutTab
                    .getLayoutParams();
            if (newHeight != lp.height) {
                lp.height = newHeight;
                ((ViewGroup) mlayoutTab.getParent()).updateViewLayout(
                        mlayoutTab, lp);
            }
        }
        if (mCloseDragHandle != null) {
            int newHeight = resources
                    .getDimensionPixelSize(R.dimen.close_handle_height);
            ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mCloseDragHandle
                    .getLayoutParams();
            if (newHeight != lp.height) {
                lp.height = newHeight;
                ((ViewGroup) mCloseDragHandle.getParent()).updateViewLayout(
                        mCloseDragHandle, lp);
            }
        }

        if (mSlidingUpPanel != null) {
            int newMargin = resources
                    .getDimensionPixelSize(mSinglePage ? R.dimen.close_handle_height_onepage
                            : R.dimen.close_handle_height);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mSlidingUpPanel
                    .getLayoutParams();

            if (newMargin != lp.bottomMargin) {
                lp.bottomMargin = newMargin;
                mSlidingUpPanel.setLayoutParams(lp);
            }
        }

        if (mNotificationHeader != null) {
            int newHeight = resources
                    .getDimensionPixelSize(R.dimen.notification_header_height);
            ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mNotificationHeader
                    .getLayoutParams();

            if (newHeight != lp.height) {
                lp.height = newHeight;
                ((ViewGroup) mNotificationHeader.getParent()).updateViewLayout(
                        mNotificationHeader, lp);
            }
        }
    }
    @Override
    protected PhoneStatusBarView makeStatusBarView() {
        PhoneStatusBarView statusBarView = super.makeStatusBarView();
        mStatusBarWindowView = getStatusBarWindow();
        mShakeListener = new ShakeListener(mContext, this);
        initView();
        mStatusbar_settings = mContext.getSharedPreferences(
                "statusbar_settings", 0);
        mKM = (KeyguardManager) mContext
                .getSystemService(Context.KEYGUARD_SERVICE);
        updateClockAndDateView();
        setCarrierLableListener();
        mContext.registerReceiver(mBroadcastReceiver, getThemeFilter());
        return statusBarView;
    }

    private boolean getClearableNotifilter() {
        final boolean any = mNotificationData.size() > 0;
        final boolean clearable = any
                && mNotificationData.hasActiveClearableNotifications();
        return clearable;
    }

    @Override
    public void updateButton() {
        if (mSinglePage) {
            mClearButton.setAlpha(getClearableNotifilter() ? 1.0f : 0.0f);
            mClearButton.setVisibility(getClearableNotifilter() ? View.VISIBLE
                    : View.GONE);
            mSettingsButton
                    .setVisibility(!getClearableNotifilter() ? View.VISIBLE
                            : View.GONE);
        } else {
            if (mSwtichLayout == null) {
                mSwtichLayout = (StatusBarSwitchLayout) getStatusBarWindow()
                        .findViewById(R.id.statusbarswitchlayout);
            }
            if (mSwtichLayout.getTab() == StatusBarSwitchLayout.TAB_NOTIFICATIONS) {
                mSettingsButton.setVisibility(View.GONE);
                mClearButton.setAlpha(getClearableNotifilter() ? 1.0f : 0.0f);
                mClearButton
                        .setVisibility(getClearableNotifilter() ? View.VISIBLE
                                : View.GONE);
                mSettingsButton
                        .setVisibility(!getClearableNotifilter() ? View.VISIBLE
                                : View.GONE);
            } else {
                mClearButton.setVisibility(View.GONE);
                mSettingsButton.setVisibility(View.VISIBLE);
            }

        }
    }

    private void setPageModeListener() {
        Log.d(TAG, "setPageModeListener V18");
        prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        mSinglePage = prefs.getBoolean(Constants.PREF_NOTITIFACTION_STYLE,
                Constants.NOTIFICATION_SINGLE_PAGE);
        mShakeListener.register();
        onPageChanged();
        mPrefChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs,
                    String key) {
                Log.d(TAG, "Settings key changed V18: " + key);
                if (key.equals(Constants.PREF_NOTITIFACTION_STYLE)) {
                    mSinglePage = prefs.getBoolean(key,
                            Constants.NOTIFICATION_SINGLE_PAGE);
                    onPageChanged();
                    updateButton();
                    sendPageChangedBroadcast();
                }else if(key.equals("shake_clean")){
                    mShakeClean = prefs.getBoolean("shake_clean", true);
                    Log.d(TAG, "mShakeClean V18: " + mShakeClean);
                    if (mShakeClean)
                        mShakeListener.register();
                    else
                        mShakeListener.unregister();
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(mPrefChangedListener);
    }

    private void sendPageChangedBroadcast() {
        Intent intentPage = new Intent();
        intentPage
                .setAction(SwitchButtonFactory.ACTION_SWITCH_WIDGET_PAGE_CHANGE);
        mContext.sendBroadcast(intentPage);
    }

    private void onPageChanged() {
        final boolean single = (mSinglePage == Constants.NOTIFICATION_SINGLE_PAGE);
        mSwtichLayout.onModeChanged(single);
        mSlidingUpPanel.onModeChanged(single);
        updateConfiguration();
        final int resid = single ? R.drawable.status_bar_bottom_single_selector
                : R.drawable.status_bar_bottom_selector;
        mCloseDragHandle.setBackgroundResource(resid);
    }

    public void initView() {
        mDisplayMetrics = new DisplayMetrics();
        mDisplay = ((WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        mSlidingUpPanel = (SlidingUpPanel) mStatusBarWindowView
                .findViewById(R.id.slidingPanel);
        mSwitchWidgetContainer = (RelativeLayout) mStatusBarWindowView
                .findViewById(R.id.switch_widget_container);
        mlayoutTab = (RelativeLayout) mStatusBarWindowView
                .findViewById(R.id.layout_tab);
        mSwtichLayout = (StatusBarSwitchLayout) mStatusBarWindowView
                .findViewById(R.id.statusbarswitchlayout);
        mSwtichLayout.setTabButtons(mStatusBarWindowView
                .findViewById(R.id.tabs_linear));
        mSwtichLayout.setNotifilterButton(mNotifilterButton);
        mSwtichLayout.setSettingButton(mSettingsButton);
        mSwtichLayout.setClearButton(mClearButton);
        mNotificationHeader = (RelativeLayout) mStatusBarWindowView
                .findViewById(R.id.notification_header);
        mClock = (Clock) mNotificationHeader.findViewById(R.id.clock);
        mDateView = (DateView) mNotificationHeader.findViewById(R.id.date);
        setPageModeListener();
        updateButton();
    }

    private void setCarrierLableListener() {
        final Context context = mContext;
        Resources res = context.getResources();
        View carrierLableGemini = mStatusBarWindowView.findViewById(
                R.id.notification_panel)
                .findViewById(R.id.carrier_label_gemini);
        if (isMSim() && carrierLableGemini != null) {
            carrierLableGemini.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    List<LewaSimInfo> list = LewaSimInfo
                            .getInsertedSimList(mContext);
                    if (list.size() > 1) {
                        Intent i = new Intent(
                                "com.android.settings.multisimsettings.MultiSimSettings");
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(i);
                        int flags = CommandQueue.FLAG_EXCLUDE_NONE;
                        animateCollapsePanels(flags);
                    }
                }
            });
        }
    }

    private void updateClockAndDateView() {
        mClock.setNoAmpm();
        mClock.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                directToAlarm(mContext, "com.android.deskclock");
                int flags = CommandQueue.FLAG_EXCLUDE_NONE;
                animateCollapsePanels(flags);
            }
        });
        mDateView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                directToCalendar(
                        mContext,
                        mContext.getResources().getString(
                                R.string.config_calendar_app));
                int flags = CommandQueue.FLAG_EXCLUDE_NONE;
                animateCollapsePanels(flags);
            }
        });
    }

    @Override
    public void start() {
        super.start();
    }

    private IntentFilter getThemeFilter() {
        if (mIntentFilter != null) {
            return mIntentFilter;
        }
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_THEME_CHANGED);
        mIntentFilter.addDataScheme("content");
        try {
            mIntentFilter.addDataType("vnd.lewa.cursor.item/theme");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(TAG, "Could not add MIME types to filter", e);
        }
        mIntentFilter.addDataAuthority("com.lewa.themechooser.themes", null);
        mIntentFilter.addDataPath("/theme", PatternMatcher.PATTERN_PREFIX);
        return mIntentFilter;
    }

    @Override
    void makeExpandedInvisible() {
        super.makeExpandedInvisible();
        mSlidingUpPanel.onExpandedChanged(false);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive, action=" + action);
            if (action.equals(ACTION_THEME_CHANGED)) {
                sendComplateBroadcast();
            }
        }
    };

    private void sendComplateBroadcast() {
        Intent i = new Intent(
                "com.android.systemui.RECREATE_STATUSBAR_COMPLATE");
        mContext.sendBroadcast(i);
    }

    @Override
     void makeExpandedVisible(boolean force) {
        super.makeExpandedVisible(force);
        mSwtichLayout.changeTabNoAnimate(mNotificationData != null
                && mNotificationData.size() > 0);
        mSlidingUpPanel.onExpandedChanged(true);
    }

    @Override
    protected void setAreThereNotifications() {
        super.setAreThereNotifications();
        updateButton();
    }

    @Override
    public void toggleRecentApps() {
        if (SHOW_BIRDVIEW) {
            showBirdView();
        } else {
            super.toggleRecentApps();
        }
    }
    private static final boolean SHOW_BIRDVIEW = true;
    private Intent intentBirdView;

    public void showBirdView() {
        if (intentBirdView == null) {
            intentBirdView = new Intent(
                    "com.lewa.birdview.action.TOGGLE_RECENTS");
            intentBirdView.setPackage("com.lewa.birdview");
        }
        try {
            PackageManager pm = mContext.getPackageManager();
            ComponentName componentName = new ComponentName(
                    "com.android.settings",
                    "com.android.settings.wizard.WizardActivity");
            int wizardComponeEnableSetting = pm
                    .getComponentEnabledSetting(componentName);
            if (wizardComponeEnableSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                mContext.startService(intentBirdView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void destroy() {
        super.destroy();
        mContext.unregisterReceiver(mBroadcastReceiver);
        prefs.unregisterOnSharedPreferenceChangeListener(mPrefChangedListener);
    }
}
