package com.ai302.app.http

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/4/9
 * desc   :
 * version: 1.0
 */
object NetworkFactory {
    fun <T> createApiService(serviceClass: Class<T>,baseUrl:String): T {
        //return NetworkModule.createService(serviceClass)
        return NetworkModule.createServiceWithBaseUrl(serviceClass, baseUrl)
    }
}