package com.lewa.systemuiext.utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import lewa.provider.SensorProviderListener.OnShakeAndShakeListener;
import lewa.provider.SensorProviderListener;
import android.util.Log;
 
public class ShakeListener {
    protected static final String TAG = "ShakeListener";
    private SensorProviderListener mSensorProviderListener;
    private Context mContext;
    private CallBack mCallBack;
    private SharedPreferences mSharedPreferences;
    private boolean mRegistered;
    
    public ShakeListener(Context context, CallBack callBack) {
        mCallBack = callBack;
        mContext = context.getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSensorProviderListener = new SensorProviderListener(context);
    }
    
    public void unregister() {
        if (mRegistered) {
            mSensorProviderListener.unregisterSensorEventerListener(1);
            mRegistered = false;
        }
    }
    
    public static interface CallBack {
        public void onShake();
    }
    
    public void register() {
        if (mSharedPreferences.getBoolean("shake_clean", true)) {
            if (mRegistered)
                return;
            mSensorProviderListener.registerSensorEventerListener(1);
            mRegistered = true;
            mSensorProviderListener
                    .setOnShakeListener(new OnShakeAndShakeListener() {
                        @Override
                        public void onShake() {
                            mCallBack.onShake();
                        }
                    });
        }
    }
}
