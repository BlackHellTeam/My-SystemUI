/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.lewa.systemuiext.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import com.lewa.systemuiext.R;
//import com.lewa.systemuiext.widgets.Clock.ClockInjection;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateView extends TextView {
    private static final String TAG = "DateView";
    private Calendar mCalendar;

    private boolean mAttachedToWindow;
    private boolean mWindowVisible;
    private boolean mUpdating;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_TIME_TICK.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                    || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                updateClock();
            }
        }
    };

    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        setUpdates();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
        setUpdates();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mWindowVisible = visibility == VISIBLE;
        setUpdates();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        setUpdates();
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // makes the large background bitmap not force us to full width
        return 0;
    }

    public void updateClock() {
        final Context context = getContext();
        Date now = new Date();
        CharSequence dow = DateFormat.format("EEEE", now);
        CharSequence date = DateFormat.getLongDateFormat(context).format(now);
        mCalendar = Calendar.getInstance(TimeZone.getDefault());
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        boolean is24 = DateFormat.is24HourFormat(context);
        String format = null;
//        LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
        String zhAmPm = "";
        Locale locale = mContext.getResources().getConfiguration().locale;
        boolean isZhAmPm = !is24 && (Locale.CHINA.equals(locale) || Locale.TAIWAN.equals(locale)); 
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int amPm = mCalendar.get(Calendar.AM_PM);
        if(isZhAmPm) {
            zhAmPm = new ClockInjection().getTimeSliceString(getContext(), hour); 
        }
        else if(!is24){
            zhAmPm = DateUtils.getAMPMString(amPm);
        }
        setText(context.getString(R.string.lewa_status_bar_date_formatter, dow, date, zhAmPm));
    }

    private boolean isVisible() {
        View v = this;
        while (true) {
            if (v.getVisibility() != VISIBLE) {
                return false;
            }
            final ViewParent parent = v.getParent();
            if (parent instanceof View) {
                v = (View)parent;
            } else {
                return true;
            }
        }
    }

    private void setUpdates() {
        boolean update = mAttachedToWindow && mWindowVisible && isVisible();
        if (update != mUpdating) {
            mUpdating = update;
            if (update) {
                // Register for Intent broadcasts for the clock and battery
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_TIME_TICK);
                filter.addAction(Intent.ACTION_TIME_CHANGED);
                filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                mContext.registerReceiver(mIntentReceiver, filter, null, null);
                updateClock();
            } else {
                mContext.unregisterReceiver(mIntentReceiver);
            }
        }
    }
}
