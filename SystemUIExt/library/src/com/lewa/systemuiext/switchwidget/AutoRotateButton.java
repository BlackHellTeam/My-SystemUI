
package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;

import android.provider.Settings;

public class AutoRotateButton extends ObserveButton
{
    public AutoRotateButton() {
        super();
        mObservedUris.add(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION));
        mLabel = R.string.title_toggle_autorotate;
        mButtonName = R.string.title_toggle_autorotate;
        mSettingsIcon = R.drawable.btn_setting_rotate;
    }

    @Override
    protected void updateState() {
        if (getOrientationState() == 1) {
            mIcon = R.drawable.stat_orientation_on;
            mTextColor = sEnabledColor;
        } else {
            mIcon = R.drawable.stat_orientation_off;
            mTextColor = sDisabledColor;
        }
    }

    @Override
    protected void toggleState() {
        if (getOrientationState() == 0) {
            Settings.System.putInt(
                    sContext.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION, 1);
        } else {
            Settings.System.putInt(
                    sContext.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION, 0);
        }
    }

    @Override
    protected boolean onLongClick() {
        startActivity("android.settings.DISPLAY_SETTINGS");
        return false;
    }

    private int getOrientationState() {
        return Settings.System.getInt(
                sContext.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0);
    }
}
