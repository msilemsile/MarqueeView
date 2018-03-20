package me.msile.tran.kotlintrandemo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * 跑马灯
 */

class MarqueeView : View, Choreographer.FrameCallback {
    private var tvPaint: TextPaint? = null
    private var textColor = Color.BLACK
    private var textSize = 12
    private var marqueeText: String? = null
    private var marqueeTime: Long = 0
    private var stopMarquee: Boolean = false

    private var marqueeX: Int = 0
    private var marqueeWidth: Int = 0

    private var marqueeAnimator: ValueAnimator? = null

    private var needInitAnimator: Boolean = false
    private var mLeftFadingShader: Shader? = null
    private var mRightFadingShader: Shader? = null
    private var mFadingPaint: Paint? = null
    private val fadingLength = 38

    private var mChoreographer: Choreographer? = null

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
        tvPaint!!.textSize = dip2px(textSize)
        mFadingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mChoreographer = Choreographer.getInstance()
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        tvPaint!!.color = textColor
    }

    fun setTextSize(textSize: Int, isDP: Boolean) {
        this.textSize = textSize
        tvPaint!!.textSize = if (isDP) dip2px(textSize) else sp2px(textSize)
        requestLayout()
    }

    fun setMarqueeTime(marqueeTime: Long) {
        this.marqueeTime = marqueeTime
        if (TextUtils.isEmpty(marqueeText)) {
            return
        }
        stopMarquee()
        startMarquee()
    }

    fun setMarqueeText(marqueeText: String) {
        this.marqueeText = marqueeText
        if (TextUtils.isEmpty(marqueeText)) {
            return
        }
        stopMarquee()
        startMarquee()
    }

    fun startMarquee() {
        if (TextUtils.isEmpty(marqueeText)) {
            return
        }
        stopMarquee = false
        needInitAnimator = true
        initMarqueeAnimator()
        requestLayout()
        invalidate()
    }

    fun stopMarquee() {
        stopMarquee = true
        if (marqueeAnimator != null) {
            marqueeAnimator!!.end()
            marqueeAnimator = null
        }
        marqueeX = 0
        mChoreographer!!.removeFrameCallback(this)
        Log.d(TAG, "--stop marquee--")
    }

    private fun initMarqueeAnimator() {
        if (marqueeWidth <= 0) {
            Log.d(TAG, "--init marquee--width is 0 delay init!")
            needInitAnimator = true
            return
        }
        Log.d(TAG, "--init marquee--success")
        needInitAnimator = false
        val marqueeNewWidth = tvPaint!!.measureText(marqueeText).toInt()
        marqueeAnimator = ValueAnimator.ofInt(marqueeWidth, -marqueeNewWidth)
        if (marqueeTime <= 0) {
            marqueeTime = 5000
        }
        val multi = marqueeNewWidth * 1.0f / marqueeWidth * 1.0f
        marqueeAnimator!!.duration = (marqueeTime * (1 + multi)).toLong()
        marqueeAnimator!!.repeatMode = ValueAnimator.RESTART
        marqueeAnimator!!.repeatCount = ValueAnimator.INFINITE
        marqueeAnimator!!.interpolator = LinearInterpolator()
        marqueeAnimator!!.start()
        mChoreographer!!.postFrameCallback(this)
        Log.d(TAG, "--start marquee--")
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (TextUtils.isEmpty(marqueeText)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val originWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val originWidthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val originHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val originHeightMode = View.MeasureSpec.getSize(heightMeasureSpec)
        if (originWidth > 0 && originWidthMode == View.MeasureSpec.EXACTLY) {
            marqueeWidth = originWidth
        } else {
            marqueeWidth = tvPaint!!.measureText(marqueeText).toInt()
        }
        val marqueeHeight: Int
        if (originHeight > 0 && originHeightMode == View.MeasureSpec.EXACTLY) {
            marqueeHeight = originHeight
        } else {
            marqueeHeight = tvPaint!!.textSize.toInt()
        }
        val newWidthSpec = View.MeasureSpec.makeMeasureSpec(marqueeWidth, View.MeasureSpec.EXACTLY)
        val newHeightSpec = View.MeasureSpec.makeMeasureSpec(marqueeHeight, View.MeasureSpec.EXACTLY)
        super.onMeasure(newWidthSpec, newHeightSpec)
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (TextUtils.isEmpty(marqueeText) || stopMarquee) {
            return
        }
        if (needInitAnimator) {
            initMarqueeAnimator()
        }
        val baseY = (canvas.height / 2 - (tvPaint!!.descent() + tvPaint!!.ascent()) / 2).toInt()
        canvas.drawText(marqueeText!!, marqueeX.toFloat(), baseY.toFloat(), tvPaint!!)
        //draw fading edge
        initFadingShader()
        mFadingPaint!!.shader = mLeftFadingShader
        canvas.drawPaint(mFadingPaint!!)
        mFadingPaint!!.shader = mRightFadingShader
        canvas.drawPaint(mFadingPaint!!)
    }

    private fun initFadingShader() {
        val width = width
        val height = height
        if (width == 0 || height == 0) {
            return
        }
        if (mLeftFadingShader != null) {
            return
        }
        mLeftFadingShader = LinearGradient(0f, (height / 2).toFloat(), fadingLength.toFloat(), (height / 2).toFloat(), Color.WHITE, 0x00000000, Shader.TileMode.CLAMP)
        mRightFadingShader = LinearGradient(width.toFloat(), (height / 2).toFloat(), (width - fadingLength).toFloat(), (height / 2).toFloat(), Color.WHITE, 0x00000000, Shader.TileMode.CLAMP)
    }

    private fun sp2px(spValue: Int): Float {
        val fontScale = resources.displayMetrics.scaledDensity
        return spValue * fontScale + 0.5f
    }

    fun dip2px(dpValue: Int): Float {
        val scale = resources.displayMetrics.density
        return dpValue * scale + 0.5f
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == View.GONE) {
            stopMarquee()
        } else if (visibility == View.VISIBLE) {
            startMarquee()
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        mChoreographer!!.removeFrameCallback(this)
        if (marqueeAnimator == null) {
            return
        }
        val integer = marqueeAnimator!!.animatedValue as Int
        if (integer != null) {
            marqueeX = integer
            invalidate()
            mChoreographer!!.postFrameCallback(this)
        }
    }

    companion object {
        private const val TAG = "MarqueeView"
    }

}
