package com.lewa.systemuiext.adapter;

import android.app.INotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.ServiceManager;
import android.util.Log;

import java.lang.reflect.Method;

public class NotiFilterAdapter {
    private static INotificationManager sNotificationManager;
    public static final String TAG = "NotiFilterAdapter";
    public static boolean areNotificationsEnabledForPackage(Context context, final String packageName, final int uid) {
        boolean enabled = false;
        if(sNotificationManager == null) {
            sNotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        }
        try {
            if(Build.VERSION.SDK_INT >= 18) {
                Method method = INotificationManager.class.getMethod("areNotificationsEnabledForPackage", String.class, int.class);
                enabled = (Boolean)method.invoke(sNotificationManager, packageName, uid);
            }
            else {
                Method method = INotificationManager.class.getMethod("areNotificationsEnabledForPackage", String.class);
                enabled = (Boolean)method.invoke(sNotificationManager, packageName);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return enabled;
    }

    public static void setNotificationsEnabledForPackage(Context context, final String packageName, final int uid, final boolean enabled) {
        if(sNotificationManager == null) {
            sNotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        }
        try {
            if(Build.VERSION.SDK_INT >= 18) {
                Method method = INotificationManager.class.getMethod("setNotificationsEnabledForPackage", String.class, int.class, boolean.class);
                method.invoke(sNotificationManager, packageName, uid, enabled);
            }
            else {
                Method method = INotificationManager.class.getMethod("setNotificationsEnabledForPackage", String.class, boolean.class);
                method.invoke(sNotificationManager,  packageName, enabled);
            }
        }
        catch (Exception e) {
               e.printStackTrace();
            }
        return;
    }

}
