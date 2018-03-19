package com.xk.span.zutuan.common.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 跑马灯
 */

public class MarqueeView extends View {
    private static final String TAG = "MarqueeView";
    private TextPaint tvPaint;
    private int textColor = Color.BLACK;
    private int textSize = 12;
    private String marqueeText;
    private long marqueeTime;
    private boolean stopMarquee;

    private int marqueeX;
    private int marqueeWidth;

    private ValueAnimator marqueeAnimator;

    private boolean needInitAnimator;

    public MarqueeView(Context context) {
        super(context);
        init();
    }

    public MarqueeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarqueeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        tvPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tvPaint.setColor(textColor);
        tvPaint.setTextSize(dip2px(textSize));
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        tvPaint.setColor(textColor);
    }

    public void setTextSize(int textSize, boolean isDP) {
        this.textSize = textSize;
        tvPaint.setTextSize(isDP ? dip2px(textSize) : sp2px(textSize));
        requestLayout();
    }

    public void setMarqueeTime(long marqueeTime) {
        this.marqueeTime = marqueeTime;
        stopMarquee();
        startMarquee();
    }

    public void setMarqueeText(String marqueeText) {
        this.marqueeText = marqueeText;
        stopMarquee();
        startMarquee();
    }

    public void startMarquee() {
        stopMarquee = false;
        needInitAnimator = true;
        initMarqueeAnimator();
        requestLayout();
        invalidate();
    }

    public void stopMarquee() {
        stopMarquee = true;
        if (marqueeAnimator != null) {
            marqueeAnimator.end();
            marqueeAnimator = null;
        }
        marqueeX = 0;
        Log.d(TAG, "--stop marquee--");
    }

    private void initMarqueeAnimator() {
        if (marqueeWidth <= 0) {
            Log.d(TAG, "--init marquee--width is 0 delay init!");
            needInitAnimator = true;
            return;
        }
        Log.d(TAG, "--init marquee--success");
        needInitAnimator = false;
        int marqueeNewWidth = (int) tvPaint.measureText(marqueeText);
        marqueeAnimator = ValueAnimator.ofInt(marqueeWidth, -marqueeNewWidth);
        if (marqueeTime <= 0) {
            marqueeTime = 5000;
        }
        float multi = marqueeNewWidth * 1.0f / marqueeWidth * 1.0f;
        marqueeAnimator.setDuration((long) (marqueeTime * (1 + multi)));
        marqueeAnimator.setRepeatMode(ValueAnimator.RESTART);
        marqueeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        marqueeAnimator.setInterpolator(new LinearInterpolator());
        marqueeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer integer = (Integer) animation.getAnimatedValue();
                marqueeX = integer;
                invalidate();
            }
        });
        marqueeAnimator.start();
        Log.d(TAG, "--start marquee--");
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (TextUtils.isEmpty(marqueeText)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int originWidth = MeasureSpec.getSize(widthMeasureSpec);
        int originWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int originHeight = MeasureSpec.getSize(heightMeasureSpec);
        int originHeightMode = MeasureSpec.getSize(heightMeasureSpec);
        if (originWidth > 0 && originWidthMode == MeasureSpec.EXACTLY) {
            marqueeWidth = originWidth;
        } else {
            marqueeWidth = (int) tvPaint.measureText(marqueeText);
        }
        int marqueeHeight;
        if (originHeight > 0 && originHeightMode == MeasureSpec.EXACTLY) {
            marqueeHeight = originHeight;
        } else {
            marqueeHeight = (int) tvPaint.getTextSize();
        }
        int newWidthSpec = MeasureSpec.makeMeasureSpec(marqueeWidth, MeasureSpec.EXACTLY);
        int newHeightSpec = MeasureSpec.makeMeasureSpec(marqueeHeight, MeasureSpec.EXACTLY);
        super.onMeasure(newWidthSpec, newHeightSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(marqueeText) || stopMarquee) {
            return;
        }
        if (needInitAnimator) {
            initMarqueeAnimator();
        }
        int baseY = (int) (canvas.getHeight() / 2 - (tvPaint.descent() + tvPaint.ascent()) / 2);
        canvas.drawText(marqueeText, marqueeX, baseY, tvPaint);
    }

    private float sp2px(int spValue) {
        float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale + 0.5f;
    }

    public int dip2px(int dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == GONE) {
            stopMarquee();
        } else if (visibility == VISIBLE) {
            startMarquee();
        }
    }

}
