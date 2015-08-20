/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.BatteryManager;
import android.os.Handler;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import lewa.provider.ExtraSettings;

import com.android.systemui.R;

public class BatteryControllerLewa extends BroadcastReceiver {
    private static final String TAG = "StatusBar.BatteryControllerLewa";

    private Context mContext;
    private ArrayList<ImageView> mIconViews = new ArrayList<ImageView>();
    private ArrayList<ViewGroup> mComboViews = new ArrayList<ViewGroup>();
    private ArrayList<TextView> mLabelViews = new ArrayList<TextView>();
    private ArrayList<ImageView> mBatteryChargeViews = new ArrayList<ImageView>();

    private static final int BATTERY_STYLE_NORMAL = 0;
    private static final int BATTERY_STYLE_TEXT = 1;
    private static final int BATTERY_STYLE_GONE = 2;

    private static final int BATTERY_ICON_STYLE_NORMAL = R.drawable.stat_sys_battery;
    private static final int BATTERY_ICON_STYLE_CHARGE = R.drawable.stat_sys_battery_charge;
    private static final int BATTERY_ICON_STYLE_NORMAL_MIN = R.drawable.stat_sys_battery;
    private static final int BATTERY_ICON_STYLE_CHARGE_MIN = R.drawable.stat_sys_battery_charge;

    private static final int BATTERY_TEXT_STYLE_NORMAL = R.string.lewa_status_bar_settings_battery_meter_format;
    private static final int BATTERY_TEXT_STYLE_PERCENTAGE = R.string.status_bar_settings_battery_meter_format;

    private boolean mBatteryPlugged = false;
    private int mBatteryStyle;
    private int mBatteryIcon = BATTERY_ICON_STYLE_NORMAL;
    private int mBatteryLevel;

    Handler mHandler;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System
                    .getUriFor(ExtraSettings.System.STATUS_BAR_BATTERY), false,
                    this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    public BatteryControllerLewa(Context context) {
        mContext = context;
        mHandler = new Handler();

        SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        settingsObserver.observe();
        updateSettings();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(this, filter);
    }

    public void addIconView(ImageView v) {
        mIconViews.add(v);
    }

    // /LEWA START
    public void addComboView(ViewGroup v) {
        mComboViews.add(v);
    }

    // /LEWA END

    public void addLabelView(TextView v) {
        mLabelViews.add(v);
    }

    public void addBatteryChargeView(ImageView v) {
        mBatteryChargeViews.add(v);
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            mBatteryLevel = level;
            mBatteryPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,
                    0) != 0;
            updateBattery();
        }
    }

    /*
     * private int getLowBatteryInt() { ContentResolver resolver =
     * mContext.getContentResolver(); int lowint =
     * Settings.System.getInt(resolver, Settings.System.POWE_LOW_VAL, 10);
     * return lowint; }
     */

    private void updateBattery() {
        int N = mIconViews.size();

        for (int i = 0; i < N; i++) {
            ImageView v = mIconViews.get(i);
            v.setImageLevel(mBatteryLevel);
            v.setContentDescription(mContext.getString(
                    R.string.accessibility_battery_level, mBatteryLevel));
        }

        N = mComboViews.size();
        for (int i = 0; i < N; i++) {
            TextView v = (TextView) mComboViews.get(i).findViewById(
                    R.id.battery_level);
            v.setText(mContext.getString(BATTERY_TEXT_STYLE_NORMAL,
                    mBatteryLevel));
        }

        /*
         * N = mLabelViews.size(); for (int i=0; i<N; i++) { TextView v =
         * mLabelViews.get(i);
         * v.setText(mContext.getString(BATTERY_TEXT_STYLE_PERCENTAGE,
         * mBatteryLevel)); }
         */
        int mIcon = View.VISIBLE;
        int mText = View.GONE;
        int mPercentage = View.GONE;
        boolean mIfFullPlug = false;
        int mIconStyle = BATTERY_ICON_STYLE_NORMAL;

        // int lowpowint = getLowBatteryInt();

        if (mBatteryStyle == BATTERY_STYLE_NORMAL) {
            mIcon = (View.VISIBLE);
            mText = (View.GONE);
            mIconStyle = BATTERY_ICON_STYLE_NORMAL;

        } else if (mBatteryStyle == BATTERY_STYLE_TEXT) {
            mIcon = (View.GONE);
            mText = (View.VISIBLE);
            mIconStyle = R.drawable.stat_sys_battery_0;
        }

        /*
         * if(mBatteryLevel <= lowpowint) { mIconStyle =
         * R.drawable.stat_sys_battery_5; }
         */

        if (mBatteryPlugged && mBatteryLevel == 100) {
            mIfFullPlug = true;
        }

        /*
         * if(mBatteryPlugged) { mIcon = View.VISIBLE; mPercentage =
         * View.VISIBLE; mText = View.GONE; mIconStyle =
         * BATTERY_ICON_STYLE_NORMAL_MIN; }
         */

        N = mIconViews.size();
        for (int i = 0; i < N; i++) {
            ImageView v = mIconViews.get(i);
            v.setVisibility(mIcon);
            v.setImageResource(mIconStyle);
        }
        N = mComboViews.size();
        for (int i = 0; i < N; i++) {
            ViewGroup view = mComboViews.get(i);
            TextView text = (TextView) view.findViewById(R.id.battery_level);
            view.setVisibility(mText);
         /*   if (mIfFullPlug) {
                text.setTextColor(mContext.getResources().getColor(
                        R.color.full_battery));
            } else {*/
                text.setTextColor(mContext.getResources().getColor(
                        R.color.white));
//            }
        }

        /*
         * for (int i=0; i<N; i++) { TextView text = mLabelViews.get(i);
         * text.setVisibility(mPercentage); if(mIfFullPlug) {
         * text.setTextColor(mContext
         * .getResources().getColor(R.color.full_battery)); }else {
         * text.setTextColor(mContext.getResources().getColor(R.color.white)); }
         * }
         */
        N = mBatteryChargeViews.size();
        for (int i = 0; i < N; i++) {
            ImageView v = mBatteryChargeViews.get(i);
            v.setVisibility((mBatteryPlugged && !mIfFullPlug) ? View.VISIBLE
                    : View.GONE);
            v.setImageResource(R.drawable.stat_sys_battery_charge);
        }
    }

    private void updateText() {

    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();

        mBatteryStyle = (Settings.System.getInt(resolver,
                ExtraSettings.System.STATUS_BAR_BATTERY, 0));
        updateBattery();
    }
}
