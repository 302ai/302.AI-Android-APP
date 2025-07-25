package com.ai302.app.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation

/**
 * author :
 * e-mail :
 * time   : 2025/5/8
 * desc   :
 * version: 1.0
 */
// 使用 @JvmOverloads 注解支持 Java 调用时的默认参数
class CircularZoomLoadingAnim @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 绘制圆形的画笔（抗锯齿、填充、白色）
    private val mPaint: Paint = Paint().apply {
        isAntiAlias = true   // 抗锯齿（平滑边缘）
        style = Paint.Style.FILL  // 填充样式（实心）
        color = Color.WHITE       // 颜色（可扩展为自定义）
    }

    private var mWidth: Float = 0f       // 视图宽度
    private var mHeight: Float = 0f      // 视图高度
    private val mMaxRadius: Float = 6f   // 圆形最大半径
    private val circularCount: Int = 4   // 圆形数量（4 个）
    private var mAnimatedValue: Float = 0f  // 动画当前值（0~0.5 往返）
    private var mJumpValue: Int = 0       // 动画重复次数（用于切换跳跃圆形）
    private var valueAnimator: ValueAnimator? = null  // 值动画对象

    init {
        initPaint()// 初始化画笔（预留扩展）
    }

    // 测量视图尺寸（记录实际宽高）
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth.toFloat()// 转换为 Float 方便计算
        mHeight = measuredHeight.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val circularX = mWidth / circularCount // 每个圆形的 X 轴间隔（总宽度均分 4 份

        // 循环绘制 4 个圆形
        for (i in 0 until circularCount) {
            val isCurrentJump = i == mJumpValue % circularCount  // 当前是否是跳跃的圆形
            val radius = mMaxRadius  // 半径（可优化为动态缩放）
            val yOffset = if (isCurrentJump) mHeight / 2 * mAnimatedValue else 0f  // 垂直偏移量

            // 绘制圆形（X 轴居中，Y 轴根据偏移量上下移动）
            canvas.drawCircle(
                i * circularX + circularX / 2f,  // X 坐标（均分后居中）
                mHeight / 2 - yOffset,             // Y 坐标（视图中心 - 偏移量）
                radius,
                mPaint
            )
        }
    }

    // 初始化画笔（预留扩展）
    private fun initPaint() {
        mPaint.color = Color.WHITE  // 可通过 attrs 支持自定义颜色
    }

    fun startAnim() {
        stopAnim()  // 停止已有动画（避免冲突）
        startViewAnim(0f, 1f, 500)  // 启动 0→1 的动画（时长 500ms）
    }

    // 停止动画（释放资源）
    fun stopAnim() {
        valueAnimator?.apply {
            clearAnimation()       // 清除动画效果
            mAnimatedValue = 0f    // 重置动画值
            mJumpValue = 0         // 重置跳跃计数
            repeatCount = 0        // 取消重复
            cancel()               // 取消动画
            end()                  // 结束动画
        }
    }

    // 启动值动画（核心逻辑）
    private fun startViewAnim(startF: Float, endF: Float, time: Long): ValueAnimator {
        valueAnimator = ValueAnimator.ofFloat(startF, endF).apply {
            duration = time  // 动画时长
            interpolator = LinearInterpolator()  // 线性插值（匀速变化）
            repeatCount = ValueAnimator.INFINITE  // 无限重复
            repeatMode = ValueAnimator.RESTART    // 重复模式（重新开始）

            // 动画值更新监听器（控制偏移量）
            addUpdateListener { animation ->
                mAnimatedValue = animation.animatedValue as Float  // 获取当前动画值（0~1）

                // 超过 0.5 时反转（实现往返效果）
                if (mAnimatedValue > 0.5f) {
                    mAnimatedValue = 1 - mAnimatedValue
                }

                invalidate()  // 触发重绘（更新视图）
            }

            // 动画重复监听器（切换跳跃的圆形）
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    super.onAnimationRepeat(animation)
                    mJumpValue++  // 每次重复时计数 +1（通过取模切换下一个圆形）
                }
            })

            if (!isRunning) {
                start()  // 启动动画（仅当未运行时）
            }
        }

        return valueAnimator!!
    }
}