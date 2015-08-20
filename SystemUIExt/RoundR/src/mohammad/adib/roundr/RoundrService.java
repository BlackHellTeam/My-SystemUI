package mohammad.adib.roundr;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import lewa.provider.ExtraSettings;
import wei.mark.standout.StandOutWindow;

public class RoundrService extends Service {
    private SettingsObserver mObserver;
    public static int mRoundFlags = 0;
    
    public static final int FLAG_ROUND_ON = 1 << 0;
    public static final int FLAG_STATUSBAR_TRANSLUCENT = 1 << 2;
    public static final int FLAG_STATUSBAR_SHOWING = 1 << 3;
    
    private static final String TAG = "RoundrService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mObserver = new SettingsObserver(new Handler());
        mObserver.observe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mObserver.unObserve();
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }
        //Settings.System.getInt(context.getContentResolver(), ExtraSettings.System.ACTIVITY_ROUNDED_CORNER_ENABLE, 0) == 1 ? true : false)
        void observe() {
            ContentResolver resolver = getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    ExtraSettings.System.ACTIVITY_ROUNDED_CORNER_ENABLE), false, this);
            onChange(false);
        }
        
        void unObserve() {
            ContentResolver resolver = getContentResolver();
            resolver.unregisterContentObserver(this);
        }
        
        @Override
        public void onChange(boolean selfChange) {
            update(RoundrService.this, null);
        }
        
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if(bundle == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        update(this, bundle);
        return super.onStartCommand(intent, flags, startId);
    }
    
    //TODO:make it convenient to change a single flag
    public static void update(final Context context, Bundle bundle) {
        boolean roundOn = Settings.System.getInt(context.getContentResolver(), ExtraSettings.System.ACTIVITY_ROUNDED_CORNER_ENABLE, 1) == 1;//jdsong fix roundr disabled first time
        Log.d(TAG, "roundOn : " + roundOn);
        if(bundle != null) {
            Log.d(TAG,"translucent : " +  bundle.getBoolean("translucent") + "showing" + bundle.getBoolean("showing"));
        }
        if(roundOn) {
            mRoundFlags |= FLAG_ROUND_ON;
        }
        else {
            mRoundFlags &= ~FLAG_ROUND_ON;
        }
        
        if(bundle != null ) {
            if(bundle.getBoolean("translucent")) {
                mRoundFlags |= FLAG_STATUSBAR_TRANSLUCENT;
            }
            else {
                mRoundFlags &= ~FLAG_STATUSBAR_TRANSLUCENT;
            }
            if(bundle.getBoolean("showing")) {
                mRoundFlags |= FLAG_STATUSBAR_SHOWING;
            }
            else {
                mRoundFlags &= ~FLAG_STATUSBAR_SHOWING;
            }
        }
        
        if((mRoundFlags & FLAG_ROUND_ON) == 0) {
            StandOutWindow.closeAll(context, Corner.class);
            return;
        }
        else {
            StandOutWindow.show(context, Corner.class, Corner.CORNER_BOTTOM_LEFT);
            StandOutWindow.show(context, Corner.class, Corner.CORNER_BOTTOM_RIGHT);
            StandOutWindow.show(context, Corner.class, Corner.CORNER_TOP_LEFT_OVERLAP);
            StandOutWindow.show(context, Corner.class, Corner.CORNER_TOP_RIGHT_OVERLAP);
            StandOutWindow.show(context, Corner.class, Corner.CORNER_TOP_LEFT);
            StandOutWindow.show(context, Corner.class, Corner.CORNER_TOP_RIGHT);
        }
       
        boolean overlap_statusbar = (mRoundFlags & FLAG_STATUSBAR_TRANSLUCENT) != 0 || (mRoundFlags & FLAG_STATUSBAR_SHOWING) ==0;
        Log.d(TAG, "overlap_statusbar : " + overlap_statusbar);
        if (overlap_statusbar) {
            StandOutWindow.reallyHide(context, Corner.class, Corner.CORNER_TOP_LEFT);
            StandOutWindow.reallyHide(context, Corner.class, Corner.CORNER_TOP_RIGHT);
        }
        else {
            StandOutWindow.reallyUnhide(context, Corner.class, Corner.CORNER_TOP_LEFT);
            StandOutWindow.reallyUnhide(context, Corner.class, Corner.CORNER_TOP_RIGHT);
        }
    }

}
