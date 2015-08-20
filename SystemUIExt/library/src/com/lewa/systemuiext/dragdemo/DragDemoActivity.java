
package com.lewa.systemuiext.dragdemo;

import com.lewa.systemuiext.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class DragDemoActivity extends Activity {

    private static final String TAG = "DragDemoActivity";

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.drag_demo_activity);
        final ViewGroup container = (ViewGroup) findViewById(R.id.container);
        final View switchContainer = findViewById(R.id.switchContainer);
        View dragMe = findViewById(R.id.dragMe);
        dragMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                LayoutParams lp = switchContainer.getLayoutParams();
                if (lp.height == LayoutParams.WRAP_CONTENT) {
                    lp.height = 200;
                }
                else {
                    lp.height = LayoutParams.WRAP_CONTENT;
                }
                container.updateViewLayout(switchContainer, lp);
            }
        });
        Intent i = new Intent();
        i.setClassName("com.lewa.systemuiext", "com.lewa.systemuiext.dragdemo.DragDemoService");
        startService(i);
        Log.d(TAG, "staring service : " + i);
    }

}
