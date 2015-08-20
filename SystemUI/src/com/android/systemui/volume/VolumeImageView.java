
package com.android.systemui.volume;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.view.View;

public class VolumeImageView extends View {

    public interface CallBack {
        void animationEnd(int endEngle);

        void animationStart(int endEngle);
    }

    public interface OnProgressChanged {
        void onProgressChanged(int progress);
    }

    private int mCicleWidth = 8;
    private Paint paint = new Paint();
    private int startAngle = -90;
    private int endAngle = 0;
    private int space = 11;
    private int color = 0x27e7e7e7;
    private int colorbackground = 0xc7000000;
    private boolean isVisiablePoint = false;
    private boolean needsInvalidate = false;
    private boolean isFirst = true;
    private int tagEndAngle = 0;
    private int doneCircleAngle = 360;
    private VolumeImageView.CallBack callBack = null;
    private static final int AnimationSpeed = 18;

    public VolumeImageView(Context context) {
        super(context);
    }

    public void setCallBack(VolumeImageView.CallBack callBack) {
        this.callBack = callBack;
    }

    public VolumeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public VolumeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCircleColor(int color) {
        this.color = color;
    }

    public void setBackgroundColor(int color) {
        this.colorbackground = color;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    public void setPercentage(float perc) {
        if (perc == 0f) {
            isVisiablePoint = false;
        }
        else {
            isVisiablePoint = true;
        }
        int degrees = (int) (perc * 360);
        if (isFirst) {
            endAngle = degrees;
            isFirst = false;
        }
        tagEndAngle = degrees;
        this.postInvalidate();
        needsInvalidate = true;
    }

    public void removeCircleView() {
        endAngle = tagEndAngle;
        isFirst = true;
    }

    public void doneCircleView() {
        endAngle = doneCircleAngle;
        isFirst = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(colorbackground);
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, paint);
        paint.setColor(color);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(mCicleWidth);
        double r = (getWidth() - 2 * space) / 2;
        float y = (float) (r * Math.sin((2 * Math.PI) / 360 * (90 - endAngle)));
        float x = (float) (r * Math.cos((2 * Math.PI) / 360 * (90 - endAngle)));
        RectF rf = new RectF(0 + space, 0 + space, getWidth() - space, getHeight() - space);
        canvas.drawArc(rf, 0, 360, false, paint);
        paint.setColor(Color.WHITE);
        canvas.drawArc(rf, startAngle, endAngle, false, paint);
        paint.setStyle(Style.FILL);
        if (isVisiablePoint) {
            /*canvas.drawCircle(getWidth() / 2, space, mCicleWidth / 2, paint);
            canvas.drawCircle((float) getWidth() / 2 + x, (float) getHeight() / 2 - y,
                    mCicleWidth / 2, paint);*/
        }
        if (needsInvalidate) {
            if (Math.abs(endAngle - tagEndAngle) < AnimationSpeed) {
                endAngle = tagEndAngle;
                needsInvalidate = false;
                if (this.callBack != null)
                    this.callBack.animationEnd(endAngle);
            }
            if (endAngle != tagEndAngle) {
                if (endAngle > tagEndAngle) {
                    endAngle = endAngle - AnimationSpeed;
                } else {
                    endAngle = endAngle + AnimationSpeed;
                }
                if (this.callBack != null)
                    this.callBack.animationStart(endAngle);
            }
            android.support.v4.view.ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}
