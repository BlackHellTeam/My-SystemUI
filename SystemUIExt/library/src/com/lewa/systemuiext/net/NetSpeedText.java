package com.lewa.systemuiext.net;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import lewa.provider.ExtraSettings;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import com.android.internal.telephony.TelephonyIntents;

public class NetSpeedText extends TextView{
    
    private static final int NET_SPEED_INTERVAL_SEC = 2;
    private static final int NET_SPEED_INTERVAL = NET_SPEED_INTERVAL_SEC * 1000;
    private NetSpeedReceiver mReceiver = null;
    private boolean mConnectivity = false;
    private boolean mShowNetSpeed = false;
    public static final long KB_IN_BYTES = 1024;
    public static final long MB_IN_BYTES = KB_IN_BYTES * 1024;
    public static final long GB_IN_BYTES = MB_IN_BYTES * 1024;
    public static final long TB_IN_BYTES = GB_IN_BYTES * 1024;
    private Handler mHandler = new Handler();
    public static final String KEY_STATUS_BAR_NET_SPEED = ExtraSettings.System.STATUS_BAR_NET_SPEED;
    private static final String TAG = "NetSpeedText";
    private long mRx = 0L;
    private boolean DEBUG_ALWAYS_SHOW = false;
    private Context mContext;
    private static ConnectivityManager mConnManager;
    public NetSpeedText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        (new SettingsObserver(mHandler)).observe();
        mReceiver = new NetSpeedReceiver();
        mConnManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private class NetSpeedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
                    || WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)
                    ||TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED.equals(action)){
                Log.d(TAG, "isNetworkConnected()"
                        + isNetworkConnected(context));
                updateSettings();
                setNetSpeedVisibility(isNetworkConnected(context)&&mShowNetSpeed);
                /*
                 * mConnectivity = !intent.getBooleanExtra(
                 * ConnectivityManager.EXTRA_NO_CONNECTIVITY, false); if
                 * (mShowNetSpeed) { setNetSpeedVisibility(mConnectivity); }
                 */
            }
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    KEY_STATUS_BAR_NET_SPEED), false, this);
            onChange(false);      
        }

        @Override
        public void onChange(boolean selfChange) {
            updateNetSpeed();
        }
    }

    private void setNetSpeedVisibility(boolean visible){
        if(DEBUG_ALWAYS_SHOW) {
            setText("100KB/S");
      	}
        Log.d(TAG, "visible " + visible);
        setVisibility(visible || DEBUG_ALWAYS_SHOW ? View.VISIBLE : View.GONE);
    }

    private void updateNetSpeed() {
        updateSettings();
        Log.d(TAG, "updateNetSpeed " + mShowNetSpeed);
        if (mShowNetSpeed) {
            setNetSpeedVisibility(isNetworkConnected(mContext));
            mHandler.postDelayed(mNetSpeedTasks, NET_SPEED_INTERVAL);
        } else {
            setNetSpeedVisibility(false);
        }
    }

    private Runnable mNetSpeedTasks = new Runnable() {
        @Override
        public void run() {
            long rx = TrafficStats.getTotalRxBytes();
            Log.d(TAG, "rx " + rx);
            setText(formatShorterSize(rx <= mRx ? 0 : (rx - mRx) / NET_SPEED_INTERVAL_SEC, "%1.2f"));
            mRx = rx;
            if(mShowNetSpeed)
                mHandler.postDelayed(mNetSpeedTasks, NET_SPEED_INTERVAL);
        }
    };

    public static String formatShorterSize(long size, String format){
        String str;
        Log.d(TAG, "size " + size);
        if( size < KB_IN_BYTES)
            str = size + "B/s";
        else if( size < MB_IN_BYTES)
            str = String.format(format, (float)size / KB_IN_BYTES) + "K/s";
        else if( size < GB_IN_BYTES)
            str = String.format(format, (float)size / MB_IN_BYTES) + "M/s";
        else if( size < TB_IN_BYTES)
            str = String.format(format, (float)size / GB_IN_BYTES) + "G/s";
        else
            str = String.format(format, (float)size / TB_IN_BYTES) + "T/s";
        return str;
    }

    private static boolean isNetworkConnected(Context context) {
        if (mConnManager != null) {
            NetworkInfo info = mConnManager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
        return false;
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();
        mShowNetSpeed = (Settings.System.getInt(resolver,
        KEY_STATUS_BAR_NET_SPEED, 0) == 1);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mContext.registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(mReceiver);
    }
}
