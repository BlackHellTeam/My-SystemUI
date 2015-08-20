
package com.lewa.systemuiext.adapter;

import android.os.Build;
import com.android.internal.statusbar.IStatusBarService;

import java.lang.reflect.Method;

public class StatusBarServiceAdapter {
    public static void collapsePanels(IStatusBarService service) {
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                Method method = service.getClass().getMethod("collapsePanels");
                method.invoke(service);
            }
            else {
                Method method = service.getClass().getMethod("collapse");
                method.invoke(service);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface onExpandedChangeListener {
        public void onExpandedChanged(boolean visable);
    }
}
