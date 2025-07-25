package com.ai302.app.plugin

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.ImageSize
import io.noties.markwon.image.ImageSizeResolver
import io.noties.markwon.image.glide.GlideImagesPlugin
import kotlin.math.roundToInt

class CustomImageSizeResolver(
    context: Context
) : ImageSizeResolver() {
    // 将 80dp 转换为像素值
    private val fixedSizePx = with(context.resources) {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            80f,
            displayMetrics
        ).toInt()
    }

    override fun resolveImageSize(drawable: AsyncDrawable): Rect {
        // 直接返回固定尺寸的正方形区域
        return Rect(0, 0, fixedSizePx, fixedSizePx)
    }
}