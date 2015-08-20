
package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;
import android.database.ContentObserver;
import android.os.Handler;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.os.Vibrator;
import android.os.Handler;
import android.media.AudioAttributes;

public class SoundButton extends ReceiverButton {

    private AudioManager mAudioManager;
    private static final int PHONE_SLIENT = 0;
    private static final int PHONE_RING = 1;
    private static final int PHONE_VIBRATE = 2;
    private static final int VIBRATE_DURATION = 300;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .build();
    private int mPhoneRingMode;
    private Vibrator mVibrator;

    public SoundButton() {
        super();
        mFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        mLabel = R.string.title_toggle_sound;
        mButtonName = R.string.title_toggle_sound;
        mSettingsIcon = R.drawable.btn_setting_sound;
        mAudioManager = (AudioManager) sContext.getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) sContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void updateState() {
    int value = getPhoneRingMode();
    switch(value) {
        case PHONE_SLIENT:
            mIcon = R.drawable.stat_silent_on;
            mState = STATE_DISABLED;
            mTextColor = sEnabledColor;
            mLabel = R.string.title_toggle_sound;
        break;
        case PHONE_VIBRATE:
            mIcon = R.drawable.stat_slient_vibrate_on;
            mState = STATE_DISABLED;
            mTextColor = sEnabledColor;
            mLabel = R.string.title_toggle_vibrator;
        break;
        case PHONE_RING:
            mIcon = R.drawable.stat_silent_off;
            mState = STATE_ENABLED;
            mTextColor = sDisabledColor;
            mLabel = R.string.title_toggle_sound;
        break;
        default:
            mIcon = R.drawable.stat_silent_off;
            mState = STATE_ENABLED;
            mTextColor = sDisabledColor;
            mLabel = R.string.title_toggle_sound;
        break;
    }
    }
    @Override
    protected void toggleState() {
        new Thread() {
            @Override
            public void run() {
                setPhoneRingClickMode();
            }
        }.start();
    }

    private boolean getVibratorMode() {
        int mode = Settings.System.getInt(sContext.getContentResolver(),
                Settings.System.VIBRATE_WHEN_RINGING, 0);
        return mode != 0;
    }

    private boolean getZenMode(){
        final int mode = Settings.Global.getInt(sContext.getContentResolver(),
                Settings.Global.ZEN_MODE, Settings.Global.ZEN_MODE_OFF);
        return mode != Settings.Global.ZEN_MODE_OFF;
    }

    private void setRingerModel() {
        if (getVibratorMode() && !getZenMode()) {
            Settings.System.putInt(sContext.getContentResolver(),
                 Settings.System.VIBRATE_WHEN_RINGING, 0);
            if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL,false);
            }
        }
    }

private int getPhoneRingMode() {
    if (mAudioManager != null) {
        int value = mAudioManager.getRingerMode();
        int phoneState = PHONE_RING;
        switch(value) {
            case AudioManager.RINGER_MODE_SILENT:
            phoneState = PHONE_SLIENT;
            break;
            case AudioManager.RINGER_MODE_VIBRATE:
            phoneState = PHONE_VIBRATE;
            break;
            case AudioManager.RINGER_MODE_NORMAL:
            phoneState =  PHONE_RING;
            break;
            default:
            break;
        }
        return phoneState;
    }
    return PHONE_RING;
}

private void setPhoneRingClickMode() {
    int value = getPhoneRingMode();
    switch(value) {
        case PHONE_SLIENT:
        setPhoneRingMode(PHONE_VIBRATE);
        break;
        case PHONE_VIBRATE:
        setPhoneRingMode(PHONE_RING);
        break;
        case PHONE_RING:
        setPhoneRingMode(PHONE_SLIENT);
        break;
        default:
        setPhoneRingMode(PHONE_RING);
        break;
    }
}

private void setPhoneSlient() {
    if (mAudioManager != null) {
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT,true);
    }
}

private void setPhoneVibrate() {
    if (mAudioManager != null) {
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE,true);
        onVibrate();
    }
}

private void setPhoneNormal() {
    if (mAudioManager != null) {
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL,true);
    }
}
    private void setPhoneRingMode(int value) {
        switch(value) {
            case PHONE_SLIENT:
            setPhoneSlient();
            break;
            case PHONE_RING:
            setPhoneNormal();
            break;
            case PHONE_VIBRATE:
            setPhoneVibrate();
            break;
            default:
            setPhoneNormal();
            break;
        }
    }

    private void onVibrate() {
        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE) {
            return;
        }
        mVibrator.vibrate(VIBRATE_DURATION, VIBRATION_ATTRIBUTES);
    }


    @Override
    protected boolean onLongClick() {
        Intent in = new Intent("android.settings.NOTIFICATION_SETTINGS");
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        return true;
    }
}
