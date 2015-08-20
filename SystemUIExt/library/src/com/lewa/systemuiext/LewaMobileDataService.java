/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.graphics.Color;
import android.view.WindowManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import lewa.provider.ExtraSettings;
import android.provider.Settings;
import lewa.telephony.LewaSimInfo;
import android.telephony.SubscriptionManager;
import lewa.telephony.TelephonyHelper;
import android.telephony.TelephonyManager;
import android.net.ConnectivityManager;
import android.content.DialogInterface;
import com.android.internal.app.AlertActivity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.app.StatusBarManager;
import android.util.Log;
import com.lewa.systemuiext.R;

public class LewaMobileDataService extends Service {

    private Handler mHandler;
    private TelephonyManager mTelephonyManager;
    private static AlertDialog mDataDialog = null;
    private boolean mSelfChange;
    private StatusBarManager mStatusBarManager;
    public final static String TAG = "LewaMobileDataService";
    private class H extends Handler {
        @Override
        public void handleMessage(Message msg) {
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new H();
        mTelephonyManager = TelephonyManager.from(this);
        mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        MobileStateChangeObserver dataObserver = new MobileStateChangeObserver(mHandler);
        dataObserver.observe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MobileStateChangeObserver extends ContentObserver {


        MobileStateChangeObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = getContentResolver();
            if(TelephonyHelper.supportMultipleSims()) {
                resolver.registerContentObserver(Settings.Global.getUriFor(
                    Settings.Global.MOBILE_DATA+0), true, this);
                 resolver.registerContentObserver(Settings.Global.getUriFor(
                    Settings.Global.MOBILE_DATA+1), true, this);
             } else {
                resolver.registerContentObserver(Settings.Global.getUriFor(
                    Settings.Global.MOBILE_DATA), true, this);
            }
        }

        @Override
        public void onChange(final boolean selfChange) {
            Log.d(TAG,"onChange->selfChange:"+selfChange);
            mHandler.post(new Runnable() {
                public void run() {
                    if (getDataState()) {
                        updateService();
                    }
                }
            });
        }

        private void updateService() {
            if (mStatusBarManager != null) {
                mStatusBarManager.collapsePanels();
            }
            new LewaMobileDialog();
        }
    }

        private void setDataState(boolean enabled) {
            if(TelephonyHelper.supportMultipleSims()) {
                 int phoneId = SubscriptionManager.getDefaultDataPhoneId();
                 Settings.Global.putInt(getContentResolver(),Settings.Global.MOBILE_DATA + phoneId,enabled ? 1:0);
                 long[] subId = SubscriptionManager.getSubId(phoneId);
                 mTelephonyManager.setDataEnabledUsingSubId(subId[0], enabled);
            } else {
                 Settings.Global.putInt(getContentResolver(),Settings.Global.MOBILE_DATA , enabled? 1:0);
            }
        }

        private boolean getDataState() {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            ContentResolver cr = getContentResolver();
            if(TelephonyHelper.supportMultipleSims()) {
                 int phoneId = SubscriptionManager.getDefaultDataPhoneId();
                 Log.d(TAG, "get Data  for phoneId-" + phoneId);
                 return Settings.Global.getInt(cr, Settings.Global.MOBILE_DATA + phoneId, 0) == 1;
             } else {
                return Settings.Global.getInt(cr, Settings.Global.MOBILE_DATA, 0) == 1;
            }
        }

    private class LewaMobileDialog implements 
        DialogInterface.OnClickListener,DialogInterface.OnDismissListener{

        private LewaMobileDialog() {
            if (mDataDialog == null) {
                AlertDialog dialog = new AlertDialog.Builder(LewaMobileDataService.this,com.lewa.internal.R.style.V5_Theme_Holo_Light_Dialog_Alert)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.mobile_data_warning)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this).create();
                mDataDialog = dialog;
                dialog.setOnDismissListener(this);
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
            } else {
                mDataDialog.show();
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub
            if (!mSelfChange) {
                setDataState(false);
            } else {
                mSelfChange = false;
            }
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
            if (dialog == mDataDialog){
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    mSelfChange = true;
                } else {
                    setDataState(false);
                }

            }
        }
    }

}
