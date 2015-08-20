
package com.lewa.systemuiext.switchwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.content.ContentResolver;
import android.provider.Settings;

import com.lewa.systemuiext.R;

public class WifiApButton extends ReceiverButton
{
    private WifiManager wm;
    private boolean mWaitForWifiStateChange = false;
    private ContentResolver mContentResolver;

    public WifiApButton() {
        super();
        mStateTracker = new WifiApStateTracker();
        mFilter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mContentResolver = sContext.getContentResolver();
        mLabel = R.string.title_toggle_wifiap;
        mButtonName = R.string.title_toggle_wifiap;
        mSettingsIcon = R.drawable.btn_setting_wifi;
        wm = (WifiManager) sContext.getSystemService(Context.WIFI_SERVICE);
    }

    private final class WifiApStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
            if (wm != null) {
                return wifiApStateToFiveState(wm.getWifiApState());
            }
            return STATE_UNKNOWN;
        }

        @Override
        protected void requestStateChange(Context context,
                final boolean desiredState) {
            final WifiManager wm = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            if (wm == null) {
                return;
            }

            // Actually request the Wifi change and persistent
            // settings write off the UI thread, as it can take a
            // user-noticeable amount of time, especially if there's
            // disk contention.
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    // Disable tethering if enabling Wifi
                    // wm.setWifiApEnabled(null, desiredState);
                    setWifiButtonEnabled(desiredState);
                    return null;
                }
            }.execute();
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                disableWifiApButton();
            } else if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(intent
                    .getAction())) {
                int wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_AP_STATE,
                        WifiManager.WIFI_AP_STATE_FAILED);
                int widgetState = wifiApStateToFiveState(wifiState);
                setCurrentState(context, widgetState);
            }
        }

        /**
         * Converts WifiManager's state values into our Wifi/Bluetooth-common
         * state values.
         */
        private int wifiApStateToFiveState(int wifiState) {
            switch (wifiState) {
            case WifiManager.WIFI_AP_STATE_ENABLING:
                return STATE_TURNING_ON;
            case WifiManager.WIFI_AP_STATE_ENABLED:
                return STATE_ENABLED;
            case WifiManager.WIFI_AP_STATE_DISABLING:
                return STATE_TURNING_OFF;
            case WifiManager.WIFI_AP_STATE_DISABLED:
                return STATE_DISABLED;
            default:
                return STATE_UNKNOWN;
            }
        }
    }


    // private BroadcastReceiver mReceiver = new BroadcastReceiver() {

    //     @Override
    //     public void onReceive(Context context, Intent intent) {
    //         mStateTracker.onActualStateChange(context, intent);
    //     }
    // };

    @Override
    protected void updateState() {
        mState = mStateTracker.getTriState(sContext);
        switch (mState) {
        case STATE_DISABLED:
            mIcon = R.drawable.wifiap_off;
            mTextColor = sDisabledColor;
            break;
        case STATE_ENABLED:
            mIcon = R.drawable.wifiap_on;
            mTextColor = sEnabledColor;
            break;
        case STATE_INTERMEDIATE:
            mIcon = R.drawable.wifiap_on;
            break;
        }
    }

    private void disableWifiApButton() {
        if (getAirplaneMode()) {
            mIcon = R.drawable.wifiap_off;
            mTextColor = sDisabledColor;
            mState = STATE_DISABLED;
            updateView();
            return;
        }
    }
    @Override
    protected void toggleState() {
        int wifiState = wm.getWifiState();
        if (getAirplaneMode()) {
            return;
        } else if (wifiState == WifiManager.WIFI_STATE_ENABLING 
            || wifiState == WifiManager.WIFI_STATE_DISABLING) {
            return;
        } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    setWifiButtonEnabled(true);
                    return null;
                }
            }.execute();
        }
        mStateTracker.toggleState(sContext);
    }

    private boolean getAirplaneMode() {
        ContentResolver cr = sContext.getContentResolver();
        return 0 != Settings.Global.getInt(cr,
                Settings.Global.AIRPLANE_MODE_ON, 0);
    }

    @Override
    protected boolean onLongClick() {
        startActivity("android.settings.TETHER_SETTINGS");
        return true;
    }

    private void setWifiButtonEnabled(boolean enable) {
        int wifiSavedState = 0;
        int wifiState = wm.getWifiState();
        if (enable
                && ((wifiState == WifiManager.WIFI_STATE_ENABLING) || (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
            wm.setWifiEnabled(false);
            Settings.Global.putInt(mContentResolver,
                    Settings.Global.WIFI_SAVED_STATE, 1);
        }
        if (!enable) {
            try {
                wifiSavedState = Settings.Global.getInt(mContentResolver,
                        Settings.Global.WIFI_SAVED_STATE);
            } catch (Settings.SettingNotFoundException e) {
                ;
            }

            if (wifiSavedState == 1) {
                mWaitForWifiStateChange = true;
            }
        }
        wm.setWifiApEnabled(null, enable);

        if (!enable) {
            if (wifiSavedState == 1) {
                wm.setWifiEnabled(true);
                Settings.Global.putInt(mContentResolver,
                        Settings.Global.WIFI_SAVED_STATE, 0);
            }
        }
    }
}
