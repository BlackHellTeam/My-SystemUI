
package com.lewa.systemuiext.adapter;

import android.os.Build;
import android.os.IPowerManager;

import java.lang.reflect.Method;

public class SystemUIAdapter {

    public static void setBrightness(IPowerManager powerManager, int brightness) {
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                Method method = powerManager.getClass().getMethod(
                        "setTemporaryScreenBrightnessSettingOverride", int.class);
                method.invoke(powerManager, brightness);
            }
            else {
                Method method = powerManager.getClass().getMethod("setBacklightBrightness");
                method.invoke(powerManager, brightness);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
