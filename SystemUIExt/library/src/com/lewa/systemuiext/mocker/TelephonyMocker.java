package com.lewa.systemuiext.mocker;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.juuda.droidmock.mock.Mocker;
import com.lewa.systemuiext.adapter.TelephonyAdapter;


public class TelephonyMocker extends Mocker{

    private static final String TAG = "TelephonyMocker";

    public TelephonyMocker(Context context, Bundle extras) {
        super(context, extras);
    }

    @Override
    public void dump() {
         Log.e(TAG, "isMtkMultisimSuported"  + TelephonyAdapter.isMtkMultisimSuported(mContext));
         Log.e(TAG, "isQrdMultisimSuported"  + TelephonyAdapter.isQrdMultisimSuported(mContext));

    }    
}
