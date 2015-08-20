
package com.lewa.systemuiext.switchwidget;

import lewa.provider.ExtraSettings;

import android.content.ContentResolver;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;

import com.lewa.systemuiext.R;

public class BrightnessButton extends ObserveButton
{
    // Auto-backlight level
    private static final int AUTO_BACKLIGHT = -1;
    private static final int MAN_MADE_BACKLIGHT = -2;
    // whether or not backlight is supported
    private static Boolean SUPPORTS_AUTO_BACKLIGHT = null;

    // CM modes of operation
    private static final int CM_MODE_AUTO_MIN_DEF_MAX = 0;

    public BrightnessButton() {
        super();
        mObservedUris.add(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE));
        mObservedUris.add(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS));
        mLabel = R.string.title_toggle_brightness;
        mButtonName = R.string.title_toggle_brightness;
        //NO Settings button here to make brightness button stable
    }

    @Override
    protected void updateState() {
        if (isBrightnessSetToAutomatic()) {
            mIcon = R.drawable.stat_brightness_auto;
            mState = STATE_ENABLED;
        } else {
            switch (getBrightnessState()) {
                case STATE_DISABLED:
                    mIcon = R.drawable.stat_brightness_on;
                    mState = STATE_DISABLED;
                    break;
                default:
                    mIcon = R.drawable.stat_brightness_auto;
                    mState = STATE_ENABLED;
                    break;
            }
        }
    }

    @Override
    protected void toggleState() {
        IPowerManager ipm = IPowerManager.Stub.asInterface(
                ServiceManager.getService("power"));
        if (ipm != null) {
            int brightness = getNextBrightnessValue();
            ContentResolver resolver = sContext.getContentResolver();
            if (brightness == AUTO_BACKLIGHT) {
                Settings.System.putInt(resolver
                        , Settings.System.SCREEN_BRIGHTNESS_MODE
                        , Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            } else {
                if (isAutomaticModeSupported()) {
                    Settings.System.putInt(resolver
                            , Settings.System.SCREEN_BRIGHTNESS_MODE
                            , Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                }
                int nowBrightness = Settings.System.getInt(resolver
                        , Settings.System.SCREEN_BRIGHTNESS, brightness);
                com.lewa.systemuiext.adapter.SystemUIAdapter.setBrightness(ipm, nowBrightness);
            }
        }
        updateState();
    }

    @Override
    protected boolean onLongClick() {
        startActivity("android.settings.DISPLAY_SETTINGS");
        return true;
    }

    private int getNextBrightnessValue() {
        int brightness = Settings.System.getInt(
                sContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
        getCurrentCMMode();

        if (isAutomaticModeSupported() && isBrightnessSetToAutomatic()) {
            brightness = MAN_MADE_BACKLIGHT;
        } else {
            brightness = AUTO_BACKLIGHT;
        }

        return brightness;
    }

    private int getBrightnessState() {
        if (isAutomaticModeSupported() && isBrightnessSetToAutomatic()) {
            return STATE_ENABLED;
        } else {
            return STATE_DISABLED;
        }
    }

    private boolean isAutomaticModeSupported() {
       /* if(TextUtils.isEmpty(SystemProperties.get("ro.lewa.device"))) {
            return false;
        }*/
        if (SUPPORTS_AUTO_BACKLIGHT == null) {
            if (sContext.getResources().getBoolean(
                    lewa.R.bool.android_config_automatic_brightness_available)) {
                SUPPORTS_AUTO_BACKLIGHT = true;
            } else {
                SUPPORTS_AUTO_BACKLIGHT = false;
            }
        }

        return SUPPORTS_AUTO_BACKLIGHT;
    }

    private boolean isBrightnessSetToAutomatic() {
        try {
            IPowerManager ipm = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            if (ipm != null) {
                int brightnessMode = Settings.System.getInt(
                        sContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
                return brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            }
        } catch (Exception e) {
        }

        return false;
    }

    private int getCurrentCMMode() {
        return Settings.System.getInt(sContext.getContentResolver()
                , ExtraSettings.System.EXPANDED_BRIGHTNESS_MODE, CM_MODE_AUTO_MIN_DEF_MAX);
    }
}
