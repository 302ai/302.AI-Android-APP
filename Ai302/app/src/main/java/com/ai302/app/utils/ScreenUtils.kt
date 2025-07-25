package com.ai302.app.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.ai302.app.R
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

/**
 * author :
 * e-mail :
 * time   : 2025/4/27
 * desc   :
 * version: 1.0
 */
object ScreenUtils {
    /**
     * 获取屏幕宽度（像素）
     */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * 获取字符串的显示宽度（像素）
     * @param text 目标字符串
     * @param textSizePx 文本大小（像素）
     * @param typeface 字体（可选）
     */
    fun getTextWidth(text: String, textSizePx: Float, typeface: Typeface = Typeface.DEFAULT): Float {
        return Paint().apply {
            this.textSize = textSizePx
            this.typeface = typeface
            isAntiAlias = true
        }.measureText(text)
    }

    fun getTextWidth1(text: String, textSize: Float, typeface: Typeface = Typeface.DEFAULT): Float {
        // 创建 Paint 对象
        val paint = Paint().apply {
            this.textSize = textSize  // 设置字号（单位：px）
            this.typeface = typeface  // 设置字体（可选，默认是系统字体）
            isAntiAlias = true        // 开启抗锯齿（优化显示效果）
        }
        // 测量字符串宽度（单位：px）
        return paint.measureText(text)
    }

    /**
     * 判断字符串宽度是否小于屏幕宽度
     * @param context 上下文
     * @param text 目标字符串
     * @param textSizeSp 文本大小（sp）
     * @param typeface 字体（可选）
     */
    fun isTextWidthLessThanScreen(
        context: Context,
        text: String,
        textSizeSp: Float,
        typeface: Typeface = Typeface.DEFAULT
    ): Boolean {
        val screenWidth = getScreenWidth(context)
        val textSizePx = textSizeSp * context.resources.displayMetrics.scaledDensity
        val textWidth = getTextWidth1(text, sp2px(context,16f), typeface)
        Log.e("ceshi","屏幕宽度：$screenWidth,,字符串宽度：$textWidth")
        return textWidth < screenWidth
    }


    /**
     * 将 sp 转换为 px（Float 类型，适用于 Paint 等场景）
     */
    fun sp2px(context: Context, spValue: Float): Float {
        return spValue * context.resources.displayMetrics.scaledDensity
    }

    /**
     * 将 sp 转换为 px（Int 类型，适用于布局参数等场景）
     */
    fun sp2pxInt(context: Context, spValue: Float): Int {
        return (sp2px(context, spValue) + 0.5f).toInt()
    }

    /**
     * 将 dp 转换为 px（扩展方法，可选）
     */
    fun dp2px(context: Context, dpValue: Float): Float {
        return dpValue * context.resources.displayMetrics.density
    }

    // Markdown 转换工具函数
    fun markdownToHtml(markdown: String): String {
        val parser = Parser.builder()
            .extensions(listOf(TablesExtension.create())) // 可选扩展
            .build()
        val document = parser.parse(markdown)
        val renderer = HtmlRenderer.builder()
            .escapeHtml(true) // 自动转义 HTML 特殊字符
            .build()
        return renderer.render(document)
    }

    fun onClickColor(view: View){
//        view.setBackgroundColor(
//            ContextCompat.getColor(this, R.color.colorSelect)
//        )
        view.setBackgroundResource(R.drawable.shape_select_site_bg_gray_home_line2)
    }



}