package com.ai302.app.view
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
/**
 * author :
 * e-mail :
 * time   : 2025/5/9
 * desc   :
 * version: 1.0
 */
class ThreeCircularLoadingAnim @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 绘制画笔（抗锯齿、填充模式）
    private val mPaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.GRAY // 可通过自定义属性修改颜色
    }

    private var mWidth: Float = 0f        // 视图宽度
    private var mHeight: Float = 0f       // 视图高度
    private val mMaxRadius: Float = 8f    // 圆点最大半径
    private val mMinRadius: Float = 2f    // 圆点最小半径
    private val circularCount: Int = 3    // 圆点数量（修改为3个）
    private var mAnimatedValue: Float = 0f // 动画核心值（0~1循环）
    private var valueAnimator: ValueAnimator? = null // 值动画对象

    init {
        initPaint()
    }

    // 测量视图尺寸
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth.toFloat()
        mHeight = measuredHeight.toFloat()
    }

    // 绘制核心逻辑
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 计算每个圆点的X轴间隔（总宽度均分3份）
        val circularX = mWidth / circularCount

        // 循环绘制3个圆点
        for (i in 0 until circularCount) {
            // 根据圆点索引计算缩放因子
            val scaleFactor = when (i) {
                0, 2 -> mAnimatedValue       // 第1、3个圆点使用当前动画值
                1 -> 1 - mAnimatedValue      // 第2个圆点使用反向动画值
                else -> 0f
            }

            // 动态计算当前半径（最小半径 + 变化量）
            val radius = mMinRadius + (mMaxRadius - mMinRadius) * scaleFactor

            // 绘制圆点（X轴居中，Y轴位于视图中心）
            canvas.drawCircle(
                i * circularX + circularX / 2f,  // X坐标（均分后居中）
                mHeight / 2,                      // Y坐标（视图垂直中心）
                radius,                           // 动态半径
                mPaint
            )
        }
    }

    // 初始化画笔（可扩展自定义属性）
    private fun initPaint() {
        // 可通过attrs获取自定义颜色，示例：
        // context.obtainStyledAttributes(attrs, R.styleable.ThreeCircularLoadingAnim).run {
        //     mPaint.color = getColor(R.styleable.ThreeCircularLoadingAnim_circleColor, Color.WHITE)
        //     recycle()
        // }
    }

    // 启动动画
    fun startAnim() {
        stopAnim()
        // 启动0→1的无限循环动画，时长800ms（可调整）
        valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            interpolator = LinearInterpolator() // 线性插值（匀速变化）
            repeatCount = ValueAnimator.INFINITE // 无限重复
            repeatMode = ValueAnimator.RESTART   // 重复时重新开始

            addUpdateListener { animation ->
                mAnimatedValue = animation.animatedValue as Float // 更新动画值（0→1循环）
                invalidate() // 触发重绘
            }

            if (!isRunning) start()
        }
    }

    // 停止动画并释放资源
    fun stopAnim() {
        valueAnimator?.apply {
            cancel()
            mAnimatedValue = 0f
            removeAllUpdateListeners()
            valueAnimator = null
        }
    }

    // 视图销毁时停止动画（防止内存泄漏）
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnim()
    }
}