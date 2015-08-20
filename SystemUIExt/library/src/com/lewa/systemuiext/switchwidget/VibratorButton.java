
package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.provider.Settings;
import android.database.ContentObserver;
import android.os.Handler;

public class VibratorButton extends ObserveButton {

    private AudioManager mAudioManager;
    private static final String TAG = "VibratorButton";

    public VibratorButton() {
        super();
        mAudioManager = (AudioManager) sContext
                .getSystemService(Context.AUDIO_SERVICE);
        mObservedUris.add(Settings.System.getUriFor(Settings.System.VIBRATE_WHEN_RINGING));
        mLabel = R.string.title_ringer_vibrator;
        mButtonName = R.string.title_ringer_vibrator;
        mSettingsIcon = R.drawable.btn_setting_vibrate;
    }

    @Override
    protected void updateState() {
        if (getVibratorMode()) {
            mIcon = R.drawable.stat_vibrate_on;
            mState = STATE_ENABLED;
            mTextColor = sEnabledColor;
        } else {
            mIcon = R.drawable.stat_vibrate_off;
            mState = STATE_DISABLED;
            mTextColor = sDisabledColor;
        }
    }

    @Override
    protected void toggleState() {
        final int isEnabled = getVibratorMode() ? 0 : 1;
        new Thread() {
            @Override
            public void run() {
                Settings.System.putInt(sContext.getContentResolver(),
                        Settings.System.VIBRATE_WHEN_RINGING, isEnabled);
            }
        }.start();
    }

    private boolean getVibratorMode() {
        int mode = Settings.System.getInt(sContext.getContentResolver(),
                Settings.System.VIBRATE_WHEN_RINGING, 0);
        return mode != 0;
    }

    @Override
    protected boolean onLongClick() {
        Intent in = new Intent("android.settings.NOTIFICATION_SETTINGS");
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        return true;
    }
}
