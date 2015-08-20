
package com.lewa.systemuiext.switchwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.lewa.systemuiext.R;
import android.util.Log;
import com.mediatek.xlog.Xlog;
import com.mediatek.hotknot.HotKnotAdapter;
//qhwu add begin
import android.os.SystemProperties;
//qhwu add end
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


public class HotknotButton extends ReceiverButton {

    private HotKnotAdapter mAdapter;

    public HotknotButton() {
        super();
        mLabel = R.string.hotknot_settings_title;
        mButtonName = R.string.hotknot_settings_title;
        mSettingsIcon = R.drawable.btn_setting_hotknot;
        mAdapter = HotKnotAdapter.getDefaultAdapter(sContext);
        mFilter.addAction(HotKnotAdapter.ACTION_ADAPTER_STATE_CHANGED);

        IntentFilter iflter = new IntentFilter();
        iflter.addAction(HotKnotAdapter.ACTION_ADAPTER_STATE_CHANGED);
        sContext.registerReceiver(mReceiver, iflter);

        if (mAdapter != null && mAdapter.isEnabled()){
            mIcon = R.drawable.stat_hotnot_on;
            mTextColor = sEnabledColor;
        } else {
            mIcon = R.drawable.stat_hotnot_off;
            mTextColor = sDisabledColor;
        }
    }

    @Override
    protected void updateState() {
    }

    @Override
    protected void toggleState() {
        onClick();
    }

    @Override
    protected boolean onLongClick() {
        try {
            startActivity("lewa.settings.HOTKNOT_SETTINGS");
        } catch (Exception e) {

        }
        return false;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(HotKnotAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                if(mAdapter == null){
                    mAdapter = HotKnotAdapter.getDefaultAdapter(sContext);
                }

                if (mAdapter != null && mAdapter.isEnabled()) {
                    mIcon = R.drawable.stat_hotnot_on;
                    mTextColor = sEnabledColor;
                } else {
                    mIcon = R.drawable.stat_hotnot_off;
                    mTextColor = sDisabledColor;
                }
            }
        }
    };

    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        //qhwu add begin
        if(mAdapter == null){
            mAdapter = HotKnotAdapter.getDefaultAdapter(sContext);
        }
        if (mAdapter != null && mAdapter.isEnabled()) {
            mIcon = R.drawable.stat_hotnot_on;
            mTextColor = sEnabledColor;
        } else {
            mIcon = R.drawable.stat_hotnot_off;
            mTextColor = sDisabledColor;
        }
        //qhwu add end
        return mFilter;
    }

    protected void onClick() {
        //PR807606 qhwu add begin
        if(mAdapter == null){
            mAdapter = HotKnotAdapter.getDefaultAdapter(sContext);
        }
        //PR807606 qhwu add end
        if(mAdapter != null){
            if (!mAdapter.isEnabled()) {
                mAdapter.enable();
                mIcon = R.drawable.stat_hotnot_on;
                mTextColor = sEnabledColor;
            } else {
                mAdapter.disable();
                mIcon = R.drawable.stat_hotnot_off;
                mTextColor = sDisabledColor;
            }
        }
    }
}
