package me.msile.tran.kotlintrandemo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * 跑马灯
 */

class MarqueeView : View {

    private var tvPaint: TextPaint? = null
    private var textColor = Color.BLACK
    private var textSize = 28
    private var marqueeText = "MarqueeView"
    private var isAutoRun = true
    private var marqueeTime: Long = 3000
    private var stopMarquee: Boolean = false

    private var marqueeX: Int = 0
    private var marqueeWidth: Int = 0

    private var marqueeAnimator: ValueAnimator? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)
        tvPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        tvPaint!!.color = textColor
        tvPaint!!.textSize = sp2px(textSize)
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        tvPaint!!.color = textColor
        invalidate()
    }

    fun setTextSize(textSize: Int) {
        this.textSize = textSize
        tvPaint!!.textSize = sp2px(textSize)
        requestLayout()
        invalidate()
    }

    fun setAutoRun(autoRun: Boolean) {
        isAutoRun = autoRun
    }

    fun stopMarquee() {
        stopMarquee = true
        if (marqueeAnimator != null) {
            marqueeAnimator!!.end()
        }
        marqueeX = 0
    }

    fun setMarqueeText(marqueeText: String) {
        this.marqueeText = marqueeText
        if (isAutoRun) {
            startMarquee()
        }
    }

    fun setMarqueeTime(marqueeTime: Long) {
        this.marqueeTime = marqueeTime
        startMarquee()
    }

    fun startMarquee() {
        stopMarquee = false
        requestLayout()
        invalidate()
    }

    private fun initMarqueeAnimator() {
        if (marqueeAnimator != null) {
            marqueeAnimator!!.end()
            marqueeAnimator = null
        }
        val marqueeNewWidth: Int = tvPaint!!.measureText(marqueeText).toInt()
        marqueeAnimator = ValueAnimator.ofInt(marqueeWidth, -marqueeNewWidth)
        if (marqueeTime <= 0) {
            marqueeTime = 3000
        }
        var multi: Float = marqueeNewWidth * 1.0f / marqueeWidth * 1.0f
        marqueeAnimator!!.duration = (marqueeTime * (1 + multi)).toLong()
        marqueeAnimator!!.repeatMode = ValueAnimator.RESTART
        marqueeAnimator!!.repeatCount = ValueAnimator.INFINITE
        marqueeAnimator!!.interpolator = LinearInterpolator()
        marqueeAnimator!!.addUpdateListener { animation ->
            val integer = animation.animatedValue as Int
            marqueeX = integer
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (TextUtils.isEmpty(marqueeText)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val originWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val originWidthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val originHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val originHeightMode = View.MeasureSpec.getSize(heightMeasureSpec)
        marqueeWidth = if (originWidth > 0 && originWidthMode == MeasureSpec.EXACTLY) {
            originWidth
        } else {
            tvPaint!!.measureText(marqueeText).toInt()
        }
        val marqueeHeight: Int
        marqueeHeight = if (originHeight > 0 && originHeightMode == MeasureSpec.EXACTLY) {
            originHeight
        } else {
            tvPaint!!.textSize.toInt()
        }
        val newWidthSpec = View.MeasureSpec.makeMeasureSpec(marqueeWidth, View.MeasureSpec.EXACTLY)
        val newHeightSpec = View.MeasureSpec.makeMeasureSpec(marqueeHeight, View.MeasureSpec.EXACTLY)
        initMarqueeAnimator()
        super.onMeasure(newWidthSpec, newHeightSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (TextUtils.isEmpty(marqueeText) || stopMarquee) {
            return
        }
        if (marqueeAnimator != null && !marqueeAnimator!!.isRunning) {
            marqueeAnimator!!.start()
        }
        val baseY = (canvas.height / 2 - (tvPaint!!.descent() + tvPaint!!.ascent()) / 2).toInt()
        canvas.drawText(marqueeText, marqueeX.toFloat(), baseY.toFloat(), tvPaint!!)
    }

    fun sp2px(spValue: Int): Float {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return spValue * fontScale + 0.5f
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopMarquee()
    }

}
