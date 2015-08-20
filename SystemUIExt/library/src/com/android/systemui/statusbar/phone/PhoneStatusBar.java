/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.android.systemui.statusbar.policy.NotificationRowLayout;


public class PhoneStatusBar  {
    static final String TAG = "PhoneStatusBar";
    boolean mExpandedVisible;
    private PhoneStatusBarView mStatusBarView;
    StatusBarWindowView mStatusBarWindow;
    private WindowManager mWindowManager;
    protected Context mContext;
    NotificationPanelView mNotificationPanel; // the sliding/resizing panel within the notification window
    protected H mHandler = createHandler();
    private NotificationRowLayout mPile;

    public void setLayers(int layerType) {
//        setPileLayers(layerType);
//        setQuickSettingLayers(layerType);
//        setNotificationIconsLayers(layerType);
//        setSignalClusterLayers(layerType);
    }
    
    protected H createHandler() {
        return new H();
    }
    
    public PhoneStatusBar(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        addStatusBarWindow();
    }
    
    protected PhoneStatusBarView makeStatusBarView() {
        mStatusBarWindow = (StatusBarWindowView) View.inflate(mContext, com.lewa.systemuiext.R.layout.fake_super_status_bar, null);
        mStatusBarWindow.mService = this;
        
        mStatusBarView = (PhoneStatusBarView) mStatusBarWindow.findViewById(com.lewa.systemuiext.R.id.status_bar);
        
        PanelHolder holder = (PanelHolder) mStatusBarWindow.findViewById(com.lewa.systemuiext.R.id.panel_holder);
        mStatusBarView.setPanelHolder(holder);
        
        mStatusBarView.setBar(this);
        return mStatusBarView;
    }
    
    private void addStatusBarWindow() {
        // Put up the view
        final int height = 24 * 3; //getStatusBarHeight();

        // Now that the status bar window encompasses the sliding panel and its
        // translucent backdrop, the entire thing is made TRANSLUCENT and is
        // hardware-accelerated.
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

        lp.gravity = getStatusBarGravity();
        lp.setTitle("StatusBar Fake");
        lp.packageName = mContext.getPackageName();
        makeStatusBarView();
        mNotificationPanel = (NotificationPanelView) mStatusBarWindow.findViewById(com.lewa.systemuiext.R.id.notification_panel);
        mNotificationPanel.setStatusBar(this);
        
        mPile = (NotificationRowLayout)mStatusBarWindow.findViewById(com.lewa.systemuiext.R.id.latestItems);

        mWindowManager.addView(mStatusBarWindow, lp);

    }
    
    protected static final int MSG_TOGGLE_RECENTS_PANEL = 1020;
    protected static final int MSG_CLOSE_RECENTS_PANEL = 1021;
    protected static final int MSG_PRELOAD_RECENT_APPS = 1022;
    protected static final int MSG_CANCEL_PRELOAD_RECENT_APPS = 1023;
    protected static final int MSG_OPEN_SEARCH_PANEL = 1024;
    protected static final int MSG_CLOSE_SEARCH_PANEL = 1025;
    protected static final int MSG_SHOW_HEADS_UP = 1026;
    protected static final int MSG_HIDE_HEADS_UP = 1027;
    protected static final int MSG_ESCALATE_HEADS_UP = 1028;
    
    protected class H extends Handler {
        public void handleMessage(Message m) {
            Intent intent;
            switch (m.what) {
//             case MSG_TOGGLE_RECENTS_PANEL:
//                 toggleRecentsActivity();
//                 break;
//             case MSG_CLOSE_RECENTS_PANEL:
//                 closeRecents();
//                 break;
//             case MSG_PRELOAD_RECENT_APPS:
//                  preloadRecentTasksList();
//                  break;
//             case MSG_CANCEL_PRELOAD_RECENT_APPS:
//                  cancelPreloadingRecentTasksList();
//                  break;
//             case MSG_OPEN_SEARCH_PANEL:
//                 if (DEBUG) Log.d(TAG, "opening search panel");
//                 if (mSearchPanelView != null && mSearchPanelView.isAssistantAvailable()) {
//                     mSearchPanelView.show(true, true);
//                     onShowSearchPanel();
//                 }
//                 break;
//             case MSG_CLOSE_SEARCH_PANEL:
//                 if (DEBUG) Log.d(TAG, "closing search panel");
//                 if (mSearchPanelView != null && mSearchPanelView.isShowing()) {
//                     mSearchPanelView.show(false, true);
//                     onHideSearchPanel();
//                 }
//                 break;
            }
        }
    }
    
    protected int getStatusBarGravity() {
        return Gravity.TOP | Gravity.FILL_HORIZONTAL;
    }
    
    void makeExpandedInvisibleSoon() {
        mHandler.postDelayed(new Runnable() { public void run() { makeExpandedInvisible(); }}, 50);
    }
    
    void makeExpandedInvisible() {
        if (true) Log.d(TAG, "makeExpandedInvisible: mExpandedVisible=" + mExpandedVisible
                + " mExpandedVisible=" + mExpandedVisible);

        if (!mExpandedVisible) {
            return;
        }

        // Ensure the panel is fully collapsed (just in case; bug 6765842, 7260868)
        mStatusBarView.collapseAllPanels(/*animate=*/ false);

        mExpandedVisible = false;

//        visibilityChanged(false);

        // Shrink the window to the size of the status bar only
//        if (mStatusBarWindow.isAttachedToWindow()) {
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) mStatusBarWindow.getLayoutParams();
            lp.height = getStatusBarHeight();
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            lp.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            mWindowManager.updateViewLayout(mStatusBarWindow, lp);
//        }

//        if ((mDisabled & StatusBarManager.DISABLE_NOTIFICATION_ICONS) == 0) {
//            setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
//        }

//        /// M: [SystemUI] Support "Notification toolbar". {
//        mToolBarView.dismissDialogs();
//        if (mQS != null) {
//            mQS.dismissDialogs();
//        }
//        /// M: [SystemUI] Support "Notification toolbar". }
//
//        /// M: [SystemUI] Dismiss application guide dialog.@{
//        if (mAppGuideDialog != null && mAppGuideDialog.isShowing()) {
//            mAppGuideDialog.dismiss();
//            Xlog.d(TAG, "performCollapse dismiss mAppGuideDialog");
//        }
//        /// M: [SystemUI] Dismiss application guide dialog.@}
//
//        
//        // Close any "App info" popups that might have snuck on-screen
//        dismissPopups();
//
//        if (mPostCollapseCleanup != null) {
//            mPostCollapseCleanup.run();
//            mPostCollapseCleanup = null;
//        }
//
//        setInteracting(StatusBarManager.WINDOW_STATUS_BAR, false);

        /// M: [Performance] Hide status bar while the panel is tracking.
        if (mStatusBarView != null) {
            mStatusBarView.setVisibility(View.VISIBLE);
        }
    }
    
    boolean panelsEnabled() {
//        return (mDisabled & StatusBarManager.DISABLE_EXPAND) == 0;
        return true;
    }

    void makeExpandedVisible() {
        if (true) Log.d(TAG, "Make expanded visible: expanded visible=" + mExpandedVisible);
        if (mExpandedVisible || !panelsEnabled()) {
            return;
        }

        mExpandedVisible = true;
//        mPile.setLayoutTransitionsEnabled(true);
//        if (mNavigationBarView != null)
//            mNavigationBarView.setSlippery(true);
//
//        updateCarrierLabelVisibility(true);
//
//        updateExpandedViewPos(EXPANDED_LEAVE_ALONE);

        // Expand the window to encompass the full screen in anticipation of the drag.
        // This is only possible to do atomically because the status bar is at the top of the screen!
//        if (mStatusBarWindow.isAttachedToWindow()) {
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) mStatusBarWindow.getLayoutParams();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mWindowManager.updateViewLayout(mStatusBarWindow, lp);
//        }

        /// M: Show always update clock of DateView.
//        if (mDateView != null) {
//            mDateView.updateClock();
//        }
//        visibilityChanged(true);
//
//        setInteracting(StatusBarManager.WINDOW_STATUS_BAR, true);
    }
    
    public void animateCollapsePanels() {
        //animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE);
        mStatusBarView.collapseAllPanels(true);
    }
    
    public int getStatusBarHeight() {
//        if (mNaturalBarHeight < 0) {
//            final Resources res = mContext.getResources();
//            mNaturalBarHeight =
//                    res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
//        }
//        return mNaturalBarHeight;
        return 72;
    }
}
