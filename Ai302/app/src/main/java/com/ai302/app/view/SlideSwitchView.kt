package com.ai302.app.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import com.ai302.app.R
import com.ai302.app.utils.ScreenUtils.sp2px

class SlideSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 绘制工具
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val slidePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    // 状态变量
    private var progress = 0f  // 0=左边（预览），1=右边（代码）
    private var targetProgress = 0f
    private var animator: ValueAnimator? = null

    // 自定义属性
    private var backgroundColor: Int
    private var slideColor: Int
    private var cornerRadius: Float
    private var textSize: Float
    @ColorInt private var textNormalColor: Int
    @ColorInt private var textActiveColor: Int
    // 点击监听
    private var onSwitchClickListener: OnSwitchClickListener? = null

    init {
        // 读取自定义属性
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideSwitchView)
        backgroundColor = typedArray.getColor(
            R.styleable.SlideSwitchView_backgroundColor,
            Color.parseColor("#E0E0E0")
        )
        slideColor = typedArray.getColor(
            R.styleable.SlideSwitchView_slideColor,
            Color.parseColor("#8e47f0")
        )
        cornerRadius = typedArray.getDimension(
            R.styleable.SlideSwitchView_cornerRadius,
            dp2px(24f)
        )
        textSize = typedArray.getDimension(
            R.styleable.SlideSwitchView_textSize,
            sp2px(16f)
        )
        textNormalColor = typedArray.getColor(
            R.styleable.SlideSwitchView_textNormalColor,
            Color.parseColor("#616161")  // 深灰色
        )
        textActiveColor = typedArray.getColor(
            R.styleable.SlideSwitchView_textActiveColor,
            Color.parseColor("#FFFFFFFF")  //白色
        )
        typedArray.recycle()

        // 初始化画笔
        backgroundPaint.color = backgroundColor
        slidePaint.color = slideColor
        textPaint.textSize = textSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            width / 3  // 默认高度为宽度1/3，保持长方形比例
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制背景圆角矩形
        canvas.drawRoundRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            cornerRadius, cornerRadius, backgroundPaint
        )

        // 计算滑动块位置（根据progress动态调整）
        val slideLeft = (width / 2f) * progress
        val slideRight = slideLeft + width / 2f
        canvas.drawRoundRect(
            slideLeft, 0f, slideRight, height.toFloat(),
            cornerRadius, cornerRadius, slidePaint
        )

        // 绘制左右文字（根据progress计算颜色渐变）
        val leftTextColor = blendColors(textActiveColor, textNormalColor, progress)
        val rightTextColor = blendColors(textNormalColor, textActiveColor, progress)

        textPaint.color = leftTextColor
        canvas.drawText(
            "预览",
            width / 4f,  // 左半区域中心x坐标
            height / 2f - (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2,  // 垂直居中
            textPaint
        )

        textPaint.color = rightTextColor
        canvas.drawText(
            "代码",
            width * 3f / 4f,  // 右半区域中心x坐标
            height / 2f - (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2,  // 垂直居中
            textPaint
        )

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val isLeftArea = event.x < width / 2f
            // 根据点击位置确定目标状态
            targetProgress = if (event.x < width / 2f) 0f else 1f
            startSlideAnimation()
            // 触发点击回调
            onSwitchClickListener?.onClick(if (isLeftArea) "preview" else "code")
        }
        return true
    }

    private fun startSlideAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(progress, targetProgress).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                progress = animation.animatedValue as Float
                invalidate()  // 触发重绘
            }
            start()
        }
    }

    // 颜色渐变混合函数（根据progress值过渡颜色）
    private fun blendColors(@ColorInt color1: Int, @ColorInt color2: Int, ratio: Float): Int {
        val r = Color.red(color1) * (1 - ratio) + Color.red(color2) * ratio
        val g = Color.green(color1) * (1 - ratio) + Color.green(color2) * ratio
        val b = Color.blue(color1) * (1 - ratio) + Color.blue(color2) * ratio
        return Color.argb(255, r.toInt(), g.toInt(), b.toInt())
    }

    // dp转px工具函数
    private fun dp2px(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    // sp转px工具函数
    private fun sp2px(sp: Float): Float {
        return sp * context.resources.displayMetrics.scaledDensity
    }

    // 点击监听接口
    interface OnSwitchClickListener {
        fun onClick(side: String)  // side: "preview"或"code"
    }

    // 设置监听方法
    fun setOnSwitchClickListener(listener: OnSwitchClickListener) {
        onSwitchClickListener = listener
    }
}
