
package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;

import android.content.Context;
import android.util.Log;
import android.os.SystemClock;
import android.os.PowerManager;

public class LockButton extends StatelessButton {

    String TAG = LockButton.class.getSimpleName();
    private static Boolean LOCK_SCREEN_STATE = null;

    public LockButton() {
        super();
        mType = "LockButton";
        mIcon = R.drawable.stat_lockscreen;
        mLabel = R.string.title_toggle_locknow;
        mButtonName = R.string.title_toggle_locknow;
        mSettingsIcon = R.drawable.btn_setting_lock;
    }

    @Override
    protected void onClick() {
    }

    @Override
    protected boolean onLongClick() {
        startActivity("android.settings.SECURITY_SETTINGS");
        return false;
    }

    @Override
    protected void updateState() {
        getState();
        if (LOCK_SCREEN_STATE == null) {
            mIcon = R.drawable.stat_lockscreen;
            mState = STATE_INTERMEDIATE;
        }
        else if (LOCK_SCREEN_STATE) {
            mIcon = R.drawable.stat_lockscreen;
            mState = STATE_ENABLED;
        }
        else {
            mIcon = R.drawable.stat_lockscreen;
            mState = STATE_DISABLED;
        }
        Log.d(TAG, "updateState");
    }

    @Override
    protected void toggleState() {
        turnScreenOff();
        update();
        Log.d(TAG, "toggleState");
    }

    private void turnScreenOff() {
        final PowerManager pm = (PowerManager)
                sContext.getSystemService(Context.POWER_SERVICE);
        pm.goToSleep(SystemClock.uptimeMillis());
    }

    private static boolean getState() {
        if (LOCK_SCREEN_STATE == null) {
            LOCK_SCREEN_STATE = true;
        }
        return LOCK_SCREEN_STATE;
    }
}
