package com.lewa.systemuiext.utils;

import lewa.support.v7.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
/// 51485 haosu 2014.05.14 BEGIN
import android.preference.PreferenceCategory;
/// 51485 haosu 2014.05.14 END
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;
import android.provider.Settings;
import lewa.provider.ExtraSettings;
import android.view.MenuItem;

import com.lewa.systemuiext.Constants;
import com.lewa.systemuiext.R;
import com.lewa.systemuiext.net.NetSpeedText;
import com.lewa.systemuiext.net.NetUsageView;
import com.lewa.systemuiext.switchwidget.SwitchWidgetSettings;
import lewa.support.v7.app.ActionBarActivity;
import android.preference.PreferenceFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.preference.PreferenceScreen;
import android.app.FragmentManager;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class PhoneStatusbarSettings extends ActionBarActivity {
    private static final String KEY_NETSPEED_SETTING = "netspeed_setting";
    private static final String KEY_NETUSAGE_SETTING = "netusage_setting";

    public static final String KEY_TRANS_STATUSBAR_SETTING = "trans_statusbar_key";

    // / 51485 haosu 2014.05.14 BEGIN
    public static final String KEY_SHAKE_CLEAN = "shake_clean";
    public static final String KEY_STATUSBAR_SETTINGS = "key_statusbarsettings";
    // / 51485 haosu 2014.05.14 END

    private SharedPreferences mStatusbar_settings;
    private Editor mStatusbar_settingEditor;
    CheckBoxPreference mDouble_page;
    SwitchPreference mTransparent;
    SwitchPreference mLockExpandable;
    SwitchPreference mNetSpeed;
    SwitchPreference mNetUsage;
    SwitchPreference mCDMAShow;
    Preference mSwitchWidgetSet;
    Preference mNotifilterSet;
    String ifdouble;
    String ifdisusage;
    private static final String KEY_ACTION_UP_DOWN = "action_updown";
    private static final String COLORFUL_ACTION = "com.lewa.intent.action.KILLPROCESSES_DONE";
    private SwitchPreference mPrefActionUpDown;
    protected static final String EXTRA_PREFS_SET_NOTIFICATION_BACK = "extra_prefs_set_notification_back";
    protected static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";
    private PhonestatusbarSettingPreferenceFragment fragment1;
    private Context mContext;

    // android:widgetLayout="@lewa:layout/preference_widget_switch_noclickable"
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        FragmentManager fragmentManager = getFragmentManager(); 
        FragmentTransaction fragmentTransaction =  
            fragmentManager.beginTransaction(); 
        fragment1 = new PhonestatusbarSettingPreferenceFragment(); 
        fragmentTransaction.replace(android.R.id.content, fragment1);
        fragmentTransaction.addToBackStack(null);  
        fragmentTransaction.commit(); 
        IntentFilter iflter = new IntentFilter();
        iflter.addAction(COLORFUL_ACTION);
        registerReceiver(mReceiver, iflter);
        getSupportActionBar().setTitle(R.string.statusbarsettings);
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
    }
    class PhonestatusbarSettingPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) { 
            super.onCreate(savedInstanceState); 
            mContext = getActivity();
            mStatusbar_settings = mContext.getSharedPreferences(
                    "statusbar_settings", 0);
            mStatusbar_settingEditor = mStatusbar_settings.edit();
            addPreferencesFromResource(R.xml.phonestatusbarsettings);
            ifdouble = getResources().getString(R.string.settings_double_status);
            // mDouble_page = (CheckBoxPreference) this.findPreference(ifdouble);
            int layoutId = getCheckPreferenceWidgetLayout(mContext);
            mSwitchWidgetSet = findPreference("key_widgetbuttongsettings");
           // mSwitchWidgetSet.setOnPreferenceClickListener(this);

            mNotifilterSet = findPreference("key_notifilter_setting");
//            /mNotifilterSet.setOnPreferenceClickListener(this);

            mTransparent = (SwitchPreference)findPreference(KEY_TRANS_STATUSBAR_SETTING);
            if (mStatusbar_settings.getInt(KEY_TRANS_STATUSBAR_SETTING, 1) == 1) {
                mTransparent.setChecked(true);
            } else {
                mTransparent.setChecked(false);
            }
            mTransparent.setWidgetLayoutResource(layoutId);
            //mTransparent.setOnPreferenceClickListener(mContext);

            /*if (android.os.Build.VERSION.SDK_INT >= 19) {// jdsong add, cannot set
                                                         // this in versions above
                                                         // 19
                ((PreferenceGroup) getPreferenceScreen().getPreference(0))
                        .removePreference(mTransparent);
            }*/

            mLockExpandable = (SwitchPreference) this
                    .findPreference("lockscreen_expandable_key");
            mLockExpandable.setWidgetLayoutResource(layoutId);
            if (mStatusbar_settings.getInt("lockscreen_expandable_key", 1) == 1) {
                mLockExpandable.setChecked(true);
            } else {
                mLockExpandable.setChecked(false);
            }
            mCDMAShow = (SwitchPreference) this
                    .findPreference("cdma_show_two_line_key");
            if (null == mCDMAShow) {
                mStatusbar_settingEditor.putInt("cdma_show_two_line_key", 0)
                        .commit();
            } else {
                if (mStatusbar_settings.getInt("cdma_show_two_line_key", 1) == 1) {
                    mCDMAShow.setChecked(true);
                } else {
                    mCDMAShow.setChecked(false);
                }
                //mCDMAShow.setOnPreferenceClickListener(this);
            }
           //mLockExpandable.setOnPreferenceClickListener(this);

            mNetUsage = (SwitchPreference) findPreference(KEY_NETUSAGE_SETTING);
            //mNetUsage.setOnPreferenceClickListener(this);
            mNetUsage.setWidgetLayoutResource(layoutId);

            mNetSpeed = (SwitchPreference) findPreference(KEY_NETSPEED_SETTING);
           // mNetSpeed.setOnPreferenceClickListener(this);
            mNetSpeed.setWidgetLayoutResource(layoutId);
            final Intent intent = new Intent();
            intent.setClassName("com.lewa.netmgr",
                    "com.lewa.netmgr.ManagerActivity");
            if (getPackageManager().queryIntentActivities(intent, 0).size() == 0)
                ((PreferenceGroup) getPreferenceScreen().getPreference(0))
                        .removePreference(mNetSpeed);
            // / 51485 haosu 2014.05.14 BEGIN
            PreferenceCategory prefScreen = (PreferenceCategory) findPreference("key_statusbarsettings");
            Preference pref = null;
            pref = findPreference("shake_clean");
            pref.setWidgetLayoutResource(layoutId);
            if (pref != null
                    && !getResources().getBoolean(R.bool.enable_shake_clean)) {
                prefScreen.removePreference(pref);
            }
            if (mTransparent != null) {
                 prefScreen.removePreference(mTransparent);
            }
            // / 51485 haosu 2014.05.14 END
            mPrefActionUpDown = (SwitchPreference) findPreference(KEY_ACTION_UP_DOWN);
            //mPrefActionUpDown.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {

            final String key = preference.getKey();
     /*       if (key.equals(ifdouble)) {
                if (mDouble_page.isChecked())
                    mStatusbar_settingEditor.putInt(ifdouble, 1).commit();
                else
                    mStatusbar_settingEditor.putInt(ifdouble, 0).commit();
            } else */
                if (key.equals("key_widgetbuttongsettings")) {
                Intent intent = new Intent();
                intent.setClass(mContext, SwitchWidgetSettings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                this.startActivity(intent);
            } else if (key.equals(KEY_TRANS_STATUSBAR_SETTING)) {
                if (mTransparent.isChecked()) {
                    mStatusbar_settingEditor.putInt(KEY_TRANS_STATUSBAR_SETTING, 1)
                            .commit();

                } else {
                    mStatusbar_settingEditor.putInt(KEY_TRANS_STATUSBAR_SETTING, 0)
                            .commit();
                }
                Intent intentTrans = new Intent();
                intentTrans.setAction("trans_statusbar");
                PhoneStatusbarSettings.this.sendBroadcast(intentTrans);

            } else if ("key_notifilter_setting".equals(key)) {
                Intent intent = new Intent("android.settings.APP_NOTIFICATION_LIST");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EXTRA_PREFS_SET_NOTIFICATION_BACK,true);
                this.startActivity(intent);
            } else if ("lockscreen_expandable_key".equals(key)) {
                if (mLockExpandable.isChecked()) {
                    mStatusbar_settingEditor.putInt("lockscreen_expandable_key", 1)
                            .commit();
                } else {
                    mStatusbar_settingEditor.putInt("lockscreen_expandable_key", 0)
                            .commit();
                }
            } else if (preference == mNetUsage) {
                Settings.System.putInt(getContentResolver(),
                        NetUsageView.KEY_STATUS_BAR_NET_USAGE,
                        mNetUsage.isChecked() ? 1 : 0);
            } else if (preference == mNetSpeed) {
                Settings.System.putInt(getContentResolver(),
                        NetSpeedText.KEY_STATUS_BAR_NET_SPEED,
                        mNetSpeed.isChecked() ? 1 : 0);
            } else if ("cdma_show_two_line_key".equals(key)) {
                if (mCDMAShow.isChecked()) {
                    mStatusbar_settingEditor.putInt("cdma_show_two_line_key", 1)
                            .commit();
                } else {
                    mStatusbar_settingEditor.putInt("cdma_show_two_line_key", 0)
                            .commit();
                }
                showRebootDialog();
            } else if (key.equals(KEY_ACTION_UP_DOWN)) {
                final int value = mPrefActionUpDown.isChecked() ? 1 : 0;
                Settings.System.putInt(getContentResolver(),
                        ExtraSettings.System.LAUNCHER_ACTION_UP_DOWN, value);
                // Log.v("ZYY_", "Set Settings.System.LAUNCHER_ACTION_UP_DOWN:" +
                // value);
            }
            return false;
        
        }
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            PhoneStatusbarSettings.this.finish();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mNetUsage.setChecked(Settings.System.getInt(getContentResolver(),
                NetUsageView.KEY_STATUS_BAR_NET_USAGE, 0) == 1);
        mNetSpeed.setChecked(Settings.System.getInt(getContentResolver(),
                NetSpeedText.KEY_STATUS_BAR_NET_SPEED, 0) == 1);
    }

    private static int getCheckPreferenceWidgetLayout(Context context) {
        int checkLayoutId = context.getResources().getIdentifier(
                "preference_widget_switch_noclickable", "layout", "lewa");
        return checkLayoutId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void showRebootDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.reboot)
                .setMessage(R.string.reboot_message)
                .setPositiveButton(R.string.reboot_now_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                                pm.reboot("make statusbar");
                            }
                        })
                .setNegativeButton(R.string.reboot_later_confirm, null).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
