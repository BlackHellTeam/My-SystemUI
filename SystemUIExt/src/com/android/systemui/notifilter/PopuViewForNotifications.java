package com.android.systemui.notifilter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.WindowManager;
import android.widget.Toast;
import android.os.ServiceManager;
import android.os.RemoteException;

import com.android.internal.statusbar.IStatusBarService;

import com.android.systemui.R;
import com.lewa.systemuiext.adapter.StatusBarServiceAdapter;
import com.lewa.systemuiext.Constants;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;
import android.text.TextUtils;
import android.service.notification.StatusBarNotification;

public class PopuViewForNotifications {

    private Context mContext;
    private String mPackageName;
    private String mTag;
    private int mId;
    private IStatusBarService mBarService;
    private String mAppName;
    private int mUserId;

    public boolean mNeedClose = false;
    public static final String TAG = "PopuViewForNotifications";
    AlertDialog ad;

    public PopuViewForNotifications(Context context,
            StatusBarNotification notification) {

        mContext = context;
        mPackageName = notification.getPackageName();
        mUserId = notification.getUserId();
        mTag = notification.getTag();
        mId = notification.getId();

        mBarService = IStatusBarService.Stub.asInterface(ServiceManager
                .getService(Context.STATUS_BAR_SERVICE));
        PackageManager pm = context.getPackageManager();

        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(mPackageName, 0);
            if ((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return;
            }
            mAppName = appinfo.loadLabel(pm).toString();
            if (mAppName == null) {
                mAppName = mPackageName;
            }
        } catch (NameNotFoundException e) {
        }
        Builder dialog = new AlertDialog.Builder(mContext).setTitle(mAppName)
                .setItems(
                        new CharSequence[] {
                                mContext.getResources().getString(
                                        R.string.poputip_title3),
                                mContext.getResources().getString(
                                        R.string.poputip_title4),
                                mContext.getResources().getString(
                                        R.string.notifilter_item_activity) },
                        ocldialog);
        ad = dialog.create();
        ad.getWindow()
                .setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
        ad.getWindow().setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(0));
        ad.show();
        context.registerReceiver(mHomeKeyEventReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    /**
     * listening home button
     */
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_REASON = "reason";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "action :" + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                Log.i(TAG, "reason :" + reason);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                    ad.dismiss();
                }
            }
        }
    };

    android.content.DialogInterface.OnClickListener ocldialog = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (which == 1) {
                mNeedClose = true;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts(
                        "package", mPackageName, null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                StatusBarServiceAdapter.collapsePanels(mBarService);
                mContext.startActivity(intent);

            } else if (which == 0) {
                Intent i = new Intent();
                i.setAction(Constants.ACTION_ADD_BLOCK_PACKAGE);
                i.putExtra("package", mPackageName);
                mContext.sendBroadcast(i);
                String str = mContext.getResources().getString(
                        R.string.poputip_toast, mAppName);
                Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
                try {
                    mBarService.onNotificationClear(mPackageName,mTag, mId,mUserId);
                } catch (RemoteException ex) {
                    // system process is dead if we're here.
                }
            } else {
                Intent intent = new Intent(Constants.NOTIFILTER_ACTION);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
            StatusBarServiceAdapter.collapsePanels(mBarService);
        }
    };

}
