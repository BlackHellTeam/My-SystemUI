
package com.lewa.systemuiext.switchwidget;

/*
 * Copyright (C) 2011 LewaTek
 *
 * @version: 1.0
 * @since: 2011/07/21
 * @update: 2011/07/21
 *
 * @description:
 *      SwitchButton: base class of all of switch buttons
 *      StatelessButton: base class of switch buttons without a state
 *
 * @author: Woody Guo <zjguo@lewatek.com>
 * 
 * @update: 2014/04/30
 * @description:
 * refactor to 
 *     1. make it crossPlatform
 *     2. fix layout problems
 * @author: JD.Song<jdsong@lewatek.com>
 * TODO: import from old settings
 */
import java.util.ArrayList;
import java.util.List;

import lewa.provider.ExtraSettings;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.statusbar.IStatusBarService;
import com.lewa.systemuiext.adapter.StatusBarServiceAdapter;

import com.lewa.systemuiext.R;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
/**
 * @author: Woody Guo <guozhenjiang@ndoo.net>
 * @description: A subclass must implement the methods updateState and
 *               toggleState
 */
public abstract class SwitchButton {
    public static final String TAG = "SwitchWidget.SwitchButton";

    public static final int STATE_ENABLED = 1;
    public static final int STATE_DISABLED = 2;
    public static final int STATE_TURNING_ON = 3;
    public static final int STATE_TURNING_OFF = 4;
    public static final int STATE_INTERMEDIATE = 5;
    public static final int STATE_UNKNOWN = 6;

    public static final String BUTTON_POWER_MANAGER = "togglePowermanager";

    public static final String BUTTON_QR_CODE = "buttonQrCode";
    public static final String POWERSAVING_ACTION_NOTIFY_ON = "powersaving_action_notify_on";
    public static final String POWERSAVING_DEV_TYPE = "dev_type";

    public static final int DEV_AIRPLANE = 1;
    public static final int DEV_BLUETOOTH = 2;
    public static final int DEV_GPS = 3;
    public static final int DEV_DATA = 4;
    public static final int DEV_SYNC = 5;
    public static final int DEV_WIFI = 6;

    public String QUERY_POWER_STATUS_ACTION = "com.lewa.spm_notification_toast_action";
    public String QUERY_POWER_STATUS_ACTION_KEY = "spm_notification_toast_message";
    public static String QUERY_POWER_STATUS_RULE_ACTION = "com.lewa.spm_notification_long_mode_rule";
    public static String QUERY_POWER_STATUS_RULE_ACTION_KEY = "longRule";
    public static String QUERY_POWER_STATUS_SLEEP_MODE_RULE_ACTION = "com.lewa.spm_notification_sleep_mode_rule";
    public static String QUERY_POWER_STATUS_SLEEP_MODE_RULE_ACTION_KEY = "sleepRule";
    public int DELAY_RUN_TIME = 5000;
    public static String NOTICE_POWER_MANAGER_MSG = "status_bar_notice_power_msg";

    public String NOTICE_POWER_MSG_SLEEP_KEY = "sleep_diff_key";
    public int NOTICE_POWER_MSG_SLEEP_KEY_VALUE = 100;

    public static boolean TINY_MODE = false;

    // A list of currently loaded buttons
    public static Context sContext = null;

    protected int mIcon;
    protected int mLabel;
    public String mLabelString;
    public int mButtonName;
    protected int mState;
    protected ImageView mViewImg;
    public TextView mView;
    protected String mType = "";
    public int mSettingsIcon;

    public static int sDisabledColor = -1;
    public static int sEnabledColor = -1;
    protected int mTextColor = -1;
    private IWindowManager mWM;
    public static final boolean DEBUG = false;
    /**
     * @author: Woody Guo <guozhenjiang@ndoo.net>
     * @description: Starts an activity with a given intent action
     * @param action is what the activity intent to do
     */
    public void startActivity(final String action) {
        dismissLockscreen();
        Intent intent = new Intent(action);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sContext.startActivity(intent);
        IStatusBarService mBarService = IStatusBarService.Stub
                .asInterface(ServiceManager
                        .getService(Context.STATUS_BAR_SERVICE));
        try {
            StatusBarServiceAdapter.collapsePanels(mBarService);
        } catch (Exception ex) {
            Log.e(TAG, "", ex);
        }
    }

    public SwitchButton() {
        mTextColor = sEnabledColor;
        mWM = WindowManagerGlobal.getWindowManagerService();
    }

    public static void setContext(Context context) {
        sContext = context;
        if(sDisabledColor == -1) {
          sDisabledColor = context.getResources().getColor(R.color.switch_widget_disabled_color);
          sEnabledColor = context.getResources().getColor(R.color.switch_widget_enabled_color);
        }
    }

    public void directTo(Context context, String packageName) {
        dismissLockscreen();
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent();
        try {
            intent = packageManager.getLaunchIntentForPackage(packageName);
        } catch (Exception e) {
        }
        context.startActivity(intent);
        IStatusBarService mBarService = IStatusBarService.Stub
                .asInterface(ServiceManager
                        .getService(Context.STATUS_BAR_SERVICE));
        try {
            StatusBarServiceAdapter.collapsePanels(mBarService);
        } catch (Exception ex) {

        }
    }

    /**
     * @author: Woody Guo <guozhenjiang@ndoo.net>
     * @description: Starts a specific component
     * @param pkgName is the name of the package containing the specific
     *            component
     * @param className is the class name of the specific component
     */
    public void startActivity(final String pkgName, final String className) {
        dismissLockscreen();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(pkgName, className);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sContext.startActivity(intent);
        IStatusBarService mBarService = IStatusBarService.Stub
                .asInterface(ServiceManager
                        .getService(Context.STATUS_BAR_SERVICE));
        try {
            StatusBarServiceAdapter.collapsePanels(mBarService);
        } catch (Exception ex) {
            Log.e(TAG, "", ex);
        }

    }

    public void startActivity(Intent intent) {
        dismissLockscreen();
        sContext.startActivity(intent);
        IStatusBarService mBarService = IStatusBarService.Stub
                .asInterface(ServiceManager
                        .getService(Context.STATUS_BAR_SERVICE));
        try {
            StatusBarServiceAdapter.collapsePanels(mBarService);
        } catch (Exception ex) {

        }
    }

    private void dismissLockscreen() {
        try {
//            ActivityManagerNative.getDefault()
//                    .keyguardWaitingForActivityDrawn();
            mWM.dismissKeyguard();
        } catch (RemoteException e) {
        }

    }

    /**
     * @author: Woody Guo <guozhenjiang@ndoo.net>
     * @description: Gets the state of a button
     */
    protected abstract void updateState();

    /**
     * @author: Woody Guo <guozhenjiang@ndoo.net>
     * @description: Button is clicked
     */
    protected abstract void toggleState();

    /**
     * @author: Woody Guo <guozhenjiang@ndoo.net>
     * @description: Does nothing by default when a button is long clicked
     */
    protected boolean onLongClick() {
        return false;
    }

    /**
     * @author: Woody Guo <guozhenjiang@ndoo.net>
     * @description: Refresh button state
     */
    protected void update() {
        updateState();
        updateView();
    }

    /**
     * // do nothing as a standard, override this if the button needs to respond
     * // to broadcast events from the StatusBarService broadcast receiver
     */
    protected void onReceive(Context context, Intent intent) {

    }

    protected void onChangeUri(Uri uri) {
        // do nothing as a standard, override this if the
        // handleOnChangeUributton needs to respond
        // to a changed setting
    }

    protected IntentFilter getBroadcastIntentFilter() {
        return null;
    }

    protected List<Uri> getObservedUris() {
        return null;
    }

    protected boolean getPowerState() {
        return Settings.System.getInt(sContext.getContentResolver(),
                ExtraSettings.System.POWERMANAGER_MODE_ON, 0) == 1;
    }

    public void bindView(TextView v) {
        mView = v;
        update();
    }

    protected void updateView() {
        mView.setTextColor(mTextColor);
        if (mLabelString != null) {
            mView.setText(mLabelString);
        }else if (mLabel != 0) {
            mView.setText(mLabel);
        } else {
            mView.setText("");
        }
        for (Drawable d : mView.getCompoundDrawables()) {
            if (null != d) {
                d.setCallback(null);
            }
        }

        mView.setCompoundDrawablesWithIntrinsicBounds(0, mIcon, 0, 0);
    }
}

/**
 * @author: Woody Guo <guozhenjiang@ndoo.net>
 * @description: A subclass must provide a constructor to set mIcon and mType,
 *               and implement the method onClick
 */
abstract class StatelessButton extends SwitchButton {
    protected abstract void onClick();

    StatelessButton() {
        super();
        mState = STATE_ENABLED;
    }

    @Override
    protected void updateState() {
    }

    @Override
    protected void toggleState() {
        onClick();
    }
}

/**
 * @author: Woody Guo <guozhenjiang@ndoo.net>
 * @description: Base class of statefull buttons which observe system settings
 */
abstract class ObserveButton extends SwitchButton {
    protected final List<Uri> mObservedUris;

    ObserveButton() {
        super();
        mObservedUris = new ArrayList<Uri>();
    }

    @Override
    protected void onChangeUri(Uri uri) {
        Log.d(TAG, "uri changed : " + uri);
        update();
    }

    @Override
    protected List<Uri> getObservedUris() {
        return mObservedUris;
    }
}

/**
 * @author: Woody Guo <guozhenjiang@ndoo.net>
 * @description: Base class of statefull buttons which receive broadcasts
 */
abstract class ReceiverButton extends SwitchButton {
    protected StateTracker mStateTracker;
    protected IntentFilter mFilter;

    ReceiverButton() {
        super();
        mFilter = new IntentFilter();
        mStateTracker = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null != mStateTracker) {
            mStateTracker.onActualStateChange(context, intent);
        }
        update();
    }

    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        return mFilter;
    }
}

/**
 * The state machine for Wifi and Bluetooth toggling, tracking reality versus
 * the user's intent. This is necessary because reality moves relatively slowly
 * (turning on &amp; off radio drivers), compared to user's expectations.
 */
abstract class StateTracker {
    // Is the state in the process of changing?
    private boolean mInTransition = false;
    /**
     * Actually make the desired change to the underlying radio API.
     */
    private Boolean mActualState = null; // initially not set

    private Boolean mIntendedState = null; // initially not set

    // Did a toggle request arrive while a state update was
    // already in-flight? If so, the mIntendedState needs to be
    // requested when the other one is done, unless we happened to
    // arrive at that state already.
    private boolean mDeferredStateChangeRequestNeeded = false;

    /**
     * User pressed a button to change the state. Something should immediately
     * appear to the user afterwards, even if we effectively do nothing. Their
     * press must be heard.
     */
    public final void toggleState(Context context) {
        int currentState = getTriState(context);
        boolean newState = false;
        switch (currentState) {
            case SwitchButton.STATE_ENABLED:
                newState = false;
                break; // ADDED BY luokairong s

            case SwitchButton.STATE_DISABLED:
                newState = true;
                break;
            case SwitchButton.STATE_INTERMEDIATE:
                if (mIntendedState != null) {
                    newState = !mIntendedState;
                }
                break;
        }
        mIntendedState = newState;
        if (mInTransition) {
            // We don't send off a transition request if we're
            // already transitioning. Makes our state tracking
            // easier, and is probably nicer on lower levels.
            // (even though they should be able to take it...)
            mDeferredStateChangeRequestNeeded = true;
        } else {
            mInTransition = true;
            requestStateChange(context, newState);
        }
    }

    /**
     * Update internal state from a broadcast state change.
     */
    public abstract void onActualStateChange(Context context, Intent intent);

    /**
     * Sets the value that we're now in. To be called from onActualStateChange.
     * 
     * @param newState one of STATE_DISABLED, STATE_ENABLED, STATE_TURNING_ON,
     *            STATE_TURNING_OFF, STATE_UNKNOWN
     */
    protected final void setCurrentState(Context context, int newState) {
        final boolean wasInTransition = mInTransition;
        switch (newState) {
            case SwitchButton.STATE_DISABLED:
                mInTransition = false;
                mActualState = false;
                break;
            case SwitchButton.STATE_ENABLED:
                mInTransition = false;
                mActualState = true;
                break;
            case SwitchButton.STATE_TURNING_ON:
                mInTransition = true;
                mActualState = false;
                break;
            case SwitchButton.STATE_TURNING_OFF:
                mInTransition = true;
                mActualState = true;
                break;
        }

        if (wasInTransition && !mInTransition) {
            if (mDeferredStateChangeRequestNeeded) {
                if (mActualState != null && mIntendedState != null
                        && mIntendedState.equals(mActualState)) {
                } else if (mIntendedState != null) {
                    mInTransition = true;
                    requestStateChange(context, mIntendedState);
                }
                mDeferredStateChangeRequestNeeded = false;
            }
        }
    }

    /**
     * If we're in a transition mode, this returns true if we're transitioning
     * towards being enabled.
     */
    public final boolean isTurningOn() {
        return mIntendedState != null && mIntendedState;
    }

    /**
     * Returns simplified 3-state value from underlying 5-state.
     * 
     * @param context
     * @return STATE_ENABLED, STATE_DISABLED, or STATE_INTERMEDIATE
     */
    public final int getTriState(Context context) {
        /*
         * if (mInTransition) { // If we know we just got a toggle request
         * recently // (which set mInTransition), don't even ask the //
         * underlying interface for its state. We know we're // changing. This
         * avoids blocking the UI thread // during UI refresh post-toggle if the
         * underlying // service state accessor has coarse locking on its //
         * state (to be fixed separately). return
         * SwitchButton.STATE_INTERMEDIATE; }
         */
        switch (getActualState(context)) {
            case SwitchButton.STATE_DISABLED:
                return SwitchButton.STATE_DISABLED;
            case SwitchButton.STATE_ENABLED:
                return SwitchButton.STATE_ENABLED;
            default:
                return SwitchButton.STATE_INTERMEDIATE;
        }
    }

    /**
     * Gets underlying actual state.
     * 
     * @param context
     * @return STATE_ENABLED, STATE_DISABLED, STATE_ENABLING, STATE_DISABLING,
     *         or or STATE_UNKNOWN.
     */
    public abstract int getActualState(Context context);

    /**
     * Actually make the desired change to the underlying radio API.
     */
    protected abstract void requestStateChange(Context context,
            boolean desiredState);
}
