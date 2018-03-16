package me.msile.tran.kotlintrandemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 跑马灯
 */

public class MarqueeView extends View {

    private TextPaint tvPaint;
    private int textColor = Color.BLACK;
    private int textSize = 14;
    private String marqueeText = "MarqueeView";
    private boolean isAutoRun = true;
    private long marqueeTime;
    private boolean stopMarquee;

    private int marqueeX;
    private int marqueeWidth;

    private ValueAnimator marqueeAnimator;

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
        tvPaint.setTextSize(sp2px(textSize));
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        tvPaint.setColor(textColor);
        invalidate();
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        tvPaint.setTextSize(sp2px(textSize));
        requestLayout();
        invalidate();
    }

    public void setAutoRun(boolean autoRun) {
        isAutoRun = autoRun;
    }

    public void stopMarquee() {
        stopMarquee = true;
        if (marqueeAnimator != null) {
            marqueeAnimator.end();
        }
        marqueeX = 0;
    }

    public void setMarqueeText(String marqueeText) {
        this.marqueeText = marqueeText;
        if (isAutoRun) {
            startMarquee();
        }
    }

    public void setMarqueeTime(long marqueeTime) {
        this.marqueeTime = marqueeTime;
        startMarquee();
    }

    public void startMarquee() {
        stopMarquee = false;
        requestLayout();
        invalidate();
    }

    private void initMarqueeAnimator() {
        if (marqueeAnimator != null) {
            marqueeAnimator.end();
            marqueeAnimator = null;
        }
        int marqueeNewWidth = (int) tvPaint.measureText(marqueeText);
        marqueeAnimator = ValueAnimator.ofInt(marqueeWidth, -marqueeNewWidth);
        if (marqueeTime <= 0) {
            marqueeTime = 3000;
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
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (TextUtils.isEmpty(marqueeText)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int originWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int originWidthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int originHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        int originHeightMode = View.MeasureSpec.getSize(heightMeasureSpec);
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
        int newWidthSpec = View.MeasureSpec.makeMeasureSpec(marqueeWidth, View.MeasureSpec.EXACTLY);
        int newHeightSpec = View.MeasureSpec.makeMeasureSpec(marqueeHeight, View.MeasureSpec.EXACTLY);
        initMarqueeAnimator();
        super.onMeasure(newWidthSpec, newHeightSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(marqueeText) || stopMarquee) {
            return;
        }
        if (marqueeAnimator != null && !marqueeAnimator.isRunning()) {
            marqueeAnimator.start();
        }
        int baseY = (int) (canvas.getHeight() / 2 - (tvPaint.descent() + tvPaint.ascent()) / 2);
        canvas.drawText(marqueeText, marqueeX, baseY, tvPaint);
    }

    private float sp2px(int spValue) {
        float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale + 0.5f;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopMarquee();
    }
}
