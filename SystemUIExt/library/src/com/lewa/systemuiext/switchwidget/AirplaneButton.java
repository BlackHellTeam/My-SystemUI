
package com.lewa.systemuiext.switchwidget;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.lewa.systemuiext.R;

@SuppressLint("NewApi")
public class AirplaneButton extends ObserveButton
{
    public final String TAG = AirplaneButton.class.getSimpleName();

    public AirplaneButton() {
        super();
        mObservedUris.add(Settings.System.getUriFor(Settings.Global.AIRPLANE_MODE_ON));
        mLabel = R.string.title_toggle_airplane;
        mButtonName = R.string.title_toggle_airplane;
        mSettingsIcon = R.drawable.btn_setting_airplane;
    }

    @Override
    protected void updateState() {
        if (getState()) {
            mIcon = R.drawable.stat_airplane_on;
            mTextColor = sEnabledColor;
        } else {
            mIcon = R.drawable.stat_airplane_off;
            mTextColor = sDisabledColor;
        }
    }

    @Override
    protected void updateView() {
        super.updateView();
        mView.setCompoundDrawablesWithIntrinsicBounds(0, mIcon, 0, 0);
    }

    @Override
    protected void toggleState() {
        boolean state = getState();
        Settings.Global.putInt(sContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                state ? 0 : 1);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", !state);
        sContext.sendBroadcast(intent);
        // System.
    }

    @Override
    protected boolean onLongClick() {
        startActivity("android.settings.AIRPLANE_MODE_SETTINGS");
        return false;
    }
    
//    @Override
//    protected boolean onClick() {
//        startActivity("android.settings.AIRPLANE_MODE_SETTINGS");
//        return false;
//    }

    private boolean getState() {
        return Settings.System.getInt(sContext.getContentResolver()
                , Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }
}
