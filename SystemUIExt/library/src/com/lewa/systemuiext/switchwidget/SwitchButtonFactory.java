package com.lewa.systemuiext.switchwidget;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.lewa.systemuiext.Constants;
import com.lewa.systemuiext.R;
import com.lewa.systemuiext.adapter.TelephonyAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * TODO: 1. make it convenient to port old versions 2.
 */
public class SwitchButtonFactory {
    public static final String ACTION_SWITCH_WIDGET_CHANGED = "lewa.intent.ACTION_SWITCH_BUTTONS_CHANGED";
    public static final String ACTION_SWITCH_WIDGET_PAGE_CHANGE = "lewa.intent.ACTION_SWITCH_WIDGET_PAGE_CHANGE";
    private static final String TAG = "SwitchButtonFactory";
    private static final String BUTTONS_LAST_FIRST = "SwitchStyleButton";
    private static final String BUTTONS_LAST_SECOND = "BrightnessButton";
    private static final String KEY_SWITCH_BUTTONS = "KEY_SWITCH_BUTTONS";
    public static final String KEY_SOUND_BUTTONS = "SoundButton";
    private static boolean sDirty = false;
    private static SwitchButtonFactory sInstance;

    public static SwitchButtonFactory getInstance(Context context) {
        synchronized (SwitchButtonFactory.class) {
            if (sInstance == null) {
                sInstance = new SwitchButtonFactory(context);
            }
            return sInstance;
        }
    }

    private List<String> mButtons = new ArrayList<String>();
    private HashMap<String, SwitchButton> mButtonsMap = new HashMap<String, SwitchButton>();
    private Context mContext;
    private final String[] mSwitchButtons;

    private SwitchButtonFactory(Context context) {
        this.mContext = context; // FIXME single object handle Context
        mSwitchButtons = mContext.getResources().getStringArray(
                R.array.switchbuttons);
    }

    private String list2String(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String str : list) {
            sb.append(str);
            sb.append("@");
        }
        return sb.toString();
    }

    boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        try {
            context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
    public List<String> loadAllButtonKeys() {
        // first load from old buttons if there are
        // then load from string set in preference,
        // then load defaults from arraylist to add those that are not in the
        // sets
        // Log.v("ZYY_", "mButtons.size():" + mButtons.size() + ", sDirty:"
        // + sDirty);
        // printLog("loadAllButtonKeys_000", mButtons);
        if (mButtons.size() == 0 || sDirty) {
            final String buttonString = mContext.getSharedPreferences(
                    Constants.PREF_FILE, Context.MODE_PRIVATE).getString(
                    KEY_SWITCH_BUTTONS, "");
            Log.d(TAG, "get stringButtons" + buttonString);
            if (TextUtils.isEmpty(buttonString)) {
                mButtons = Arrays.asList(mSwitchButtons);
            } else {
                mButtons = string2List(buttonString);
            }
            sDirty = false;
        }
        // printLog("loadAllButtonKeys_111", mButtons);
        ArrayList<String> buttons = new ArrayList<String>(mButtons);
        if (!TelephonyAdapter.isMtkMultisimSuported(mContext)
                && buttons.contains("GeminiDataButton")) {
            buttons.remove("GeminiDataButton");
        }
        if (!TelephonyAdapter.isQrdMultisimSuported(mContext)
                && buttons.contains("MsimDataButton")) {
            buttons.remove("MsimDataButton");
        }
        if (!checkApkExist(mContext, "com.lewa.spm")) {
            buttons.remove("PowerManagerButton");
        }
        if (buttons.contains("LockButton")) {
            buttons.remove("LockButton");
        }
        if (buttons.contains("RebootButton")) {
            buttons.remove("RebootButton");
        }
        if (buttons.contains("ShutdownButton")) {
            buttons.remove("ShutdownButton");
        }
        // printLog("loadAllButtonKeys_222", buttons);
        return buttons;
    }

    public SwitchButton loadButton(String key) {
        SwitchButton button = mButtonsMap.get(key);
        if (button == null) {
            try {
                button = (SwitchButton) Class.forName(
                        "com.lewa.systemuiext.switchwidget." + key)
                        .newInstance();
                mButtonsMap.put(key, button);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
        return button;
    }

    public List<String> loadSwitchButtons() {
        List<String> buttons = loadAllButtonKeys();
        if (buttons.contains(BUTTONS_LAST_SECOND)) {
            buttons.remove(BUTTONS_LAST_SECOND);
        }
        if (buttons.contains(BUTTONS_LAST_FIRST)) {
            buttons.remove(BUTTONS_LAST_FIRST);
        }
        // printLog("loadSwitchButtons_result", buttons);
        return buttons;
    }

    // private void printLog(String tag, List<String> switch_buttons) {
    // Log.v("ZYY_", "Entry printLog(), lists is:" + switch_buttons);
    // for (String switch_button : switch_buttons) {
    // Log.v("ZYY_" + tag, "switch_button:" + switch_button);
    // }
    // }

    public void saveSwitchButtons(List<String> buttons) {
        // printLog("saveSwitchButtons_000", buttons);
        ArrayList<String> toSaved = new ArrayList<String>(buttons);
        if (!toSaved.contains(BUTTONS_LAST_SECOND)) {
            toSaved.add(BUTTONS_LAST_SECOND);
        }
        if (!toSaved.contains(BUTTONS_LAST_FIRST)) {
            toSaved.add(BUTTONS_LAST_FIRST);
        }
        // printLog("saveSwitchButtons_result", toSaved);
        mContext.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE)
                .edit().putString(KEY_SWITCH_BUTTONS, list2String(toSaved))
                .commit();
        sDirty = true;
        mContext.sendBroadcast(new Intent(ACTION_SWITCH_WIDGET_CHANGED));

    }

    private List<String> string2List(String str) {
        String[] array = str.split("@");
        List<String> list = Arrays.asList(array);
        return list;
    }
}
