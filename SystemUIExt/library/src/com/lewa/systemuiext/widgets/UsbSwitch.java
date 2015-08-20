
package com.lewa.systemuiext.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;

import lewa.internal.v5.widget.SlidingButton;

/**
 * @author juude.song@gmail.com
 * @description this class is intended to switch UsbMassStorage. it is mainly
 *              copied from the file
 *              `packages/SystemUI/src/com/android/systemui/
 *              usb/UsbStorageActivity.java` in AOSP
 */

public class UsbSwitch extends SlidingButton {

    private static final String TAG = "UsbSwitch";
    private Handler mUIHandler;
    private IntentFilter mFilter;
    private StorageManager mStorageManager = null;
    private Handler mAsyncStorageHandler;
    private OnCheckedChangedListener mUsbModeButtonCheckListener;

    public UsbSwitch(Context context) {
        this(context, null, 0);
        System.out.println(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
    }

    public UsbSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UsbSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private class USBButtonListener implements OnCheckedChangedListener {
        @Override
        public void onCheckedChanged(CompoundButton compoundbutton, boolean isChecked) {
            setEnabled(false);
            if (isChecked) {
                checkStorageUsers();
            }
            else {
                switchUsbMassStorage(false);
            }
        }
    }

    private void switchDisplay(final boolean usbStorageInUse) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                setChecked(usbStorageInUse);
                setEnabled(true);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        mFilter = new IntentFilter(UsbManager.ACTION_USB_STATE);
        if (mStorageManager == null) {
            mStorageManager = (StorageManager) getContext().getSystemService(
                    Context.STORAGE_SERVICE);
            if (mStorageManager == null) {
                Log.w(TAG, "Failed to get StorageManager");
            }
        }
        mUsbModeButtonCheckListener = new USBButtonListener();
        setOnCheckedChangedListener(mUsbModeButtonCheckListener);
        HandlerThread thr = new HandlerThread("SystemUI UsbStorageActivity");
        thr.start();
        mAsyncStorageHandler = new Handler(thr.getLooper());
        mStorageManager.registerListener(mStorageListener);
        getContext().registerReceiver(mUsbStateReceiver, mFilter);
        try {
            mAsyncStorageHandler.post(new Runnable() {
                @Override
                public void run() {
                    switchDisplay(mStorageManager.isUsbMassStorageEnabled());
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Failed to read UMS enable state", ex);
        }
        mUIHandler = new Handler();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mUsbStateReceiver);
        if (mStorageManager == null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
    }

    private void handleUsbStateChanged(Intent intent) {
        boolean connected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
        if (!connected) {
            finish();
        }
        UsbManager usbManager = (UsbManager)getContext().getSystemService(Context.USB_SERVICE);
        if(usbManager.isFunctionEnabled(UsbManager.USB_FUNCTION_MASS_STORAGE)) {
            setVisibility(View.VISIBLE);
        }
        else {
            setVisibility(View.GONE);
        }
    }

    private IMountService getMountService() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IMountService.Stub.asInterface(service);
        }
        return null;
    }

    private void switchUsbMassStorage(final boolean on) {
        // things to do elsewhere
        mAsyncStorageHandler.post(new Runnable() {
            @Override
            public void run() {
                if (on) {
                    Log.d(TAG, "enableUsbMassStorage");
                    mStorageManager.enableUsbMassStorage();
                } else {
                    Log.d(TAG, "disableUsbMassStorage");
                    mStorageManager.disableUsbMassStorage();
                }
            }
        });
    }

    private void checkStorageUsers() {
        mAsyncStorageHandler.post(new Runnable() {
            @Override
            public void run() {
                checkStorageUsersAsync();
            }
        });
    }

    private void checkStorageUsersAsync() {
        IMountService ims = getMountService();
        if (ims == null) {
            // Display error dialog
            Log.e(TAG, "error :: switchUsbMassStorage true");
        }
        UserManager um = (UserManager) getContext().getSystemService(Context.USER_SERVICE);
        /*
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            return;
        }
        */
        switchUsbMassStorage(true);
    }

    private void finish() {
        setChecked(false);
        setEnabled(true);
    }

    private StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.d(TAG, " mesiateState oldState : " + oldState + "... + newState : " + newState);
            final boolean on = newState.equals(Environment.MEDIA_SHARED);
            switchDisplay(on);
        }
    };

    private BroadcastReceiver mUsbStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_STATE)) {
                Log.d(TAG, "usb state changed");
                handleUsbStateChanged(intent);
            }
            if (intent.getAction().equals("com.mediatek.ppl.NOTIFY_LOCK")) {
                finish();
            }
        }
    };

}
