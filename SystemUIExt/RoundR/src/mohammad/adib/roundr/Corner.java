
package mohammad.adib.roundr;

/**
 * Copyright 2013 Mohammad Adib
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class Corner extends StandOutWindow {

    /**
     * The individual floating window that sits at one of the corners of the
     * screen. Window ID corresponds to which corner this goes to.
     * 
     * @author Mohammad Adib <m.a.adib96@gmail.com> Contributors: Mark Wei, Jan
     *         Metten
     */

    public static final String ACTION_SETTINGS = "SETTINGS";
    public static final String BCAST_CONFIGCHANGED = "android.intent.action.CONFIGURATION_CHANGED";
    public static final int UPDATE_CODE = 2;
    public static final int NOTIFICATION_CODE = 3;
    public static final int wildcard = 0;
    private SharedPreferences prefs;
    public static boolean running = false;

    public static final int CORNER_TOP_LEFT = 0;
    public static final int CORNER_TOP_RIGHT = 1;
    public static final int CORNER_BOTTOM_LEFT = 2;
    public static final int CORNER_BOTTOM_RIGHT = 3;
    public static final int CORNER_TOP_LEFT_OVERLAP = 4;
    public static final int CORNER_TOP_RIGHT_OVERLAP = 5;

    @Override
    public String getAppName() {
        return "RoundR";
    }

    @Override
    public int getAppIcon() {
        return R.drawable.notif_icon;
    }

    @Override
    public void createAndAttachView(int corner, FrameLayout frame) {
        // Set the image based on window corner
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        ImageView v = (ImageView) inflater.inflate(R.layout.corner, frame, true).findViewById(
                R.id.iv);
        // Top left by default
        switch (corner) {
            case Corner.CORNER_TOP_LEFT_OVERLAP:
            case Corner.CORNER_TOP_LEFT:
                v.setImageDrawable(getResources().getDrawable(R.drawable.topleft));
                break;
            case Corner.CORNER_TOP_RIGHT:
            case Corner.CORNER_TOP_RIGHT_OVERLAP:
                v.setImageDrawable(getResources().getDrawable(R.drawable.topright));
                break;
            case Corner.CORNER_BOTTOM_LEFT:
                v.setImageDrawable(getResources().getDrawable(R.drawable.bottomleft));
                break;
            case Corner.CORNER_BOTTOM_RIGHT:
                v.setImageDrawable(getResources().getDrawable(R.drawable.bottomright));
                break;
        }
    }

    private int pxFromDp(double dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    /**
     * Corners: 0 = top left; 1 = top right; 2 = bottom left; 3 = bottom right;
     */
    @Override
    public StandOutLayoutParams getParams(int corner, Window window) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Check if this corner is enabled
        int roundEnabled = 1;
        if (roundEnabled == 1) {
            int radius = pxFromDp(prefs.getInt("radius", 10));
            // Thanks to Jan Metten for rewriting this based on gravity
            switch (corner) {
                case CORNER_TOP_LEFT_OVERLAP:
                    // LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    // LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    StandOutLayoutParams standLp = new
                            StandOutLayoutParams(corner, radius, radius, Gravity.TOP | Gravity.LEFT);
                    standLp.flags = StandOutLayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            StandOutLayoutParams.FLAG_SHOW_WHEN_LOCKED;
                    standLp.setTitle("CORNER_TOP_LEFT_OVERLAP");
                    return standLp;
                case CORNER_TOP_LEFT:
                    StandOutLayoutParams standLp2 = new StandOutLayoutParams(corner, radius,
                            radius, Gravity.TOP | Gravity.LEFT);
                    standLp2.flags = StandOutLayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | StandOutLayoutParams.FLAG_NOT_TOUCH_MODAL
                            | StandOutLayoutParams.FLAG_NOT_TOUCHABLE
                            | StandOutLayoutParams.FLAG_NOT_FOCUSABLE;;
                    standLp2.type = StandOutLayoutParams.TYPE_SYSTEM_ALERT;
                    standLp2.setTitle("CORNER_TOP_LEFT");
                    return standLp2;
                case CORNER_TOP_RIGHT:
                    StandOutLayoutParams standLp3 = new StandOutLayoutParams(corner, radius,
                            radius, Gravity.TOP | Gravity.RIGHT);
                    standLp3.flags = StandOutLayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | StandOutLayoutParams.FLAG_NOT_TOUCH_MODAL
                            | StandOutLayoutParams.FLAG_NOT_TOUCHABLE
                            | StandOutLayoutParams.FLAG_NOT_FOCUSABLE;
                    standLp3.type = StandOutLayoutParams.TYPE_SYSTEM_ALERT;
                    standLp3.setTitle("CORNER_TOP_RIGHT");
                    return standLp3;
                case CORNER_TOP_RIGHT_OVERLAP:
                    StandOutLayoutParams standLp4 = new StandOutLayoutParams(corner, radius,
                            radius, Gravity.TOP | Gravity.RIGHT);
                    standLp4.flags = StandOutLayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            StandOutLayoutParams.FLAG_SHOW_WHEN_LOCKED;
                    standLp4.setTitle("CORNER_TOP_RIGHT_OVERLAP");
                    return standLp4;
                case CORNER_BOTTOM_LEFT:
                    StandOutLayoutParams standLp5 = new StandOutLayoutParams(corner, radius,
                            radius, Gravity.BOTTOM | Gravity.LEFT);
                    standLp5.flags = StandOutLayoutParams.FLAG_SHOW_WHEN_LOCKED;
                    standLp5.setTitle("CORNER_BOTTOM_LEFT");
                    return standLp5;
                case CORNER_BOTTOM_RIGHT:
                    StandOutLayoutParams standLp6 = new StandOutLayoutParams(corner, radius,
                            radius, Gravity.BOTTOM | Gravity.RIGHT);
                    standLp6.flags = StandOutLayoutParams.FLAG_SHOW_WHEN_LOCKED;
                    standLp6.setTitle("CORNER_BOTTOM_RIGHT");
                    return standLp6;
            }
        }
        // Outside of screen
        return new StandOutLayoutParams(corner, 1, 1, -1, -1, 1, 1);
    }

    @Override
    public int getFlags(int corner) {
        return super.getFlags(corner) | StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE
                | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE
                | StandOutFlags.FLAG_WINDOW_HIDE_ENABLE;
    }

    @Override
    public String getPersistentNotificationMessage(int corner) {
        return "Tap to configure";
    }

    @Override
    public Intent getPersistentNotificationIntent(int corner) {
        return new Intent(this, Corner.class).putExtra("id", corner).setAction(ACTION_SETTINGS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            int corner = intent.getIntExtra("id", DEFAULT_ID);
            if (corner == ONGOING_NOTIFICATION_ID) {
                throw new RuntimeException(
                        "ID cannot equals StandOutWindow.ONGOING_NOTIFICATION_ID");
            }

            if (ACTION_SHOW.equals(action) || ACTION_RESTORE.equals(action)) {
                show(corner);
            } else if (ACTION_HIDE.equals(action)) {
                hide(corner);
            } else if (ACTION_CLOSE.equals(action)) {
                close(corner);
            }
            else if (ACTION_REALLY_HIDE.equals(action)) {
                reallyHide(corner);
            }
            else if (ACTION_REALLY_UNHIDE.equals(action)) {
                reallyUnhide(corner);
            }
            else if (ACTION_CLOSE_ALL.equals(action)) {
                closeAll();
            } else if (ACTION_SEND_DATA.equals(action)) {
                if (isExistingId(corner) || corner == DISREGARD_ID) {
                    Bundle data = intent.getBundleExtra("wei.mark.standout.data");
                    int requestCode = intent.getIntExtra("requestCode", 0);
                    @SuppressWarnings("unchecked")
                    Class<? extends StandOutWindow> fromCls = (Class<? extends StandOutWindow>) intent
                            .getSerializableExtra("wei.mark.standout.fromCls");
                    int fromId = intent.getIntExtra("fromId", DEFAULT_ID);
                    onReceiveData(corner, requestCode, data, fromCls, fromId);
                }
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public boolean onClose(final int corner, final Window window) {
        running = false;
        return false;
    }

    @Override
    public boolean onShow(final int corner, final Window window) {
        running = true;
        return false;
    }

    @Override
    public void onReceiveData(int corner, int requestCode, Bundle data,
            Class<? extends StandOutWindow> fromCls, int fromId) {
        Window window = getWindow(corner);
        if (requestCode == UPDATE_CODE) {
            // Update the corners when device is rotated
            updateViewLayout(Corner.CORNER_TOP_LEFT, 
                    getParams(Corner.CORNER_TOP_LEFT, window));
            updateViewLayout(Corner.CORNER_TOP_RIGHT, 
                    getParams(Corner.CORNER_TOP_RIGHT, window));
            updateViewLayout(Corner.CORNER_BOTTOM_LEFT,
                    getParams(Corner.CORNER_BOTTOM_LEFT, window));
            updateViewLayout(Corner.CORNER_BOTTOM_RIGHT,
                    getParams(Corner.CORNER_BOTTOM_RIGHT, window));
            updateViewLayout(Corner.CORNER_TOP_LEFT_OVERLAP,
                    getParams(Corner.CORNER_TOP_LEFT_OVERLAP, window));
            updateViewLayout(Corner.CORNER_TOP_RIGHT_OVERLAP,
                    getParams(Corner.CORNER_TOP_RIGHT_OVERLAP, window));

        }
    }
}
