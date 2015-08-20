package com.lewa.systemuiext;

import com.juuda.droidmock.mock.Mocks;
import com.lewa.systemuiext.mocker.NetUsageViewMocker;
import com.lewa.systemuiext.mocker.TelephonyMocker;
import com.lewa.systemuiext.mocker.ThemeDebugMocker;
import com.lewa.systemuiext.mocker.ViewDebugMocker;

import com.lewa.systemuiext.Constants;
import android.content.Context;
public class SystemUIExtApplication {
	
	private Context mContext;

	public SystemUIExtApplication(Context context) {
		mContext = context.getApplicationContext();
	}

    public static void onCreate() {
        Mocks.sModuleMap.put("netusage", NetUsageViewMocker.class);
        Mocks.sModuleMap.put("telephony", TelephonyMocker.class);
        Mocks.sModuleMap.put("theme", ThemeDebugMocker.class);
        Mocks.sModuleMap.put("viewdebug", ViewDebugMocker.class);
    }
}
