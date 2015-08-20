package com.lewa.systemuiext.switchwidget;

import android.app.StatusBarManager;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.android.internal.telephony.TelephonyIntents;
import com.lewa.systemuiext.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import lewa.telephony.LewaSimInfo;
import lewa.provider.ExtraSettings;
import android.telephony.SubscriptionManager;
import lewa.telephony.TelephonyHelper;
import android.telephony.TelephonyManager;
import android.content.BroadcastReceiver;
import com.android.internal.telephony.IccCardConstants;
///LEWA BEGIN
/***
 * @author zhangxianjia
 * @author zhangbo
 * @author juude.song@gmail.com
 * @description toggle mobile data TODO: make it platform-independent
 */

public class DataButton extends ObserveButton {

    public static String TAG = DataButton.class.getSimpleName();

    public static boolean STATE_CHANGE_REQUEST = false;

    private TelephonyManager mTelephonyManager;
    public static final boolean DEBUG = true;

    public DataButton() {
        super();
        ContentResolver resolver = sContext.getContentResolver();
        mTelephonyManager = TelephonyManager.from(sContext);
        try {
            Cursor settingsCursor = sContext
                    .getContentResolver()
                    .query(Settings.Secure.CONTENT_URI,
                            null,
                            "(" + Settings.System.NAME + "=?)",
                            new String[] { Settings.Secure.LOCATION_PROVIDERS_ALLOWED },
                            null);
            mContentQueryMap = new ContentQueryMap(settingsCursor,
                    Settings.System.NAME, true, null);
            mContentQueryMap.addObserver(new SettingsObserver());
            if (settingsCursor != null) {
                settingsCursor.close();
                settingsCursor = null;
            }
            mObservedUris.add(Settings.Global
                    .getUriFor(Settings.Global.AIRPLANE_MODE_ON));
            if (TelephonyHelper.supportMultipleSims()) {
                mObservedUris.add(Settings.Global
                        .getUriFor(Settings.Global.MOBILE_DATA + 0));
                mObservedUris.add(Settings.Global
                        .getUriFor(Settings.Global.MOBILE_DATA + 1));
                /*
                 * resolver.registerContentObserver(Settings.Global
                 * .getUriFor(Settings.Global.MOBILE_DATA + 0), true,
                 * mMobileStateChangeObserver);
                 * resolver.registerContentObserver(Settings.Global
                 * .getUriFor(Settings.Global.MOBILE_DATA + 1), true,
                 * mMobileStateChangeObserver);
                 */
            } else {
                mObservedUris.add(Settings.Global
                        .getUriFor(Settings.Global.MOBILE_DATA));
                /*
                 * resolver.registerContentObserver(
                 * Settings.Global.getUriFor(Settings.Global.MOBILE_DATA), true,
                 * mMobileStateChangeObserver);
                 */
            }
            /*
             * resolver.registerContentObserver(
             * Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON),
             * true, mMobileStateChangeObserver);
             */
            // add for sync phone setting and systemUI end
        } catch (Exception ex) {

        }
        mLabel = R.string.title_toggle_mobiledata;
        mButtonName = R.string.title_toggle_mobiledata;
        mSettingsIcon = R.drawable.btn_setting_data;
        IntentFilter iflter = new IntentFilter();
        iflter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        sContext.registerReceiver(mSimReceiver, iflter);
    }

    // add for sync phone setting and systemUI begin
    /*
     * private ContentObserver mMobileStateChangeObserver = new
     * ContentObserver(new Handler()) {
     * 
     * @Override public void onChange(boolean selfChange) { { boolean enabled =
     * getDataState(); if(DEBUG) Log.d(TAG, "onReceive,data==" + enabled);
     * ///LEWA BEGIN if (getAirplaneMode()) { mIcon = R.drawable.stat_data_off;
     * mState = STATE_DISABLED; } else if (enabled) { ///LEWA END mIcon =
     * R.drawable.stat_data_on; mState = STATE_ENABLED; } else { mIcon =
     * R.drawable.stat_data_off; mState = STATE_DISABLED; } updateView(); } } };
     */

    @Override
    public void updateView() {
        // if (DEBUG)
        //     Log.d(TAG, "state = " + mState);
        mTextColor = (mState == STATE_ENABLED) ? sEnabledColor : sDisabledColor;
        super.updateView();
    }

    @Override
    protected void updateState() {
        long simId = SubscriptionManager.getDefaultDataSubId();
        if (DEBUG) {
            Log.d(TAG, "updateState to " + getDataState());
            Log.d(TAG, "simId " + simId);
        }
        // /LEWA BEGIN
        if (getAirplaneMode()) {
            mIcon = R.drawable.stat_data_off;
            mState = STATE_DISABLED;
        } else if (STATE_CHANGE_REQUEST) {
            // /LEWA END
            mIcon = R.drawable.stat_data_on;
            mState = STATE_INTERMEDIATE;
        } else if (getDataState()
                && simId > ExtraSettings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
            mIcon = R.drawable.stat_data_on;
            mState = STATE_ENABLED;
        } else {
            mIcon = R.drawable.stat_data_off;
            mState = STATE_DISABLED;
        }
    }

    @Override
    protected void toggleState() {
        // /LEWA BEGIN
        int simCount = LewaSimInfo.getInsertedSimCount(mView.getContext());
        if (simCount == 0 || getAirplaneMode()) {
            mIcon = R.drawable.stat_data_off;
            mState = STATE_DISABLED;
            updateView();
            return;
        }
        // /LEWA END // zhanghui
        final boolean enabled = getDataState();
        if (DEBUG)
            Log.d(TAG, "data==" + enabled);
        final ConnectivityManager cm = (ConnectivityManager) sContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final boolean newState = !enabled;
        /* collapse statusbar */
        new Thread() {
            @Override
            public void run() {
                if (TelephonyHelper.supportMultipleSims()) {
                    int phoneId = SubscriptionManager.getDefaultDataPhoneId();
                    if (DEBUG)
                        Log.d(TAG, "Set Data  for phoneId-" + phoneId + " is "
                                + newState);
                    Settings.Global.putInt(sContext.getContentResolver(),
                            Settings.Global.MOBILE_DATA + phoneId, newState ? 1
                                    : 0);
                    long[] subId = SubscriptionManager.getSubId(phoneId);
                    if (subId != null) {
                        mTelephonyManager.setDataEnabledUsingSubId(subId[0],
                                !enabled);
                    }
                } else {
                    Settings.Global.putInt(sContext.getContentResolver(),
                            Settings.Global.MOBILE_DATA, newState ? 1 : 0);
                }
            }
        }.start();
    }

    @Override
    protected boolean onLongClick() {
        int simCount = LewaSimInfo.getInsertedSimCount(mView.getContext());
        if (simCount > 0) {
            Intent intent = new Intent();
            intent.setClassName("com.android.phone",
                    "com.android.phone.SelectSubscription");
            intent.putExtra("PACKAGE", "com.android.phone");
            intent.putExtra("TARGET_CLASS",
                    "com.android.phone.MSimMobileNetworkSubSettings");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }
    private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String stateExtra = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
            if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)){
                updateState();
                updateView();
            }
        }
    };
    // /LEWA BEGIN
    // TODO:can we remove it?
    private boolean getAirplaneMode() {
        ContentResolver cr = sContext.getContentResolver();
        return 0 != Settings.Global.getInt(cr,
                Settings.Global.AIRPLANE_MODE_ON, 0);
    }

    // /LEWA END
    private boolean getDataState() {
        ConnectivityManager cm = (ConnectivityManager) sContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        ContentResolver cr = sContext.getContentResolver();
        if (TelephonyHelper.supportMultipleSims()) {
            int phoneId = SubscriptionManager.getDefaultDataPhoneId();
            if (DEBUG)
                Log.d(TAG, "get Data  for phoneId-" + phoneId);
            return Settings.Global.getInt(cr, Settings.Global.MOBILE_DATA
                    + phoneId, 0) == 1;
        } else {
            return Settings.Global.getInt(cr, Settings.Global.MOBILE_DATA, 0) == 1;
        }
    }

    private final class SettingsObserver implements Observer {

        public void update(Observable o, Object arg) {
            if (DEBUG)
                Log.d(TAG, "dataChanged,data==");
            DataButton.this.update();
        }
    }

    private ContentQueryMap mContentQueryMap = null;

    @Override
    protected void onChangeUri(Uri uri) {
        if (DEBUG)
            Log.d(TAG, "uri changed");
        super.update();
    }
}
