
package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.content.res.Configuration;
import android.widget.SeekBar;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
import android.os.IPowerManager;
/**
 * TODO: use just one Handler
 */
public class BrightnessView extends LinearLayout {
    private int mMinimumScreenBrightnessSetting;
    private int mMaximumScreenBrightnessSetting;
    private SeekBar mSb;
    private int mOldBrightness;
    private static final int SEEK_BAR_RANGE = 255;
    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT = 
            Resources.getSystem().getBoolean(
                    lewa.R.bool.config_use_screen_auto_brightness_adjustment);
    public static final String TAG = "BrightnessView";
    private Context mContext;
    private BrightnessSettingsObserver mObserver;
    private RelativeLayout mBrightnessControlView;

    public BrightnessView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 17) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                mMinimumScreenBrightnessSetting = (Integer) pm.getClass()
                        .getMethod("getMinimumScreenBrightnessSetting").invoke(pm);
                mMaximumScreenBrightnessSetting = (Integer) pm.getClass()
                        .getMethod("getMaximumScreenBrightnessSetting").invoke(pm);
            }
            else {
                mMinimumScreenBrightnessSetting = getContext().getResources().getInteger(
                        lewa.R.integer.android_config_screenBrightnessDim);
                mMaximumScreenBrightnessSetting = android.os.PowerManager.BRIGHTNESS_ON;
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mObserver == null) {
            mObserver = new BrightnessSettingsObserver(sHandler);
        }
        mObserver.observe();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateConfiguration();
    }
    
    private void updateConfiguration() {        
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams)mBrightnessControlView.getLayoutParams();
        Resources resources = mContext.getResources();

        int newWidth = resources.getDimensionPixelSize(R.dimen.status_bar_switchwidget_width);

        if(newWidth != lp.width) {
            lp.width = newWidth;
            updateViewLayout(mBrightnessControlView, lp);
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mObserver.unobserve();
    }

    private static Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }

    };

    private int getBrightness(int defaultValue) {
        int mode = getBrightnessMode(0);
        float brightness = defaultValue;
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            brightness = Settings.System.getFloat(getContext().getContentResolver(),
                    Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, defaultValue);
            brightness = (brightness + 1) / 2;
        } else {
            brightness = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, defaultValue);
            brightness = (brightness - mMinimumScreenBrightnessSetting)
                    / (mMaximumScreenBrightnessSetting - mMinimumScreenBrightnessSetting);
        }
        return (int) (brightness * SEEK_BAR_RANGE);
    }

    private int getBrightnessMode(int defaultValue) {
        int brightnessMode = defaultValue;
        try {
            brightnessMode = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException snfe) {
        }
        return brightnessMode;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int mode = getBrightnessMode(0);
        mSb = (SeekBar) findViewById(R.id.seekbright);
        Drawable thumb = mContext.getResources().getDrawable(
                R.drawable.seek_bar_thumb);
        Drawable progress = mContext.getResources().getDrawable(
                R.drawable.seek_bar_background);
        mSb.setThumb(thumb);
        mSb.setProgressDrawable(progress);
        mSb.setMax(SEEK_BAR_RANGE);
        mOldBrightness = getBrightness(0);
        mSb.setProgress(mOldBrightness);
        mSb.setOnSeekBarChangeListener(seekbarChangeListener);
        mSb.setFocusable(false);
        mSb.setFocusableInTouchMode(false);
        mSb.setClickable(false);
       /* if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            hideBrightnessView();
        } else {
            showBrightnessView();
        }*/
        updateBrightnessValue();
        mBrightnessControlView = (RelativeLayout)findViewById(R.id.brightnessControlView);
    }

    private void updateBrightnessValue() {
        int brightnessValue = getBrightness(0);
        if (mSb != null) {
            mSb.setProgress(brightnessValue);
        }
    }

    private void showBrightnessView() {
        setVisibility(View.VISIBLE);
    }

    private void hideBrightnessView() {
        if (!USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT) {
            setVisibility(View.INVISIBLE);
        }
    }

    private SeekBar.OnSeekBarChangeListener seekbarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            setBrightness(progress, false);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            setBrightness(mSb.getProgress(), true);
        }

    };

    private class BrightnessSettingsObserver extends ContentObserver {

        public BrightnessSettingsObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), false, this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), false, this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ), false,
                    this);
        }

        private void unobserve() {
            ContentResolver resolver = getContext().getContentResolver();
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri.equals(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE))) {
                /*int mode = getBrightnessMode(0);
                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    hideBrightnessView();
                } else {
                    showBrightnessView();
                }*/
                 updateBrightnessValue();
            } else if (uri.equals(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS))) {
                updateBrightnessValue();
            } else if (uri.equals(Settings.System
                    .getUriFor(Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ))) {
                updateBrightnessValue();
            }
        }
    }

    private void setBrightness(int brightness) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                ServiceManager.getService("power"));
            power.setTemporaryScreenBrightnessSettingOverride(brightness);
        } catch (RemoteException ex) {
        }
    }

    private void setBrightnessAdj(float adj) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                ServiceManager.getService("power"));
            power.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(adj);
        } catch (RemoteException ex) {
        }
    }

    private void setBrightness(int brightness, boolean write) {
        int mode = getBrightnessMode(0);
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            float valf = ((float) brightness * 2) / (SEEK_BAR_RANGE) - 1.0f;
            setBrightnessAdj(valf);
                if (write) {
                    final ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putFloat(resolver,
                            Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, valf);
                }
        } else {
            int range = (mMaximumScreenBrightnessSetting - mMinimumScreenBrightnessSetting);
            brightness = (brightness * range) / SEEK_BAR_RANGE + mMinimumScreenBrightnessSetting;
            setBrightness(brightness);
                if (write) {
                    final ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putInt(resolver,
                            Settings.System.SCREEN_BRIGHTNESS, brightness);
                }
        }
    }
}
