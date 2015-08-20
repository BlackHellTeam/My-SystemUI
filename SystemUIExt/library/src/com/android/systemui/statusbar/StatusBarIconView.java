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

package com.android.systemui.statusbar;

import android.app.Notification;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.ViewDebug;

import com.android.internal.statusbar.StatusBarIcon;

public class StatusBarIconView extends AnimatedImageView {
    private static final String TAG = "StatusBarIconView";

    private StatusBarIcon mIcon;
    @ViewDebug.ExportedProperty private String mSlot;
    private Drawable mNumberBackground;
    private Paint mNumberPain;
    private int mNumberX;
    private int mNumberY;
    private String mNumberText;
    private Notification mNotification;

    public StatusBarIconView(Context context, String slot, Notification notification) {
        super(context);

    }

    /// @}
}
