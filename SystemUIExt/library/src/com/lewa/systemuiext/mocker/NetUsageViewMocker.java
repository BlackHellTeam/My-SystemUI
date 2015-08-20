package com.lewa.systemuiext.mocker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.juuda.droidmock.mock.MockUtils;
import com.juuda.droidmock.mock.Mocker;
import com.lewa.systemuiext.net.NetUsageView;

public class NetUsageViewMocker extends Mocker{

    public NetUsageViewMocker(Context context, Bundle extras) {
        super(context, extras);
    }
    
    public static final long M = 1024 * 1024;
    @Override
    public void dump() {
        Intent i =  new Intent(NetUsageView.ACTION_DATA_UPDATED);
        
//        long todayBytes =  extras.getLong("today");
//        boolean disabled = extras.getBoolean("disabled");
//        long totalBytes =  extras.getLong("total");
//        long limitBytes =  extras.getLong("limit");
//        long warningBytes =  extras.getLong("warning");
//        boolean exceed = extras.getBoolean("exceed");
        String type = MockUtils.getString(mExtras, "type" , "disabled");
        if(type.equals("disabled")) {
            i.putExtra("disabled", true);
        }
        else if(type.equals("nolimit")){
            i.putExtra("disabled", false);
            i.putExtra("today", 1 * M);
            i.putExtra("limit", -1);
        }
        else if(type.equals("exceed")){
            i.putExtra("exceed", true);
            i.putExtra("disabled", false);
            i.putExtra("today", 1 * M);
            i.putExtra("limit", 100 * M);
            i.putExtra("warning", 30 * M);
            i.putExtra("total", 120 * M);
        }
        else if(type.equals("normal")){
            i.putExtra("disabled", false);
            i.putExtra("today", 1 * M);
            i.putExtra("limit", 100 * M);
            i.putExtra("warning", 30 * M);
            i.putExtra("total", 80 * M);
            i.putExtra("exceed", false);
        }
        mContext.sendBroadcast(i);
    }

}
