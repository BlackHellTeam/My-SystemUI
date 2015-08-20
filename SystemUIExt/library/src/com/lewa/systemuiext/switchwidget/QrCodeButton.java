
package com.lewa.systemuiext.switchwidget;

//mhyuan 963919 15-04-15 begin
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ServiceManager;
import android.widget.Toast;
import android.view.IWindowManager;
//mhyuan 963919 15-04-15 end
import android.content.Intent;
import com.lewa.systemuiext.R;

public class QrCodeButton extends StatelessButton {
    //mhyuan 963919 15-04-15 begin
    KeyguardManager keyguardManagerOs = (KeyguardManager)sContext.getSystemService(sContext.KEYGUARD_SERVICE);
    IBinder wmbinder = ServiceManager.getService( "window" );          
    final IWindowManager wm = IWindowManager.Stub.asInterface( wmbinder );
    //mhyuan 963919 15-04-15 end
    public QrCodeButton() {
        super();
        mIcon = R.drawable.stat_qrcode;
        mLabel = R.string.title_qr_code;
        mButtonName = R.string.title_qr_code;
        mSettingsIcon = R.drawable.button_setting_scan;
    }

    @Override
    protected boolean onLongClick() {
        gotoQrCode();
        return false;
    }

    @Override
    protected void onClick() {
        // PASS
    }
    //mhyuan 963919 15-04-15 begin
    private void gotoQrCode() {
        KeyguardLock keyguardLock = keyguardManagerOs.newKeyguardLock(""); 
        if (keyguardManagerOs.isKeyguardLocked()){
            try {
                 wm.dismissKeyguard();
            } catch (Exception e) {
            // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Message msg = Message.obtain();
            msg.arg1=1000;
            QrCodeHander.sendMessageDelayed(msg, 500);
        }else{
            Message msg1 = Message.obtain();
            msg1.arg1=1001;
            QrCodeHander.sendMessageDelayed(msg1,300);
        } 
    }
    private Handler QrCodeHander = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if (msg.arg1 == 1000) {
               if (keyguardManagerOs.isKeyguardLocked()) {
                   gotoQrCode();
                   return;
               }
            }
            Intent i = new Intent("com.lewa.qrcode.SCAN");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(i);
            } catch (Exception e) {
            }
        }
    };
    //mhyuan 963919 15-04-15 end
    @Override
    protected void toggleState() {
        gotoQrCode();
    }
    
}
