package com.ai302.app.plugin

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.LeadingMarginSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ai302.app.R
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.CodeBlockSpan


class CodeCopySpan(
    private val theme: MarkwonTheme,
    private val code: String,
    private val textView: TextView
) : LeadingMarginSpan, Runnable {

    private var width: Int = 0
    private var copyButton: View? = null
    //private var canvas:Canvas? = null

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence?, start: Int, end: Int, first: Boolean,
        layout: Layout?
    ) {
        if (first) {
            val right = x + dir * width
            setupCopyButton(right, top, bottom,canvas)
        }
    }

    private fun setupCopyButton(right: Int, top: Int, bottom: Int,canvas: Canvas) {
        if (copyButton == null) {
            val inflater = LayoutInflater.from(textView.context)
            copyButton = inflater.inflate(R.layout.code_copy_button, null).apply {
                setOnClickListener {
                    copyToClipboard(code)
                    // 可选：显示复制成功提示
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                }
            }

            // 测量并定位按钮
            copyButton!!.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            width = copyButton!!.measuredWidth

            // 定位到代码块的右上角
            copyButton!!.layout(
                right - width,
                top,
                right,
                top + copyButton!!.measuredHeight
            )
        }

        // 绘制按钮
        copyButton!!.draw(canvas)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = ContextCompat.getSystemService(textView.context, ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("code", text))
    }

    override fun getLeadingMargin(first: Boolean): Int = if (first) width else 0
    override fun run() {

    }
}