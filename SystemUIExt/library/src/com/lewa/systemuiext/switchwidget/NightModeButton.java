
package com.lewa.systemuiext.switchwidget;

/*
 * @author:wangqiang
 * @time:2013-08-27
 * @function:add night mode 
 * 
 * */

import com.lewa.systemuiext.R;

import android.provider.Settings;
import lewa.provider.ExtraSettings;
import android.content.Intent;

public class NightModeButton extends ObserveButton {

    public NightModeButton() {
        super();
        mObservedUris.add(Settings.System.getUriFor(ExtraSettings.System.NIGHT_MODES));
        mObservedUris.add(Settings.System.getUriFor("nightmode_enable"));
        mIcon = R.drawable.stat_nightmode_on;
        mLabel = R.string.title_nightmode;
        mButtonName = R.string.title_nightmode;
        mSettingsIcon = R.drawable.btn_setting_night;
    }

    @Override
    protected void updateState() {
        // TODO Auto-generated method stub
        boolean enable = getEnable();
        if (enable) {
            mIcon = R.drawable.stat_nightmode_on;
            mTextColor = sEnabledColor;
        } else {
            mIcon = R.drawable.stat_nightmode_off;
            mTextColor = sDisabledColor;
        }
    }

    @Override
    protected void toggleState() {
        // TODO Auto-generated method stub
        boolean enable = getEnable();
        if (!enable) {
            startNightService(true);
        } else {
            startNightService(false);
        }
        update();
    }

    private void startNightService(boolean value) {
        Settings.System.putInt(sContext.getContentResolver(),
                "nightmode_enable", value ? 1 : 0);
        Intent service = (new Intent()).setClassName("com.android.systemui",
                "com.android.systemui.NightModeService");
        if (value) {
            sContext.startService(service);
        } else {
            sContext.stopService(service);
        }
    }
    @Override
    protected boolean onLongClick() {
        startActivity("android.settings.DISPLAY_SETTINGS");
        return true;
    }

    private boolean getEnable() {
        return Settings.System.getInt(sContext.getContentResolver()
                , "nightmode_enable", 0) == 1 ? true : false;
    }

}
