package com.ai302.app

import android.app.Application
import android.content.Context
import androidx.databinding.library.BuildConfig
//import leakcanary.LeakCanary

/**
 * author :
 * e-mail :
 * time   : 2025/4/16
 * desc   :
 * version: 1.0
 */
class MyApplication:Application() {

    companion object {
        lateinit var myApplicationContext: Context
    }


    override fun onCreate() {
        super.onCreate()
        myApplicationContext = this
        // LeakCanary 会自动初始化并开始监控
        // 初始化 LeakCanary（无需检查分析进程）
        if (BuildConfig.DEBUG) {
//            LeakCanary.config = LeakCanary.config.copy(
//                dumpHeap = true // 确保在调试版本中启用堆转储
//            )
        }
    }

}