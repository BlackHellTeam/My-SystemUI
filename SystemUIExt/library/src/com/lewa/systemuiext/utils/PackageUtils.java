package com.lewa.systemuiext.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class PackageUtils {
    public static final String TAG = "PackageUtils";

    public static boolean isSystemApp(Context context, String pkgname) {
        PackageManager mPM = context.getPackageManager();
        try {
            ApplicationInfo applicationinfo = mPM.getApplicationInfo(pkgname,
                    PackageManager.GET_SIGNATURES);
            if ((applicationinfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            }
        } catch (Exception ex) {
            Log.d(TAG, "no package " + pkgname);
        }
        return false;
    }

    public static int getPackageUid(Context context, String pkgname) {
        PackageManager mPM = context.getPackageManager();
        try {
            return mPM.getApplicationInfo(pkgname,
                    PackageManager.GET_SIGNATURES).uid;
        } catch (Exception ex) {
            Log.d(TAG, "no this package" + pkgname);
        }
        return -1;
    }

    public static boolean isPackageInstalled(Context context, String pkgname) {
        PackageManager mPM = context.getPackageManager();
        try {
            mPM.getApplicationInfo(pkgname, 0);
        } catch (Exception ex) {
            Log.d(TAG, "no this package " + pkgname);
            return false;
        }
        return true;
    }

}
