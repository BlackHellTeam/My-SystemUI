
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

import com.lewa.systemuiext.R;

public class WifiButton extends ReceiverButton
{
    WifiManager wm;

    public WifiButton() {
        super();
        mStateTracker = new WifiStateTracker();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mLabel = R.string.title_toggle_wifi;
        mButtonName = R.string.title_toggle_wifi;
        mSettingsIcon = R.drawable.btn_setting_wifi;
        IntentFilter iflter = new IntentFilter();
        iflter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        iflter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        sContext.registerReceiver(mReceiver, iflter);
        updateWifiSSID();
    }

    private static final class WifiStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
            WifiManager wm = (WifiManager)
                    context.getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                return wifiStateToFiveState(wm.getWifiState());
            }
            return STATE_UNKNOWN;
        }

        @Override
        protected void requestStateChange(Context context,
                final boolean desiredState) {
            final WifiManager wm = (WifiManager)
                    context.getSystemService(Context.WIFI_SERVICE);
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
                    int wifiApState = wm.getWifiApState();
                    if (desiredState && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING)
                            || (wifiApState == wm.WIFI_AP_STATE_ENABLED))) {
                        wm.setWifiApEnabled(null, false);
                    }
                    wm.setWifiEnabled(desiredState);
                    return null;
                }
            }.execute();
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (!WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                return;
            }
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            int widgetState = wifiStateToFiveState(wifiState);
            setCurrentState(context, widgetState);
        }

        /**
         * Converts WifiManager's state values into our Wifi/Bluetooth-common
         * state values.
         */
        private static int wifiStateToFiveState(int wifiState) {
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    return STATE_DISABLED;
                case WifiManager.WIFI_STATE_ENABLED:
                    return STATE_ENABLED;
                case WifiManager.WIFI_STATE_DISABLING:
                    return STATE_TURNING_OFF;
                case WifiManager.WIFI_STATE_ENABLING:
                    return STATE_TURNING_ON;
                default:
                    return STATE_UNKNOWN;
            }
        }
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
                    || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

                ConnectivityManager connectivityManager = (ConnectivityManager) sContext.
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()
                        && info.getTypeName().equalsIgnoreCase("wifi")) {
                    updateWifiSSID();
                } else {
                    //modified for bug:#65955
                    if (mView != null) {
                        mView.setText(R.string.title_toggle_wifi);
                        mLabelString = mView.getText().toString();
                    }
                }
               mStateTracker.onActualStateChange(context,intent);
            }
        }
    };

    public void updateWifiSSID() {
        WifiManager wm = (WifiManager)
                sContext.getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            WifiInfo mWifiInfo = wm.getConnectionInfo();
            if (mWifiInfo != null) {
                if (mView != null) {
                    if (!TextUtils.isEmpty(mWifiInfo.getSSID()) &&
                            !mWifiInfo.getSSID().equalsIgnoreCase("<unknown ssid>") &&
                            !mWifiInfo.getSSID().equalsIgnoreCase("0x")) {
                        mLabelString = mWifiInfo.getSSID().toString();
                        mView.setText(mWifiInfo.getSSID().toString());
                    } else {
                        mView.setText(R.string.title_toggle_wifi);
                        mLabelString = mView.getText().toString();
                    }
                }
            }
        }
    }

    @Override
    protected void updateState() {
        mState = mStateTracker.getTriState(sContext);
        switch (mState) {
            case STATE_DISABLED:
                mIcon = R.drawable.stat_wifi_off;
                mTextColor = sDisabledColor;
                break;
            case STATE_ENABLED:
                mIcon = R.drawable.stat_wifi_on;
                mTextColor = sEnabledColor;
                updateWifiSSID();
                break;
            case STATE_INTERMEDIATE:
                mIcon = R.drawable.switch_wifi_inter;
                break;
        }
    }

    @Override
    protected void toggleState() {
        final WifiManager wm = (WifiManager)sContext.getSystemService(Context.WIFI_SERVICE);
        int apState = wm.getWifiApState();
        int wifiState = wm.getWifiState();
        if (apState == WifiManager.WIFI_AP_STATE_ENABLING
            || apState == WifiManager.WIFI_AP_STATE_DISABLING) {
            return;
        } else if (apState == WifiManager.WIFI_AP_STATE_ENABLED 
            || wifiState == WifiManager.WIFI_STATE_DISABLED){
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    wm.setWifiApEnabled(null,false);
                    wm.setWifiEnabled(true);
                    return null;
                }
            }.execute();
        }
        mStateTracker.toggleState(sContext);
    }

    @Override
    protected boolean onLongClick() {
        startActivity("android.settings.WIFI_SETTINGS");
        return true;
    }
}
