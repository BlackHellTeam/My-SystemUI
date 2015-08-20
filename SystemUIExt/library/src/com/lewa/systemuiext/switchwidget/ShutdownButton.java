
package com.lewa.systemuiext.switchwidget;

import android.content.Context;
import android.os.PowerManager;

import com.lewa.systemuiext.R;

public class ShutdownButton extends StatelessButton
{
    public ShutdownButton() {
        super();
        mIcon = R.drawable.stat_shutdown;
        mLabel = R.string.title_toggle_shutdown;
        mButtonName = R.string.title_toggle_shutdown;
        mSettingsIcon = R.drawable.btn_setting_shutdown;
    }

    @Override
    protected void onClick() {
        showShutdownDialog();
    }

    private void showShutdownDialog() {
        PowerManager pm = (PowerManager) sContext
                .getSystemService(Context.POWER_SERVICE);
        if (null != pm) {
            //pm.rebootConfirm("shutdown", true);

        }

    }

    @Override
    protected boolean onLongClick() {
        showShutdownDialog();
        return true;
    }
}
