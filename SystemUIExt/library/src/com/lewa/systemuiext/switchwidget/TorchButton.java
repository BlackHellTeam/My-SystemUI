
package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;
import android.content.Intent;
import android.provider.Settings;
import android.hardware.Camera;
import lewa.content.ExtraIntent;
import lewa.provider.ExtraSettings;

public class TorchButton extends ObserveButton
{
    Camera camera;

    public TorchButton() {
        super();
        mObservedUris.add(Settings.System.getUriFor(ExtraSettings.System.TORCH_STATE));
        mLabel = R.string.title_toggle_flashlight;
        mIcon = R.drawable.stat_torch_off;
        mButtonName = R.string.title_toggle_flashlight;
        mSettingsIcon = R.drawable.btn_setting_flashlight;
    }

    @Override
    protected void updateState() {
        if (!getTorchState()) {
            mIcon = R.drawable.stat_torch_off;
            mState = STATE_DISABLED;
            mTextColor = sDisabledColor;
        } else {
            mIcon = R.drawable.stat_torch_on;
            mState = STATE_ENABLED;
            mTextColor = sEnabledColor;
        }
    }

    @Override
    protected void toggleState() {
        Intent i = new Intent(ExtraIntent.ACTION_TOGGLE_TORCH);
        if (mState == STATE_ENABLED) {
            i.putExtra(ExtraIntent.EXTRA_IS_ENABLE, false);
        } else {
            i.putExtra(ExtraIntent.EXTRA_IS_ENABLE, true);
        }
        sContext.sendBroadcast(i);
    }

    private boolean getTorchState() {
        return Settings.System.getInt(sContext.getContentResolver(),
                ExtraSettings.System.TORCH_STATE,
                0) == 0 ? false : true;
    }

    @Override
    protected boolean onLongClick() {
        return true;
    }
}
