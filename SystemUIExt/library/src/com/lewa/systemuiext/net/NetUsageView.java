package com.lewa.systemuiext.net;

import static com.lewa.systemuiext.net.FormatUtils.combindString;
import static com.lewa.systemuiext.net.FormatUtils.formatShorterSize;
import static com.lewa.systemuiext.net.FormatUtils.formatSize;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.NetworkPolicy;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lewa.systemuiext.R;

import java.util.Timer;
import java.util.TimerTask;
import com.lewa.systemuiext.adapter.StatusBarServiceAdapter;

/**
 * @author juude.song@gmail.com description: this is the net usage view, it uses
 *         netusage.xml as the layout file
 */

public class NetUsageView extends RelativeLayout implements
        StatusBarServiceAdapter.onExpandedChangeListener {
    private static final String TAG = "NetUsageView";
    private Context mContext;
    private static final boolean DEBUG = false;
    private Intent mUpdateIntent = new Intent(
            "com.lewa.netmgr.APPWIDGET_UPDATE");
    public final static String ACTION_DATA_UPDATED = "lewa.intent.action.ACTION_DATA_UPDATED";

    private static final int NET_USAGE_INTERVAL = 5 * 1000;
    private Timer mTimer;
    public static final String KEY_STATUS_BAR_NET_USAGE = "status_bar_net_usage";
    private boolean mShowNetUsage = false;
    private Handler mHandler = new Handler();
    private NetUsageReceiver mReceiver = null;
    private TextView mMainText;
    private TextView mSubText;
    private ImageView mProgress;
    private boolean bServiceStarted = false;

    public NetUsageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        if (isInEditMode()) {
            return;
        }
        (new SettingsObserver(mHandler)).observe();

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClassName("com.lewa.netmgr",
                        "com.lewa.netmgr.ManagerActivity");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(i);
                if (DEBUG)
                Log.d(TAG, "NetUsageView : is onClick ");
            }
        });
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            if (DEBUG)
            Log.d(TAG, "NetUsageView : observe");
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(KEY_STATUS_BAR_NET_USAGE), false,
                    this);
            onChange(false);
        }

        @Override
        public void onChange(boolean selfChange) {
           if (DEBUG)
            Log.d(TAG, "NetUsageView : onChange");
            updateSettings();
            setVisibility(mShowNetUsage ? View.VISIBLE : View.GONE);
        }

    }

    @Override
    public void onExpandedChanged(boolean visible) {
        if (DEBUG)
        Log.d(TAG, "onExpandedVisible  collapsePanel visible = " + visible
                + "isVisibleToUser" + isVisibleToUser());
        if (visible) {
            if (isVisibleToUser()) {
                startListening();
            }
        } else {
            stopListening();
        }
    }

    public void startListening() {
        if (bServiceStarted) {
            return;
        }
        mContext.sendBroadcast(mUpdateIntent);
        if (DEBUG)
            Log.d(TAG, "sendBroadcast : " + mUpdateIntent);
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mContext.sendBroadcast(mUpdateIntent);
                if (DEBUG)
                    Log.d(TAG, "sendBroadcast : " + mUpdateIntent);
            }
        }, NET_USAGE_INTERVAL, NET_USAGE_INTERVAL);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(ACTION_DATA_UPDATED);
        mReceiver = new NetUsageReceiver();
        mContext.registerReceiver(mReceiver, filter);
        bServiceStarted = true;
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();
        mShowNetUsage = (Settings.System.getInt(resolver,
                KEY_STATUS_BAR_NET_USAGE, 0) == 1);
    }

    public void stopListening() {
        if (!bServiceStarted) {
            return;
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        bServiceStarted = false;
    }

    private class NetUsageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
           if (DEBUG)
            Log.d(TAG, "intent.getAction():" + intent.getAction());
            if (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
                // if (mShowNetUsage) {
                // final boolean landscape =
                // mContext.getResources().getConfiguration().orientation ==
                // Configuration.ORIENTATION_LANDSCAPE;
                // setVisibility(landscape ? View.GONE : View.VISIBLE);
                // }
            } else if (ACTION_DATA_UPDATED.equals(intent.getAction())) {
                updateView(intent.getExtras());
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        mMainText = (TextView) findViewById(android.R.id.text1);
        mSubText = (TextView) findViewById(android.R.id.text2);
        mProgress = (ImageView) findViewById(R.id.progress);
        super.onFinishInflate();
        updateSettings();
        if (mShowNetUsage
                && mContext.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            setVisibility(View.VISIBLE);
        }

    }

    public void updateView(Bundle extras) {
        long todayBytes = extras.getLong("today");
        boolean disabled = extras.getBoolean("disabled");
        long totalBytes = extras.getLong("total");
        long limitBytes = extras.getLong("limit");
        long warningBytes = extras.getLong("warning");
        boolean exceed = totalBytes > limitBytes;
        int defaultColor = mContext.getResources().getColor(
                R.color.systemuiPrimaryColor);
        int red = mContext.getResources().getColor(
                android.R.color.holo_red_light);
        setBackgroundResource(R.drawable.netusage_background);
        LayoutParams progressLP = (LayoutParams) mProgress.getLayoutParams();
        progressLP.width = 0;
        mSubText.setTextColor(defaultColor);
        if (totalBytes > warningBytes) {
            setBackgroundResource(R.drawable.netusage_background_overlimit);
        }
        if (disabled) {
            mMainText.setText(mContext
                    .getString(R.string.widget_disabled_mobile));
            mSubText.setText("");
        } else {
            if (limitBytes == NetworkPolicy.LIMIT_DISABLED) {
                mSubText.setText(mContext
                        .getString(R.string.widget_data_pack_warning));
            } else {
                if (exceed) {
                    setBackgroundResource(R.drawable.netusage_background_overlimit);
                    mSubText.setTextColor(red);
                    mSubText.setText(combindString(mContext,
                            R.string.widget_exceed,
                            formatSize(mContext, totalBytes - limitBytes)));
                } else {
                    mSubText.setText(combindString(
                            mContext,
                            R.string.widget_remain,
                            formatShorterSize(limitBytes - totalBytes,
                                    limitBytes)));
                    progressLP.width = (int) (getMeasuredWidth() * totalBytes
                            * 1.0 / limitBytes);
                }
            }
            mMainText.setText(combindString(mContext,
                    R.string.widget_today_usage,
                    formatSize(mContext, todayBytes)));
            mSubText.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            mMainText.setVisibility(View.VISIBLE);
        }
        updateViewLayout(mProgress, progressLP);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (DEBUG)
        Log.d(TAG, "onVisibilityChanged = visibility" + visibility);
        if (visibility == View.VISIBLE) {
            startListening();
        } else {
            stopListening();
        }
    }
}
