/**
 * @author krluo
 * @author juude.song@gmail.com
 * */

package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;

import lewa.provider.ExtraSettings;

public class PowerManagerButton extends ObserveButton
{

    public final String TAG = PowerManagerButton.class.getSimpleName();
    private String POWER_ACTION = "com.lewa.powermanager.action";
    IntentFilter mFilter;
    public static final String SPM_DEVS_SWITTCH_FINISH_ACTION = "spm_dev_switch_finish_action";

    public PowerManagerButton() {
        super();
        mType = BUTTON_POWER_MANAGER;
        mFilter = new IntentFilter();
        mFilter.addAction(SPM_DEVS_SWITTCH_FINISH_ACTION);
        mObservedUris.add(Settings.System.getUriFor(ExtraSettings.System.POWERMANAGER_MODE_ON));
        mLabel = R.string.title_toggle_powermanager;
        mButtonName = R.string.title_toggle_powermanager;
        mSettingsIcon = R.drawable.btn_setting_powrm;
    }

    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        return mFilter;
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(SPM_DEVS_SWITTCH_FINISH_ACTION)) {
            update();
        }
    }

    @Override
    protected void updateState() {
        if (getState()) {
            Log.d(TAG, "state is : " + getState());
            mIcon = R.drawable.stat_power_on;
            mState = STATE_ENABLED;
            mTextColor = sEnabledColor;
        } else {
            mIcon = R.drawable.stat_power_off;
            mState = STATE_DISABLED;
            mTextColor = sDisabledColor;
        }
    }

    @Override
    protected void toggleState() {
        boolean state = getState();
        Intent intent = new Intent(POWER_ACTION);
        intent.putExtra("powerstate", !state ? 1 : 0);
        sContext.sendBroadcast(intent);
        mIcon = R.drawable.stat_power_inter;
        updateView();
    }

    @Override
    protected boolean onLongClick() {
        directTo(sContext, "com.lewa.spm");
        return true;
    }

    private boolean getState() {
        return Settings.System.getInt(sContext.getContentResolver(),
                ExtraSettings.System.POWERMANAGER_MODE_ON, 0) == 1;
    }

}
