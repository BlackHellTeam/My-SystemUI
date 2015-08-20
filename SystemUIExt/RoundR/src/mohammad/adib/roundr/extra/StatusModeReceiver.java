
package mohammad.adib.roundr.extra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import mohammad.adib.roundr.RoundrService;

public class StatusModeReceiver extends BroadcastReceiver {

    public static final String TAG = "StatusModeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        RoundrService.update(context, extras);
    }

//    private void update(final Context context, boolean overlap_statusbar) {
//        if (overlap_statusbar) {
//            StandOutWindow.reallyHide(context, Corner.class, Corner.CORNER_TOP_LEFT);
//            StandOutWindow.reallyHide(context, Corner.class, Corner.CORNER_TOP_RIGHT);
//            StandOutWindow.reallyUnhide(context, Corner.class, Corner.CORNER_TOP_LEFT_OVERLAP);
//            StandOutWindow.reallyUnhide(context, Corner.class, Corner.CORNER_TOP_RIGHT_OVERLAP);
//        }
//        else {
//            StandOutWindow.reallyUnhide(context, Corner.class, Corner.CORNER_TOP_LEFT);
//            StandOutWindow.reallyUnhide(context, Corner.class, Corner.CORNER_TOP_RIGHT);
//            StandOutWindow.reallyHide(context, Corner.class, Corner.CORNER_TOP_LEFT_OVERLAP);
//            StandOutWindow.reallyHide(context, Corner.class, Corner.CORNER_TOP_RIGHT_OVERLAP);
//        }
//    }

}
