package com.ai302.app.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.ai302.app.R

/**
 * author :
 * e-mail :
 * time   : 2025/6/12
 * desc   :
 * version: 1.0
 */
class CustomToast private constructor(
    private val context: Context,
    private val message: String,
    private val duration: Int = Toast.LENGTH_SHORT,
    private val gravity: Int = Gravity.CENTER,
    private val xOffset: Int = 0,
    private val yOffset: Int = 0
) {
    fun show() {
        val toast = Toast(context)
        toast.duration = duration
        toast.setGravity(gravity, xOffset, yOffset)

        // 创建自定义布局
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast_layout, null)

        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = message

        toast.view = layout
        toast.show()
    }

    companion object {
        fun makeText(
            context: Context,
            message: String,
            duration: Int = Toast.LENGTH_SHORT,
            gravity: Int = Gravity.CENTER,
            xOffset: Int = 0,
            yOffset: Int = 0
        ): CustomToast {
            return CustomToast(context, message, duration, gravity, xOffset, yOffset)
        }
    }
}

