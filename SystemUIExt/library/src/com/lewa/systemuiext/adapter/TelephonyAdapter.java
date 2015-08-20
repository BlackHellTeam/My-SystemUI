package com.lewa.systemuiext.adapter;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class TelephonyAdapter {

    private static final String TAG = "TelephonyAdapter";

    public static boolean isMultisimReady() {

        return false;
    }

    public static boolean isMtkMultisimSuported(Context context) {
        /*
         * FeatureOption.MTK_GEMINI_SUPPORT &&
         * SIMInfo.getInsertedSIMList(mContext).size() > 1;
         */
        /*
         * try {
         * 
         * Class<?> featureOption = Class
         * .forName("com.mediatek.common.featureoption.FeatureOption"); Field
         * fieldMTK_GEMINI_SUPPORT =
         * featureOption.getField("MTK_GEMINI_SUPPORT"); boolean
         * MTK_GEMINI_SUPPORT =
         * fieldMTK_GEMINI_SUPPORT.getBoolean(featureOption);
         * 
         * Class<?> classSIMINFO =
         * Class.forName("android.provider.Telephony$SIMInfo"); Method
         * methodgetInsertedSIMList =
         * classSIMINFO.getMethod("getInsertedSIMList", Context.class); List<?>
         * simList = (List<?>) methodgetInsertedSIMList.invoke(classSIMINFO,
         * context);
         * 
         * return (MTK_GEMINI_SUPPORT && simList.size() > 1); } catch (Exception
         * e) { Log.e(TAG, "", e); }
         */
        return false;
    }

    public static boolean isQrdMultisimSuported(Context context) {
        /*
         * return MSimTelephonyManager.getDefault().getSimState(0) ==
         * TelephonyManager.SIM_STATE_READY &&
         * MSimTelephonyManager.getDefault().getSimState(1) ==
         * TelephonyManager.SIM_STATE_READY;
         */
        /*
         * try { Class<?> classSimManager =
         * Class.forName("android.telephony.MSimTelephonyManager"); Method
         * methodGetDefault = classSimManager.getMethod("getDefault"); Object
         * objectSimManager = methodGetDefault.invoke(classSimManager); Method
         * methodGetSimState = classSimManager.getMethod("getSimState",
         * int.class); int simState0 = (Integer)
         * methodGetSimState.invoke(objectSimManager, 0); int simState1 =
         * (Integer) methodGetSimState.invoke(objectSimManager, 1);
         * 
         * Class<?> classTelephonyManager =
         * Class.forName("android.telephony.TelephonyManager"); Field
         * fieldSIM_STATE_READY =
         * classTelephonyManager.getField("SIM_STATE_READY"); int
         * SIM_STATE_READY = (Integer)
         * fieldSIM_STATE_READY.get(classTelephonyManager);
         * 
         * if (simState1 == SIM_STATE_READY && simState0 == SIM_STATE_READY) {
         * return true; }
         * 
         * } catch (Exception e) { Log.e(TAG, "", e); }
         */
        return false;
    }

}
