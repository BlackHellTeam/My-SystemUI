/**
 * Copyright (c) 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.systemuiext.utils;

import static android.media.AudioAttributes.USAGE_ALARM;
import static android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE;
import static android.media.AudioAttributes.USAGE_NOTIFICATION;
import static android.media.AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT;
import android.app.AppOpsManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.service.notification.NotificationListenerService;
import android.service.notification.ZenModeConfig;
import android.telecom.TelecomManager;
import android.util.Log;
import android.util.Slog;

import com.android.internal.R;

import libcore.io.IoUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

public class ZenModeHelper {
    private static final String TAG = "ZenModeHelper";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private final Context mContext;
    private final Handler mHandler;
    private final SettingsObserver mSettingsObserver;
    private final AppOpsManager mAppOps;
    private final ZenModeConfig mDefaultConfig;
    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    private ComponentName mDefaultPhoneApp;
    private int mZenMode;
    private ZenModeConfig mConfig;
    private AudioManager mAudioManager;
    private int mPreviousRingerMode = -1;

    public ZenModeHelper(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        mDefaultConfig = readDefaultConfig(context.getResources());
        mConfig = mDefaultConfig;
        mSettingsObserver = new SettingsObserver(mHandler);
        mSettingsObserver.observe();
    }

    public static ZenModeConfig readDefaultConfig(Resources resources) {
        XmlResourceParser parser = null;
        try {
            parser = resources.getXml(R.xml.default_zen_mode_config);
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                final ZenModeConfig config = ZenModeConfig.readXml(parser);
                if (config != null) return config;
            }
        } catch (Exception e) {
            Slog.w(TAG, "Error reading default zen mode config from resource", e);
        } finally {
            IoUtils.closeQuietly(parser);
        }
        return new ZenModeConfig();
    }

    public void addCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    public void setAudioManager(AudioManager audioManager) {
        mAudioManager = audioManager;
    }

    public int getZenModeListenerInterruptionFilter() {
        switch (mZenMode) {
            case Global.ZEN_MODE_OFF:
                return NotificationListenerService.INTERRUPTION_FILTER_ALL;
            case Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS:
                return NotificationListenerService.INTERRUPTION_FILTER_PRIORITY;
            case Global.ZEN_MODE_NO_INTERRUPTIONS:
                return NotificationListenerService.INTERRUPTION_FILTER_NONE;
            default:
                return 0;
        }
    }

    private static int zenModeFromListenerInterruptionFilter(int listenerInterruptionFilter,
            int defValue) {
        switch (listenerInterruptionFilter) {
            case NotificationListenerService.INTERRUPTION_FILTER_ALL:
                return Global.ZEN_MODE_OFF;
            case NotificationListenerService.INTERRUPTION_FILTER_PRIORITY:
                return Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS;
            case NotificationListenerService.INTERRUPTION_FILTER_NONE:
                return Global.ZEN_MODE_NO_INTERRUPTIONS;
            default:
                return defValue;
        }
    }

    public void requestFromListener(int interruptionFilter) {
        final int newZen = zenModeFromListenerInterruptionFilter(interruptionFilter, -1);
        if (newZen != -1) {
            setZenMode(newZen, "listener");
        }
    }


    public int getZenMode() {
        return mZenMode;
    }

    public void setZenMode(int zenModeValue, String reason) {
        Global.putInt(mContext.getContentResolver(), Global.ZEN_MODE, zenModeValue);
    }

    public void updateZenMode() {
        final int mode = Global.getInt(mContext.getContentResolver(),
                Global.ZEN_MODE, Global.ZEN_MODE_OFF);
        mZenMode = mode;
        final boolean zen = mZenMode != Global.ZEN_MODE_OFF;
        final String[] exceptionPackages = null; // none (for now)

        // call restrictions
        final boolean muteCalls = zen && !mConfig.allowCalls;
        mAppOps.setRestriction(AppOpsManager.OP_VIBRATE, USAGE_NOTIFICATION_RINGTONE,
                muteCalls ? AppOpsManager.MODE_IGNORED : AppOpsManager.MODE_ALLOWED,
                exceptionPackages);
        mAppOps.setRestriction(AppOpsManager.OP_PLAY_AUDIO, USAGE_NOTIFICATION_RINGTONE,
                muteCalls ? AppOpsManager.MODE_IGNORED : AppOpsManager.MODE_ALLOWED,
                exceptionPackages);

        // alarm restrictions
        final boolean muteAlarms = false;//mZenMode == Global.ZEN_MODE_NO_INTERRUPTIONS;
        mAppOps.setRestriction(AppOpsManager.OP_VIBRATE, USAGE_ALARM,
                muteAlarms ? AppOpsManager.MODE_IGNORED : AppOpsManager.MODE_ALLOWED,
                exceptionPackages);
        mAppOps.setRestriction(AppOpsManager.OP_PLAY_AUDIO, USAGE_ALARM,
                muteAlarms ? AppOpsManager.MODE_IGNORED : AppOpsManager.MODE_ALLOWED,
                exceptionPackages);
        dispatchOnZenModeChanged();
    }


    public void readXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        final ZenModeConfig config = ZenModeConfig.readXml(parser);
        if (config != null) {
            setConfig(config);
        }
    }

    public void writeXml(XmlSerializer out) throws IOException {
        mConfig.writeXml(out);
    }

    public ZenModeConfig getConfig() {
        return mConfig;
    }

    public boolean setConfig(ZenModeConfig config) {
        if (config == null || !config.isValid()) return false;
        if (config.equals(mConfig)) return true;
        mConfig = config;
        dispatchOnConfigChanged();
        final String val = Integer.toString(mConfig.hashCode());
        Global.putString(mContext.getContentResolver(), Global.ZEN_MODE_CONFIG_ETAG, val);
        updateZenMode();
        return true;
    }


    private void dispatchOnConfigChanged() {
        for (Callback callback : mCallbacks) {
            callback.onConfigChanged();
        }
    }

    private void dispatchOnZenModeChanged() {
        for (Callback callback : mCallbacks) {
            callback.onZenModeChanged();
        }
    }

    private class SettingsObserver extends ContentObserver {
        private final Uri ZEN_MODE = Global.getUriFor(Global.ZEN_MODE);

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(ZEN_MODE, false /*notifyForDescendents*/, this);
            update(null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            if (ZEN_MODE.equals(uri)) {
                updateZenMode();
            }
        }
    }

    public static class Callback {
        void onConfigChanged() {}
        void onZenModeChanged() {}
    }
}
