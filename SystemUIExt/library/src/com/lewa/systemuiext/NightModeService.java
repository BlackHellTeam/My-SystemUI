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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.graphics.Color;
import android.view.WindowManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import lewa.provider.ExtraSettings;
import android.provider.Settings;
import android.app.ActivityManagerNative;

import android.util.Log;

public class NightModeService extends Service {

    private final String TAG = "NightModeService";
    private View mView;
    private WindowManager mNightWindowManager;
    private WindowManager.LayoutParams mNightWindowParams;
    private static View mCurrentView = null;
    private static View mBlackView = null;
    private static View mRedView = null;
    private static View mYellowView = null;
    private static final int MSG_CREATE_NIGHTVIEW = 5;
    private int mNightModeValue = 0;
    private int mNightmodeEnable = 0;
    Handler mHandler;

    private class PolicyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CREATE_NIGHTVIEW:
                //updateService();
                break;
            }
        }
    }

    private void initNightView() {
        mNightWindowManager = (WindowManager)getSystemService("window");
        mNightWindowParams = new WindowManager.LayoutParams();
        mNightWindowParams.flags = 280;
        mNightWindowParams.format = 1;
        mNightWindowParams.gravity = 17;
        mNightWindowParams.type = 2006;
        mNightWindowParams.x = 0;
        mNightWindowParams.y = 0;
        mNightWindowParams.width = -1;
        mNightWindowParams.height = -1;
        if (mBlackView == null) {
            mBlackView = new View(this);
            mBlackView.setFocusable(false);
            mBlackView.setFocusableInTouchMode(false);
            mBlackView.setBackgroundColor(Color.argb(190,
                    Color.red(-16777216), Color.green(-16777216), Color.blue(-16777216)));
        }

        if (mRedView == null) {
            mRedView = new View(this);
            mRedView.setFocusable(false);
            mRedView.setFocusableInTouchMode(false);
            mRedView.setBackgroundColor(0x4FFF0000);
        }

        if (mYellowView == null) {
            mYellowView = new View(this);
            mYellowView.setFocusable(false);
            mYellowView.setFocusableInTouchMode(false);
            mYellowView.setBackgroundColor(0x4FCDCD00);
        }
    }

    private boolean addNightView()
    {
        int value = getStateValue();
        try
        {
            if (value == 0) {
                mNightWindowManager.addView(mBlackView, mNightWindowParams);
                mCurrentView = mBlackView;
            } else if (value == 1) {
                mNightWindowManager.addView(mRedView, mNightWindowParams);
                mCurrentView = mRedView;
            }else if (value == 2) {
                mNightWindowManager.addView(mYellowView, mNightWindowParams);
                mCurrentView = mYellowView;
            }
            return true;
        }
        catch (Exception localException)
        {
            Log.e(TAG, "Can't add view : " + localException);
        }
        return false;
    }

    private void removeNightView(View view) {
        try
        {
            mNightWindowManager.removeView(view);
        } catch (Exception localException) {
            Log.e(TAG, "Can't remove view : " + localException);
        }
    }

    private void initNightViewAndMode(ContentResolver resolver) {
        if (ActivityManagerNative.isSystemReady()) {
            int value = Settings.System.getInt(resolver,
                ExtraSettings.System.NIGHT_MODES, 0);
            int enable = Settings.System.getInt(resolver,
                "nightmode_enable", 0);
            if (mNightModeValue != value || mNightmodeEnable != enable) {
                mNightModeValue = value;
                mNightmodeEnable = enable;
                initNightView();
                updateNightMode();
            }
        }
    }

    private boolean removeNightView()
    {
        try
        {
            int value = getStateValue();
            if (value == 0) {
                mNightWindowManager.removeView(mBlackView);
                mCurrentView = null;
            } else if (value == 1) {
                mNightWindowManager.removeView(mRedView);
                mCurrentView = null;
            } else if (value == 2) {
                mNightWindowManager.removeView(mYellowView);
                mCurrentView = null;
            }
            return true;
        } catch (Exception localException) {
            Log.e(TAG, "Can't remove view : " + localException);
        }
        return false;
    }

    private int getStateValue() {
        int value = Settings.System.getInt(getContentResolver()
                ,ExtraSettings.System.NIGHT_MODES, 0);
        return value;
    }

    private boolean getEnable() {
        return Settings.System.getInt(getContentResolver()
                , "nightmode_enable", 0) == 1 ? true : false;
    }

    private void updateNightMode() {
        boolean enable = getEnable();
        if (enable) {
            if (mCurrentView != null) {
                removeNightView(mCurrentView);
            }
            addNightView();
        } else {
            removeNightView();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new PolicyHandler();
        LewaNightObserver nightObserver = new LewaNightObserver(mHandler);
        nightObserver.observe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCurrentView != null) {
            ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(mCurrentView);
            mCurrentView = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class LewaNightObserver extends ContentObserver {


        LewaNightObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                ExtraSettings.System.NIGHT_MODES), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                ExtraSettings.System.NIGHTMODES_ENABLE), false, this);
            updateService();
        }

        @Override
        public void onChange(boolean selfChange) {
            mHandler.post(new Runnable() {
                public void run() {
                    updateService();
                }
            });
        }

        private void updateService() {
            ContentResolver resolver = getContentResolver();
            initNightViewAndMode(resolver);
        }
    }

}
