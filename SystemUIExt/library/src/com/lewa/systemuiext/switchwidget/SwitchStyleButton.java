
package com.lewa.systemuiext.switchwidget;

import lewa.provider.ExtraSettings;

import android.content.Intent;
import android.provider.Settings;

import com.lewa.systemuiext.R;

public class SwitchStyleButton extends ObserveButton {
    public static final String ACTION_SWITCH_WIDGET_SETTINGS = "lewa.intent.action.SWITCH_WIDGET_SETTINGS";

    public SwitchStyleButton() {
        super();
        mObservedUris.add(Settings.System.getUriFor(ExtraSettings.System.SWITCH_WIDGET_STYLE));
        mLabel = R.string.title_toggle_switchwidgetstyle;
        mButtonName = R.string.title_toggle_switchwidgetstyle;
        //NO settings button
    }

    @Override
    protected void updateState() {
        mIcon = R.drawable.switch_widget_more;
        mState = STATE_ENABLED;
    }

    @Override
    protected void toggleState() {
        gotoSwitchSettings();
    }

    @Override
    protected boolean onLongClick() {
        return true;
    }

    private void gotoSwitchSettings() {
        Intent in = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sContext.sendBroadcast(in);
        Intent intent = new Intent(ACTION_SWITCH_WIDGET_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
