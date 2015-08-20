package com.lewa.systemuiext.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lewa.systemuiext.R;

public class ForceTextColorLayout extends LinearLayout{
    private int mColor;
    public ForceTextColorLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ForceTextColorLayout,
                defStyle, 0);
        mColor = a.getColor(R.styleable.ForceTextColorLayout_forceTextColor, 0xFF0000FF);
        a.recycle();
        
        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {

            @Override
            public void onChildViewAdded(View parent, View child) {
                setTextColor(child);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                
            }
            
        });
    }
    
    public ForceTextColorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ForceTextColorLayout(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setTextColor(this);
    }
    
    @Override
    protected void onViewAdded(View child) {
        super.onViewAdded(child);
        setTextColor(child);
    }

    public void setTextColor(View view) {
        if(view instanceof TextView) {
            ((TextView)view).setTextColor(mColor);
        }
        else if(view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)view;
            for(int i = 0 ; i < viewGroup.getChildCount(); i++) {
                setTextColor(viewGroup.getChildAt(i));
            }
        }
    }

}

