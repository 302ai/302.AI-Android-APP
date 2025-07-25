package com.ai302.app.view

/**
 * author :
 * e-mail :
 * time   : 2025/5/12
 * desc   :
 * version: 1.0
 */
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class VoiceWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 波浪条参数
    private val waveCount = 38                // 波浪条数量
    private val waveSpacing = dp2px(4f)       // 条带间隔（4dp）
    private val waveCornerRadius = dp2px(2f)  // 条带圆角半径（2dp）
    private var waveWidth = 0f                // 单个条带宽度（动态计算）
    private val maxWaveHeight = dp2px(15f)    // 条带最大高度（30dp）
    private val minWaveHeight = dp2px(2f)     // 条带最小高度（2dp）

    // 绘制工具
    private var wavePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        // 渐变色（浅蓝→深蓝）
        shader = LinearGradient(
            0f, 0f, 0f, maxWaveHeight,
            Color.parseColor("#81D4FA"),  // 浅蓝
            Color.parseColor("#81D4FA"),  // 深蓝
            Shader.TileMode.CLAMP
        )
    }

    fun setColor1(){
        wavePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            // 渐变色（浅蓝→深蓝）
            shader = LinearGradient(
                0f, 0f, 0f, maxWaveHeight,
                Color.parseColor("#81D4FA"),  // 浅蓝
                Color.parseColor("#81D4FA"),  // 深蓝
                Shader.TileMode.CLAMP
            )
        }
    }

    // 动画相关
    private var valueAnimator: ValueAnimator? = null
    private val currentHeights = FloatArray(waveCount)  // 当前条带高度
    private val targetHeights = FloatArray(waveCount)   // 目标随机高度
    private var noiseOffset = 0f                        // Perlin噪声偏移量（控制整体趋势）

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        waveWidth = (width - waveSpacing * (waveCount - 1)) / waveCount.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制每个波浪条
        for (i in 0 until waveCount) {
            val left = i * (waveWidth + waveSpacing)
            val right = left + waveWidth
            val bottom = height / 2f + currentHeights[i]
            val top = height / 2f - currentHeights[i]

            canvas.drawRoundRect(
                left, top, right, bottom,
                waveCornerRadius, waveCornerRadius, wavePaint
            )
        }
    }

    // 启动无规律波浪动画
    fun startAnim() {
        stopAnim()
        // 初始化目标高度（初始随机）
        targetHeights.indices.forEach { i ->
            targetHeights[i] = randomHeight()
        }

        valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 150  // 每帧更新间隔（150ms，可调整）
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                // 1. 用Perlin噪声生成整体趋势偏移
                noiseOffset += 0.05f  // 噪声步进（控制变化速度）
                val noise = perlinNoise(noiseOffset) * 0.5f + 0.5f  // 归一化到0~1

                // 2. 更新每个条带的当前高度（向目标高度平滑过渡）
                currentHeights.indices.forEach { i ->
                    // 随机概率更新目标高度（模拟音量突变）
                    if (Random.nextFloat() < 0.1f) {  // 10%概率触发更新
                        targetHeights[i] = randomHeight() * noise  // 结合噪声调整目标
                    }
                    // 线性插值平滑过渡（当前高度→目标高度）
                    currentHeights[i] = lerp(
                        currentHeights[i],
                        targetHeights[i],
                        0.3f  // 插值系数（越大过渡越快）
                    )
                }

                invalidate()
            }

            start()
        }
    }

    // 停止动画并重置
    fun stopAnim() {
        valueAnimator?.apply {
            cancel()
            removeAllUpdateListeners()
            valueAnimator = null
        }
        currentHeights.fill(minWaveHeight)
        invalidate()
    }

    // 生成随机高度（min~max之间）
    private fun randomHeight(): Float {
        return Random.nextFloat() * (maxWaveHeight - minWaveHeight) + minWaveHeight
    }

    // 线性插值函数（a→b，t为插值系数）
    private fun lerp(a: Float, b: Float, t: Float): Float {
        return a + t * (b - a)
    }

    // Perlin噪声简化实现（生成0~1的平滑随机值）
    private fun perlinNoise(x: Float): Float {
        val i0 = x.toInt()
        val i1 = i0 + 1
        val x0 = x - i0
        val x1 = x0 - 1f

        // 噪声梯度（简化版）
        val n0 = noise(i0)
        val n1 = noise(i1)

        // 平滑函数（cos插值）
        val t = (1 - kotlin.math.cos(x0 * Math.PI)).toFloat() * 0.5f
        return lerp(n0, n1, t)
    }

    // 基础噪声函数（生成-1~1的随机值）
    private fun noise(i: Int): Float {
        val seed = i * 0x12345678
        val rand = (seed ushr 13) xor seed
        return (1f - (rand * (rand * rand * 15731 + 789221) + 1376312589 and 0x7fffffff) / 1073741824f)
    }

    // dp转px工具函数
    private fun dp2px(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun setColor(){
        //wavePaint.color
        wavePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            // 渐变色（浅蓝→深蓝）
            shader = LinearGradient(
                0f, 0f, 0f, maxWaveHeight,
                Color.parseColor("#FD5912"),  // 浅红
                Color.parseColor("#FD5912"),  // 深红
                Shader.TileMode.CLAMP
            )
        }
    }



}