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

import static com.android.systemui.statusbar.phone.BarTransitions.MODE_TORCH_UP;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_WIFI_AP;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_SOUND_REC;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_INCALL_UI;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_TRANSPARENT;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import android.net.wifi.WifiDevice;
import android.app.Notification;
import android.content.ComponentName;
import android.os.UserHandle;
import java.util.HashSet;
import android.app.PendingIntent;
import com.android.systemui.R;
import android.content.ContentResolver;
import android.provider.Settings;
import lewa.provider.ExtraSettings;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import com.android.systemui.statusbar.policy.HotspotControllerImpl;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.lewa.systemuiext.utils.StatusBarCallTimer;

public class StatusRemindController extends BroadcastReceiver {

    private static final String TAG = "StatusRemindController";
    private static final boolean DEBUG = true;//Log.isLoggable(TAG, Log.DEBUG);
    public static final String ACTION_INCALL_UI = "com.lewa.action.STATUS_INCALL_UI";
    public static final String ACTION_SOUND_REC = "com.lewa.action.STATUS_SOUND_REC";
    public static final String ACTION_TORCH_UP = "com.lewa.action.STATUS_TORCH_UP";
    private static final String STATUS_REMIND_HEIGHT = "status_bar_remind_height";
    public static final String INCALL_UI_NAME = "com.android.dialer";
    public static final String TORCH_UP_NAME = "com.lewa.flashlight";
    public static final String WIFI_AP_NAME = "com.android.wifi.ap";
    public static final String SOUND_REC_NAME = "com.android.soundrecorder";
    private static final String REC_MODE_SETTINGS = "status_remind_rec";
    private static final String INCALL_UI_SETTINGS = "status_remind_incall";
    public static final String NEW_OUTGOING_CALL = "InCallActivity.new_outgoing_call";
    private static final long CALL_TIME_UPDATE_INTERVAL_MS = 1000;
    private static final int REC_START_RESUME = 0;
    private static final int REC_STOP = 1;
    private static final int REC_PAUSE = 2;
    private static final int INCALL_UI_START_TIME = 0;
    private static final int INCALL_UI_START_NOTIME = 1;
    private static final int INCALL_UI_CANCEL = 2;
    private final ArrayList<StatusStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    private List<WifiDevice> mWifiDevice;
    private Intent mIntent;
    private String mRemindPackage;
    private int mRemindMode = -1;
    private StatusBarNotification mNotification;
    private boolean mStatusRemindMode;
    private Handler mHandler = new Handler();
    private Context mContext;
    private int mStatusBarHeight;
    private int mStatusRemindHeight;
    private String mStatusRemindText;
    private final SettingsObserver mSettingsObserver;
    private final HotspotControllerImpl mHotspotController;
    private final Callback mCallback = new Callback();
    private final ConnectivityManager mConnectivityManager;
    private final ActivityManager mActivityManager;
    private TelephonyManager mTelephonyManager;
    private StatusBarCallTimer mCallTimer;
    private StatusBarCallTimer mRecordTimer;
    private long mPhoneOffHook = 0;
    private int mCallTimeCount;
    private int mTimeCount = 0;
    private boolean mCallState = false;
    private long mDefaultTime = 0;
    private boolean mCallTimeVisible;
    public static HashSet mAppHashSet = new HashSet();
    static {
        mAppHashSet.add("com.android.soundrecorder");
        mAppHashSet.add("com.android.dialer");
        mAppHashSet.add("com.lewa.flashlight");
    }

    public StatusRemindController(Context context) {
        mContext = context;
        mStatusBarHeight = context.getResources().getDimensionPixelSize(
            com.android.internal.R.dimen.status_bar_height);
        mStatusRemindHeight = context.getResources().getDimensionPixelSize(R.dimen.status_bar_remind_height);
        putStatusRemindHeight(mStatusBarHeight);
        statusSettingsReset(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.TETHER_CONNECT_STATE_CHANGED);
        filter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        filter.addAction(ACTION_SOUND_REC);
        filter.addAction(ACTION_INCALL_UI);
        context.registerReceiver(this, filter);
        mSettingsObserver = new SettingsObserver(new Handler());
        mSettingsObserver.observe();
        mHotspotController = new HotspotControllerImpl(context);
        mHotspotController.addCallback(mCallback);
        mConnectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        createCallTimer();
        createRecordTimer();
        updateRingTone();
    }


    public static void statusSettingsReset(Context context) {

        Settings.System.putInt(context.getContentResolver(),ExtraSettings.System.TORCH_STATE,0);

        Settings.System.putInt(context.getContentResolver(),REC_MODE_SETTINGS,0);

        Settings.System.putInt(context.getContentResolver(),REC_MODE_SETTINGS,0);

        Settings.System.putInt(context.getContentResolver(),INCALL_UI_SETTINGS,0);
    }

    public void addStateChangedCallback(StatusStateChangeCallback cb) {
        mChangeCallbacks.add(cb);
        cb.onStatusRemindChanged(mIntent,mRemindMode);
    }

    public void removeStateChangedCallback(StatusStateChangeCallback cb) {
        mChangeCallbacks.remove(cb);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(ConnectivityManager.TETHER_CONNECT_STATE_CHANGED)) {
            updateStatusSave();
        } else if (action.equals(ACTION_SOUND_REC)) {
            int value = intent.getIntExtra("state",REC_PAUSE);
            mTimeCount = intent.getIntExtra("time",0);
            switch(value) {
                case REC_START_RESUME:
                startRecordTimer();
                break;
                case REC_PAUSE:
                pauseRecordTimer();
                break;
                case REC_STOP:
                cancelRecordTimer();
                break;
            }
        } else if (action.equals(ACTION_INCALL_UI)){
            int value = intent.getIntExtra("state",INCALL_UI_CANCEL);
            mCallTimeCount = (int)(intent.getLongExtra("time",mDefaultTime));
            switch (value) {
                case INCALL_UI_START_TIME:
                mCallState = true;
                mCallTimeVisible = true;
                startCallTimer();
                break;
                case INCALL_UI_START_NOTIME:
                mCallState = true;
                mCallTimeVisible = false;
                break;
                case INCALL_UI_CANCEL:
                mCallState = false;
                mCallTimeVisible = false;
                cancelCallTimer();
                break;
            }
            updateStatusSave();
        }
    }

    public void putStatusRemindHeight(int sHeight) {
        ContentResolver cr = mContext.getContentResolver();
        Settings.System.putInt(cr,STATUS_REMIND_HEIGHT, sHeight);
    }

    private String getTopActivity() {
        if (mActivityManager == null) return null;
        List<RunningTaskInfo> runningTaskInfos = mActivityManager.getRunningTasks(1);
        if(runningTaskInfos != null) {
            return (runningTaskInfos.get(0).topActivity.getPackageName()).toString();
        } else {
            return null;
        }
    }

    public String getStatusRemindText() {
        return mStatusRemindText;
    }

    private String getStringFromRes(int resId) {
        return mContext.getResources().getString(resId);
    }

    private void updateStatusSave() {
        boolean stateRec = getSoundRecState();
        boolean stateTorch = getTorchState();
        boolean stateWifi = getWifiAPStatus();
        String topActivity = getTopActivity();
        /*if (mAppHashSet.contains(topActivity)) 
            return;*/
        if (mCallState) {
            mIntent = getInCallIntent();
            mStatusRemindMode = true;
            mStatusRemindText = getStringFromRes(com.lewa.systemuiext.R.string.status_remind_incall);
            mRemindMode = MODE_INCALL_UI;
            putStatusRemindHeight(mStatusRemindHeight + mStatusBarHeight);
            fireStatusLevelChanged();
            return;
        } else if (stateRec) {
            mIntent = getSoundRecIntent();
            mStatusRemindMode = true;
            if (getSoundRecStateValue() == 1) {
                 mStatusRemindText = getStringFromRes(com.lewa.systemuiext.R.string.status_remind_rec_start);
            } else {
                 mStatusRemindText = getStringFromRes(com.lewa.systemuiext.R.string.status_remind_rec_pause);
            }
            mRemindMode = MODE_SOUND_REC;
            putStatusRemindHeight(mStatusRemindHeight + mStatusBarHeight);
            fireStatusLevelChanged();
            return;
        } else if (stateTorch) {
            mIntent = getLightIntent();
            mStatusRemindMode = true;
            mStatusRemindText = getStringFromRes(com.lewa.systemuiext.R.string.status_remind_light);
            mRemindMode = MODE_TORCH_UP;
            putStatusRemindHeight(mStatusRemindHeight + mStatusBarHeight);
            fireStatusLevelChanged();
            return;
        } else if (stateWifi){
            mIntent = getWifiApIntent();
            mStatusRemindMode = true;
            mStatusRemindText = getStringFromRes(com.lewa.systemuiext.R.string.status_remind_wifiap) 
            + getWifiAPStatusValue() + getStringFromRes(com.android.systemui.R.string.status_remind_wifiap_connect);
            mRemindMode = MODE_WIFI_AP;
            putStatusRemindHeight(mStatusRemindHeight + mStatusBarHeight);
            fireStatusLevelChanged();
            return;
        } else {
            mIntent = null;
            mStatusRemindMode = false;
            mCallTimeVisible = false;
            mRemindMode = MODE_TRANSPARENT;
            putStatusRemindHeight(mStatusBarHeight);
            fireStatusRemoveChanged();
        }
    }


    private boolean getTorchState() {
        return Settings.System.getInt(mContext.getContentResolver(),
                ExtraSettings.System.TORCH_STATE,
                0) == 0 ? false : true;
    }

    private int getSoundRecStateValue(){
        return Settings.System.getInt(mContext.getContentResolver(),REC_MODE_SETTINGS,0);
    }

    private boolean getSoundRecState() {
        return Settings.System.getInt(mContext.getContentResolver(),REC_MODE_SETTINGS,0) > 0 ? true : false;
    }

    private boolean getWifiAPStatus(){
        int size;
        if (mConnectivityManager == null) return false;
        mWifiDevice = mConnectivityManager.getTetherConnectedSta();
        if (mWifiDevice != null) {
            size = mWifiDevice.size();
        } else {
            size = 0;
        }
        if (size > 0) {
            return true;
        } else {
            return false;
        }
    }

    private int getWifiAPStatusValue(){
        int size;
        if (mConnectivityManager == null) return 0;
        mWifiDevice = mConnectivityManager.getTetherConnectedSta();
        if (mWifiDevice != null) {
            size = mWifiDevice.size();
        } else {
            size = 0;
        }
        return size;
    }


    public boolean getStatusRemindMode() {
        return mStatusRemindMode;
    }

    public boolean getCallTimeVisible(){
        return mCallTimeVisible;
    }

    private void fireStatusLevelChanged() {
        final int N = mChangeCallbacks.size();
        for (int i = 0; i < N; i++) {
            mChangeCallbacks.get(i).onStatusRemindChanged(mIntent,mRemindMode);
        }
    }

    private void fireStatusCallTimeChanged(int time) {
        final int N = mChangeCallbacks.size();
        for (int i = 0; i < N; i++) {
            mChangeCallbacks.get(i).onStatusCallTimeChanged(mRemindMode,time);
        }
    }

    private void fireStatusRecordTimeChanged(int time) {
        final int N = mChangeCallbacks.size();
        for (int i = 0; i < N; i++) {
            mChangeCallbacks.get(i).onStatusTimeChanged(mRemindMode,time);
        }
    }

    private void fireStatusRemoveChanged() {
        final int N = mChangeCallbacks.size();
        for (int i = 0; i < N; i++) {
            mChangeCallbacks.get(i).onStatusRemindMode(mRemindMode);
        }
    }

    public Intent getInCallIntent() {
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        intent.setClassName("com.android.dialer",
            "com.android.incallui.InCallActivity");
        intent.putExtra(NEW_OUTGOING_CALL, false);
        return intent;
    }

    public Intent getLightIntent() {
        final Intent intent = new Intent();
        intent.setClassName("com.lewa.flashlight",
                "com.lewa.flashlight.FlashlightActivity");
        return intent;
    }

    public Intent getSoundRecIntent() {
        final Intent intent = new Intent();
        intent.setClassName("com.android.soundrecorder",
                "com.android.soundrecorder.RecordActivity");
        return intent;
    }

    public Intent getWifiApIntent() {
        final Intent intent = new Intent();
        intent.setAction("android.settings.TETHER_SETTINGS");
        return intent;
    }


    private void createCallTimer() {
        mCallTimer = new StatusBarCallTimer (new Runnable() {
            @Override
            public void run() {
                updateCallTime();
            }
        });
    }

    private void createRecordTimer() {
        mRecordTimer = new StatusBarCallTimer(new Runnable() {
            @Override
            public void run() {
                updateRecordTime();
            }
        });
    }

    private void startCallTimer() {
        if (mCallTimer != null) {
            mCallTimer.start(CALL_TIME_UPDATE_INTERVAL_MS);
        }
    }

    private void startRecordTimer() {
        if (mRecordTimer != null) {
            mRecordTimer.start(CALL_TIME_UPDATE_INTERVAL_MS);
        }
    }

    private void cancelRecordTimer() {
        if (mRecordTimer != null) {
            mRecordTimer.cancel();
            mTimeCount = 0;
        }
    }

    private void cancelCallTimer() {
        if (mCallTimer != null) {
            mCallTimer.cancel();
            mCallTimeCount = 0;
        }
    }

    private void pauseRecordTimer() {
        if (mRecordTimer != null) {
            mRecordTimer.cancel();
            fireStatusRecordTimeChanged(mTimeCount);
        }
    }

    private void updateCallTime() {
        fireStatusCallTimeChanged(mCallTimeCount);
        mCallTimeCount ++;
    }

    private void updateRecordTime() {
        fireStatusRecordTimeChanged(mTimeCount);
        mTimeCount ++;
    }

    private String getRingTone() {
        final String uriString = Settings.System.getString(mContext.getContentResolver(), Settings.System.RINGTONE);
        if (uriString == null) {
            return null;
        }
        return uriString;
    }

    private String getRingTone2() {
        final String uriString = Settings.System.getString(mContext.getContentResolver(), Settings.System.RINGTONE + "_" + 2);
        if (uriString == null) {
            return null;
        }
        return uriString;
    }

    private String getNotificationSound() {
        final String uriString = Settings.System.getString(mContext.getContentResolver(), Settings.System.NOTIFICATION_SOUND);
        if (uriString == null) {
            return null;
        }
        return uriString;
    }

    private void updateRingTone() {
        Log.d("mseven","Settings.System.DEFAULT_RINGTONE_URI:"+Settings.System.DEFAULT_RINGTONE_URI.toString());
        Log.d("mseven","Settings.System.DEFAULT_RINGTONE_URI_2:"+Settings.System.DEFAULT_RINGTONE_URI_2.toString());
        Log.d("mseven","getRingTone:"+getRingTone());
        Log.d("mseven","getRingTone2:"+getRingTone2());
        Log.d("mseven","getNotificationSound:"+getNotificationSound());
    }

    private class SettingsObserver extends ContentObserver {
        private final Uri TORCH_MODE = Settings.System.getUriFor(ExtraSettings.System.TORCH_STATE);
        private final Uri REC_MODE = Settings.System.getUriFor(REC_MODE_SETTINGS);
        private final Uri NOTIFICATION_SOUND = Settings.System.getUriFor(Settings.System.NOTIFICATION_SOUND);
        private final Uri RINGTONE_MODE = Settings.System.getUriFor(Settings.System.RINGTONE);
        private final Uri RINGTONE_MODE2 = Settings.System.getUriFor(Settings.System.RINGTONE + "_" + 2);
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(TORCH_MODE, false , this);
            resolver.registerContentObserver(REC_MODE, false , this);
            resolver.registerContentObserver(RINGTONE_MODE, false , this);
            resolver.registerContentObserver(RINGTONE_MODE2, false , this);
            resolver.registerContentObserver(NOTIFICATION_SOUND,false,this);
            update(null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
            updateRingTone();
        }

        public void update(Uri uri) {
            updateStatusSave();
        }
    }

    private void refreshState(boolean enabled) {
        if (!enabled) {
            updateStatusSave();
        }
    }


    public static long getCallDuration(Call call) {
        long duration = 0;
        List connections = call.getConnections();
        int count = connections.size();
        Connection c;
        if (count == 1) {
            c = (Connection) connections.get(0);
            duration = c.getDurationMillis();
        } else {
            for (int i = 0; i < count; i++) {
                c = (Connection) connections.get(i);
                long t = c.getDurationMillis();
                if (t > duration) {
                    duration = t;
                }
            }
        }
        return duration;
    }

    public long getConnectTimeMillis() {
        return mPhoneOffHook;
    }

    public void removePhoneListener() {
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    mPhoneOffHook = SystemClock.elapsedRealtime();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                default:
                    break;
            }
        }
    };

    private final class Callback implements HotspotController.Callback {
        @Override
        public void onHotspotChanged(boolean enabled) {
            refreshState(enabled);
        }
    };

    public interface StatusStateChangeCallback {
        void onStatusRemindChanged(final Intent intent,final int mode);
        void onStatusRemindMode(final int mode);
        void onStatusTimeChanged(final int mode,int time);
        void onStatusCallTimeChanged(final int mode,int time);
    }

}
