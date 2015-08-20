/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.util.Log;
import android.view.VelocityTracker;


public class CloseDragHandle extends LinearLayout {
    PhoneStatusBar mService;
    private int mTouchflag = 0;
    private static final int UP_SWIPE = 3;
    public final boolean DEBUG = true;
    public static final String TAG = "CloseDragHandle";
    private float lastmotionX;
    private float lastmotionY;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private final static int SNAP_VELOCITY = 1800;
 
    public CloseDragHandle(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVelocityTracker = VelocityTracker.obtain();
    }

    /**
     * Ensure that, if there is no target under us to receive the touch,
     * that we process it ourself.  This makes sure that onInterceptTouchEvent()
     * is always called for the entire gesture.
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        final int action = ev.getAction();
        if (DEBUG) {
            Log.d(TAG, "onTouchEvent : " + MotionEvent.actionToString(action));
        }
        boolean handled = false;
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            lastmotionX = x;
            lastmotionY = y;
            break;
        case MotionEvent.ACTION_MOVE:
            final int xDiff = (int) Math.abs(x - lastmotionX);
            if (lastmotionY - y > mTouchSlop * 2 && xDiff < 200) {
                mTouchflag = UP_SWIPE;
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (mTouchflag == UP_SWIPE) {
                if (DEBUG)
                    Log.d(TAG, "UP_SWIPE");
                mService.animateCollapsePanels();
            }
            mTouchflag = 0;
            break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if (DEBUG) {
            Log.d(TAG,
                    "onInterceptTouchEvent : "
                            + MotionEvent.actionToString(action));
        }
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchflag > 0)) {
            return true;
        }
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_MOVE:
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            mTouchflag = 0;
            break;
        default:
            break;
        }
        return mTouchflag > 0 ? true : false;
    }
}

