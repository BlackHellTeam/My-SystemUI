package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

public class ScreenWakeButton extends ObserveButton {

    public final String TAG = ScreenWakeButton.class.getSimpleName();
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;
    private static String SCREEN_HIGH_LIGHT_TIME = "statusbar_screen_lighttime";
    private static final String ACTION_STATUS_POWER_LIGHT = "com.lewa.action.POWER_LIGHT";
    private static final int SHOW_LIGHT_ICON = 1;
    private static final int CANCEL_LIGHT_ICON = 0;
    public ScreenWakeButton() {
        super();
        mObservedUris.add(Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT));
        mLabel = R.string.title_toggle_screen_wake;
        mButtonName = R.string.title_toggle_screen_wake;
        mSettingsIcon = R.drawable.stat_settings_highlight;
    }

    @Override
    protected void updateState() {
        if (getState()) {
            mIcon = R.drawable.stat_sys_highlight_on;
            mState = STATE_ENABLED;
            mTextColor = sEnabledColor;
            sendPowerIconCast(SHOW_LIGHT_ICON);
        } else {
            mIcon = R.drawable.stat_sys_highlight_off;
            mState = STATE_DISABLED;
            mTextColor = sDisabledColor;
            sendPowerIconCast(CANCEL_LIGHT_ICON);
        }
    }

    @Override
    protected void toggleState() {
        if (getState()) {
            putCurrentTime(getLastTime());
        } else {
            putLastTime(getCurrentTime());
            putCurrentTime(Integer.MAX_VALUE);
        }
        updateView();
    }

    @Override
    protected boolean onLongClick() {
        Intent in = new Intent("android.settings.DISPLAY_SETTINGS");
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        return true;
    }

    private boolean getState() {
        final long currentTimeout = Settings.System.getLong(sContext.getContentResolver(),
            SCREEN_OFF_TIMEOUT,FALLBACK_SCREEN_TIMEOUT_VALUE);
        if (Integer.MAX_VALUE == (int)currentTimeout) {
            return true;
        } else {
            return false;
        }
    }

    private static void sendPowerIconCast(int enabled) {
        Intent intent = new Intent(ACTION_STATUS_POWER_LIGHT);
        intent.putExtra("state", enabled);
        sContext.sendBroadcast(intent);
    }

    private long getCurrentTime() {
        final long currentTimeout = Settings.System.getLong(sContext.getContentResolver(),
            SCREEN_OFF_TIMEOUT,FALLBACK_SCREEN_TIMEOUT_VALUE);
        return currentTimeout;
    }

    private void putCurrentTime(long time) {
        Settings.System.putLong(sContext.getContentResolver(),SCREEN_OFF_TIMEOUT,time);
    }

    private void putLastTime(long time) {
        Settings.System.putLong(sContext.getContentResolver(),SCREEN_HIGH_LIGHT_TIME,time);
    }

    private long getLastTime() {
        final long currentTimeout = Settings.System.getLong(sContext.getContentResolver(),
            SCREEN_HIGH_LIGHT_TIME,FALLBACK_SCREEN_TIMEOUT_VALUE);
        return currentTimeout;
    }

}
