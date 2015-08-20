package com.lewa.systemuiext.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class PrettyTextView extends TextView {

    public PrettyTextView(Context context) {
        this(context, null, 0);
    }

    public PrettyTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrettyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPrettyTypeface(context, this);
    }
    
    public static void setPrettyTypeface(Context context , TextView textView) {
        Typeface neoSans =Typeface.createFromAsset(context.getAssets(),"fonts/NeoSans.otf");
        textView.setTypeface(neoSans);
    }
    
}