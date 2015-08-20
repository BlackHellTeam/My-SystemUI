package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import com.android.internal.statusbar.IStatusBarService;
import android.os.ServiceManager;

import java.util.List;
import lewa.telephony.LewaSimInfo;

import android.util.Log;

public class GeminiDataButton extends ObserveButton
{

    public String TAG=GeminiDataButton.class.getSimpleName();

    public GeminiDataButton() {
        super();
      //  mObservedUris.add(Settings.System.getUriFor(Settings.System.GPRS_CONNECTION_SIM_SETTING));
        mLabel = R.string.title_toggle_gemini_data;
        mButtonName = R.string.title_toggle_gemini_data;
        mSettingsIcon = R.drawable.btn_gemini_data;
    }

    @Override
    protected void updateState() {
        if (getState()) {
            mIcon = R.drawable.stat_gemini_data1;
            mTextColor = sEnabledColor;
            mState = STATE_ENABLED;
        } else {
            mIcon = R.drawable.stat_gemini_data2;
            mState = STATE_DISABLED;
            mTextColor = sEnabledColor;
        }
    }

    @Override
    protected void toggleState() {
        Context context = sContext;
        List<LewaSimInfo> list = LewaSimInfo.getInsertedSimList(sContext);
        if(list.size() <= 1)
            return;
        long simId = 0;
        for(int i = 0; i < list.size(); i++){
            
            LewaSimInfo info = list.get(i);
            if(simId == info.mSimId){
                info = list.get(i == list.size() - 1 ? 0 : i + 1);
                /*Intent intent = new Intent(Intent.ACTION_DATA_DEFAULT_SIM_CHANGED);
                intent.putExtra("simid", Long.valueOf(info.mSimId));
                context.sendBroadcast(intent);*/
                break;
            }
        }
    }

    @Override
    protected boolean onLongClick() {
        Context context = sContext;
        List<LewaSimInfo> list = LewaSimInfo.getInsertedSimList(context);
        if(list.size() > 1){
            Intent intent = new Intent("com.android.settings.SIM_MANAGEMENT_ACTIVITY");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            IStatusBarService mBarService = IStatusBarService.Stub.asInterface(
                    ServiceManager.getService(Context.STATUS_BAR_SERVICE));
            try{
                mBarService.collapsePanels();
            }catch(Exception ex){
            }
            return false;
        }
        return true;
    }

    private boolean getState() {
        Context context = sContext;
        long simId = 0;
        List<LewaSimInfo> list = LewaSimInfo.getInsertedSimList(context);
        for(LewaSimInfo info : list){
            if(simId == info.mSimId){
                return info.mSlot == 0;
            }
        }
        return true;
    }
}
