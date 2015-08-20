package com.lewa.systemuiext.switchwidget;

import android.content.Context;
import android.os.PowerManager;

import com.lewa.systemuiext.R;

public class RebootButton extends StatelessButton {
    public static final String TAG = "RebootButton";

    public RebootButton() {
        super();
        mIcon = R.drawable.stat_reboot;
        mLabel = R.string.title_toggle_reboot;
        mButtonName = R.string.title_toggle_reboot;
        mSettingsIcon = R.drawable.btn_setting_reboot;
    }

    @Override
    protected void onClick() {
        reboot();
    }


    @Override
    protected boolean onLongClick() {
        reboot();
        return true;
    }

    private void reboot() {
        PowerManager pm = (PowerManager) sContext
                .getSystemService(Context.POWER_SERVICE);
        if (null != pm) {
           // pm.rebootConfirm("null", true);
        }
    }
}
