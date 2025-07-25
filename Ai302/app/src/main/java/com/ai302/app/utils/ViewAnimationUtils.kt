package com.ai302.app.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View

// 动画工具类（放在任意 Kotlin 文件中）
object ViewAnimationUtils {
    /**
     * 显示 View（带淡入+缩放动画）
     * @param view 需要显示的 View
     * @param duration 动画时长（毫秒）
     */
    fun showWithAnimation(view: View, duration: Long = 300) {
        view.visibility = View.VISIBLE
        view.alpha = 0f       // 初始透明度（完全透明）
        view.scaleX = 0.8f    // 初始缩放比例（横向）
        view.scaleY = 0.8f    // 初始缩放比例（纵向）

        // 组合透明度和缩放动画
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f),       // 透明度从0→1
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),     // 横向缩放从0.8→1
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)      // 纵向缩放从0.8→1
        ).apply {
            this.duration = duration
            start()
        }
    }

    /**
     * 隐藏 View（带淡出+缩放动画）
     * @param view 需要隐藏的 View
     * @param duration 动画时长（毫秒）
     */
    fun hideWithAnimation(view: View, duration: Long = 300) {
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.ALPHA, 0f),       // 透明度从1→0
            PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f),   // 横向缩放从1→0.8
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f)    // 纵向缩放从1→0.8
        ).apply {
            this.duration = duration
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE  // 动画结束后设置为GONE
                }
            })
            start()
        }
    }


    /**
     * 点击后先放大再恢复的动画
     * @param view 需要执行动画的 View
     * @param scaleFactor 放大倍数（如 1.2f 表示放大到 120%）
     * @param duration 单次动画时长（毫秒）
     */
    fun performClickEffect(view: View, scaleFactor: Float = 1.2f, duration: Long = 200) {
        // 放大动画
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, scaleFactor)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, scaleFactor)

        // 恢复动画
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", scaleFactor, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", scaleFactor, 1f)

        // 设置动画属性
        scaleUpX.duration = duration
        scaleUpY.duration = duration
        scaleDownX.duration = duration
        scaleDownY.duration = duration

        // 组合动画（先放大，再恢复）
        val animatorSet = AnimatorSet()
        animatorSet.play(scaleUpX).with(scaleUpY)  // 同时执行放大
        animatorSet.play(scaleDownX).with(scaleDownY).after(scaleUpX)  // 放大后执行恢复

        // 启动动画
        animatorSet.start()
    }


}