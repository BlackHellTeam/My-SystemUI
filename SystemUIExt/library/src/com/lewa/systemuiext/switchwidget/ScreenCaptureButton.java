
package com.lewa.systemuiext.switchwidget;

import com.android.internal.statusbar.IStatusBarService;
import com.lewa.systemuiext.R;
import com.lewa.systemuiext.adapter.StatusBarServiceAdapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
//import lewa.screenshot.ScreenshotManager;

public class ScreenCaptureButton extends StatelessButton {
    private IStatusBarService mBarService;
    private final static boolean DEBUG_GESTURE = true;

    public ScreenCaptureButton() {
        super();
        mLabel = R.string.title_toggle_screencapture;
        mIcon = R.drawable.stat_screenshots_on;
        mHandler = new Handler();
        mBarService = IStatusBarService.Stub.asInterface(
                ServiceManager.getService(Context.STATUS_BAR_SERVICE));
        mButtonName = R.string.title_toggle_screencapture;
        mSettingsIcon = R.drawable.btn_setting_screenshot;
    }

    final Object mScreenshotLock = new Object();
    ServiceConnection mScreenshotConnection = null;
    private Handler mHandler;

    private void takeshot() {
        synchronized (mScreenshotLock) {
            if (mScreenshotConnection != null) {
                return;
            }
            ComponentName cn = new ComponentName("com.android.systemui",
                    "com.android.systemui.screenshot.TakeScreenshotService");
            Intent intent = new Intent();
            intent.setComponent(cn);
            ServiceConnection conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name,
                        IBinder service) {
                    synchronized (mScreenshotLock) {
                        if (mScreenshotConnection != this) {
                            return;
                        }
                        Messenger messenger = new Messenger(service);

                        Message msg = Message.obtain(null, 1);
                        final ServiceConnection myConn = this;
                        Handler h = new Handler(mHandler.getLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                synchronized (mScreenshotLock) {
                                    if (mScreenshotConnection == myConn) {
                                        sContext.unbindService(mScreenshotConnection);
                                        mScreenshotConnection = null;
                                        mHandler.removeCallbacks(mScreenshotTimeout);
                                    }
                                }
                            }
                        };
                        msg.replyTo = new Messenger(h);
                        msg.arg1 = msg.arg2 = 0;
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            if (sContext.bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
                mScreenshotConnection = conn;
                mHandler.postDelayed(mScreenshotTimeout, 10000);
            }
        }
    }
    private void takeScreenshot(String packageName, String cls, final boolean isEditScreenshot) {
        synchronized (mScreenshotLock) {

            if (mScreenshotConnection != null) {
                if (DEBUG_GESTURE) {
                    Log.d(TAG, "mScreenshotConnection is not null");
                }
                return;
            }
            ComponentName cn = new ComponentName(packageName,
                    cls);
            Intent intent = new Intent();
            intent.setComponent(cn);
            ServiceConnection conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (mScreenshotLock) {
                        if (mScreenshotConnection != this) {
                            if (DEBUG_GESTURE) {
                                Log.d(TAG, "mScreenshotConnection is not null");
                            }
                            return;
                        }
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, 1);
                        final ServiceConnection myConn = this;
                        Handler h = new Handler(mHandler.getLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                synchronized (mScreenshotLock) {
                                    if (mScreenshotConnection == myConn) {
                                        if (DEBUG_GESTURE) {
                                            Log.d(TAG, "unbind remote services");
                                        }
                                        sContext.unbindService(mScreenshotConnection);
                                        mScreenshotConnection = null;
                                        if (!isEditScreenshot) {
                                            mHandler.removeCallbacks(mScreenshotTimeout);
                                        }
                                    }
                                }
                            }
                        };
                        msg.replyTo = new Messenger(h);
                        msg.arg1 = msg.arg2 = 0;
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                            Log.d(TAG, "failed to send message to remote services ");
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            if (sContext.bindService(
                    intent, conn, Context.BIND_AUTO_CREATE)) {
                Log.d(TAG, "bind service successfull");
                mScreenshotConnection = conn;
                if (!isEditScreenshot) {
                    mHandler.postDelayed(mScreenshotTimeout, 10000);
                }
            }
        }
    }

    final Runnable mScreenshotTimeout = new Runnable() {
        @Override
        public void run() {
            synchronized (mScreenshotLock) {
                if (mScreenshotConnection != null) {
                    sContext.unbindService(mScreenshotConnection);
                    mScreenshotConnection = null;
                }
            }
        }
    };

    Runnable takeshot = new Runnable() {
        @Override
        public void run() {
            takeshot();
            /*ScreenshotManager.takeScreenshot(sContext,false);
            if (ScreenshotManager.isScreenshotEnable(sContext, false)) {
                takeScreenshot(ScreenshotManager.PACKAGE_NAME, ScreenshotManager.CLASS_NAME,
                            true);
            }*/
        }
    };

    @Override
    protected boolean onLongClick() {
        try {
            StatusBarServiceAdapter.collapsePanels(mBarService);
            mHandler.postDelayed(takeshot, 2000);
        } catch (Exception ex) {

        }

        return false;
    }

    @Override
    protected void onClick() {
        try {
            StatusBarServiceAdapter.collapsePanels(mBarService);
            mHandler.postDelayed(takeshot, 1000);
        } catch (Exception ex) {

        }

    }

}
