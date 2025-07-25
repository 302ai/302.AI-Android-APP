package com.ai302.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * author :
 * e-mail :
 * time   : 2025/4/16
 * desc   :
 * version: 1.0
 */
object TimeUtils {

    /**
     * 获取当前时间的字符串表示，格式为 yyyy-MM-dd HH:mm:ss
     * @return 当前时间的字符串
     */
    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    /**
     * 获取当前年份
     * @return 当前年份
     */
    fun getCurrentYear(): Int {
        val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    /**
     * 获取当前月份
     * @return 当前月份（1 - 12）
     */
    fun getCurrentMonth(): Int {
        val dateFormat = SimpleDateFormat("MM", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    /**
     * 获取当前日期
     * @return 当前日期
     */
    fun getCurrentDay(): Int {
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    /**
     * 获取当前小时
     * @return 当前小时
     */
    fun getCurrentHour(): Int {
        val dateFormat = SimpleDateFormat("HH", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    /**
     * 获取当前分钟
     * @return 当前分钟
     */
    fun getCurrentMinute(): Int {
        val dateFormat = SimpleDateFormat("mm", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    /**
     * 获取当前秒数
     * @return 当前秒数
     */
    fun getCurrentSecond(): Int {
        val dateFormat = SimpleDateFormat("ss", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }
}