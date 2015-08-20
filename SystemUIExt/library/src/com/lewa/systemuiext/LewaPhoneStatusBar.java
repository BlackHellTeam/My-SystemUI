package com.lewa.systemuiext;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.PatternMatcher;

//
//import com.android.systemui.statusbar.phone.ImageView;
//import com.android.systemui.statusbar.phone.LewaBatteryController;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.lewa.systemuiext.utils.ShakeListener;
import com.lewa.systemuiext.widgets.SlidingUpPanel;
import com.lewa.systemuiext.widgets.StatusBarSwitchLayout;

import java.lang.reflect.Field;

public class LewaPhoneStatusBar extends PhoneStatusBar implements ShakeListener.CallBack{
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefChangedListener;

    private SharedPreferences mStatusbar_settings;
    private KeyguardManager mKM;
    private StatusBarWindowView mStatusBarWindowView;
    private boolean mHasNetMgr = false;
    private StatusBarSwitchLayout mSwtichLayout;
    private Display mDisplay;
    private DisplayMetrics mDisplayMetrics;
    private LinearLayout mExpandViewLayout;
    private TextView mTabSwitches;
    private TextView mTabNotifications;
    private boolean mLastScrollViewIsMaxHeight = false;
    private boolean mScrollViewIsMaxHeight = true;
    private ShakeListener mShakeListener;
    public static final String TAG = "LewaPhoneStatusBar";
    public static boolean LEWA_SYSTEMUI = true;
    private IntentFilter mIntentFilter;
    public static final String ACTION_THEME_CHANGED = "com.lewa.intent.action.THEME_CHANGED";

    private boolean mSinglePage;

    private SlidingUpPanel mSlidingUpPanel;
    public static boolean CONFIG_HIDE_NETWORK_TYPE;
    public LewaPhoneStatusBar(Context context) {
        super(context);
        init();
        sInstance = this;
    }
    
    private void loadConfigs() {
        CONFIG_HIDE_NETWORK_TYPE = mContext.getResources().getBoolean(R.bool.config_hide_network_type);
    }
    
    private void setPageModeListener() {
        Log.d(TAG, "setPageModeListener");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSinglePage = prefs.getBoolean(Constants.PREF_NOTITIFACTION_STYLE, Constants.NOTIFICATION_SINGLE_PAGE);
        onPageChanged();
        mPrefChangedListener = 
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    Log.d(TAG, "Settings key changed: " + key);
                    if(key.equals(Constants.PREF_NOTITIFACTION_STYLE)) {
                        mSinglePage = prefs.getBoolean(key, Constants.NOTIFICATION_SINGLE_PAGE);
                        Log.d(TAG, "mSinglePage : " + mSinglePage);
                        onPageChanged();
                    }
                }
            };
        prefs.registerOnSharedPreferenceChangeListener(mPrefChangedListener);
    }
    
    @Override
    protected PhoneStatusBarView makeStatusBarView() { 
        PhoneStatusBarView statusBarView = super.makeStatusBarView();
        Field filedStatusBarWindow;
        try {
            filedStatusBarWindow = PhoneStatusBar.class.getDeclaredField("mStatusBarWindow");
            filedStatusBarWindow.setAccessible(true);
            mStatusBarWindowView = (StatusBarWindowView) filedStatusBarWindow.get(this);
        } catch (Exception e) {
            Log.e(TAG, "", new Throwable());
        }
        
        mShakeListener = new ShakeListener(mContext, this);
//        mLewaBatteryController = new LewaBatteryController(mContext);
//        Log.d(TAG, "battery_image : " + mStatusBarWindowView.findViewById(R.id.battery_image));
//        mLewaBatteryController.addIconView((ImageView)mStatusBarWindowView.findViewById(R.id.battery_image));
//        mLewaBatteryController.addLabelView((TextView)mStatusBarWindowView.findViewById(R.id.battery_percentage));
//        mLewaBatteryController.addComboView((ViewGroup)mStatusBarWindowView.findViewById(R.id.battery_combo));
        initView();
        if(Build.VERSION.SDK_INT < 19) {
        //TODO: use reflection PhoneStatusBarInjector.injectTransparentManager(mStatusBarView);
        }
        mStatusbar_settings = mContext.getSharedPreferences("statusbar_settings", 0);
        mKM = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
//        updateClockAndDateView();
//        setCarrierLableListener();
        mContext.registerReceiver(mBroadcastReceiver, getThemeFilter());
        return statusBarView;
    }
    
    private Bundle getStatusBarExtras() {
        Bundle bundle = new Bundle();
//        bundle.putBoolean("translucent", (mStatusBarMode != com.android.systemui.statusbar.phone.BarTransitions.MODE_OPAQUE));
//        bundle.putBoolean("showing", mStatusBarWindowState == android.app.StatusBarManager.WINDOW_STATE_SHOWING);
        return bundle;
    }

    
    private void init() {
        //start corner servive
        loadConfigs();
        
//        Intent i = new Intent();
//        i.setClassName("mohammad.adib.roundr", "mohammad.adib.roundr.RoundrService");
//        i.replaceExtras(getStatusBarExtras());
//        mContext.startService(i);
        SystemUIExtApplication.onCreate();
    }
    
    private void onPageChanged() {
        mSwtichLayout.onModeChanged(mSinglePage == Constants.NOTIFICATION_SINGLE_PAGE);
        mSlidingUpPanel.onModeChanged(mSinglePage == Constants.NOTIFICATION_SINGLE_PAGE);
    }
    
    public void initView() {
        mSwtichLayout = (StatusBarSwitchLayout)mStatusBarWindowView.findViewById(R.id.statusbarswitchlayout);
        mSwtichLayout.setTabButtons(mStatusBarWindowView.findViewById(R.id.tabs_linear));
        mSlidingUpPanel = (SlidingUpPanel)mStatusBarWindowView.findViewById(R.id.slidingPanel);
        setPageModeListener();
    }

    @Override
    public void onShake(){
//        if(mClearButton == null) {
//            return;
//        }
//        if(mClearButton.isEnabled()) {
//            Vibrator vibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE); 
//            vibrator.vibrate(100); 
//            Log.d(TAG, "shaked ");
//            mClearButton.performClick();
//        }    
    }

     private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive, action=" + action);  
            if(action.equals(ACTION_THEME_CHANGED)) {
               //updateNotificationIcons();
               recreateStatusBar();
            }
        }
     }; 
     
     private IntentFilter getThemeFilter() {
        if(mIntentFilter != null) {
            return mIntentFilter;
        }
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_THEME_CHANGED);
        mIntentFilter.addDataScheme("content");
        try {
            mIntentFilter.addDataType("vnd.lewa.cursor.item/theme");
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(TAG, "Could not add MIME types to filter", e);
        }
        mIntentFilter.addDataAuthority("com.lewa.themechooser.themes", null);
        mIntentFilter.addDataPath("/theme", PatternMatcher.PATTERN_PREFIX);
        return mIntentFilter;
    }

      /**
    * For Debug
    */
    private static LewaPhoneStatusBar sInstance = null;
    public static LewaPhoneStatusBar getInstance() {
        return sInstance;
    }

    
}
