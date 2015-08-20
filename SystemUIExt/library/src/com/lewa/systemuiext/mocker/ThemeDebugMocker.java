package com.lewa.systemuiext.mocker;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.juuda.droidmock.mock.Mocker;

public class ThemeDebugMocker extends Mocker{
//  sModuleMap.put("notifilter", NotiFilterMocker.class);
    
    private static final String TAG = "ThemeDebugMocker";

    public ThemeDebugMocker(Context context, Bundle extras) {
        super(context, extras);
    }

    @Override
    public void dump() {
        final String method = mExtras.getString("method", "toast");
        try {
            ThemeDebugMocker.class.getMethod(method).invoke(this);
        }catch(Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public void toast() {
        Toast.makeText(mContext, "This is a Toast ", Toast.LENGTH_SHORT).show();
    }
}
