package com.ai302.app.http

import okhttp3.Interceptor
import okhttp3.Response

/**
 * author :
 * e-mail :
 * time   : 2025/6/5
 * desc   :
 * version: 1.0
 */
class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 1. 添加 Token 到请求头
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer")
            .build()

        // 2. 执行请求并获取响应
        val response = chain.proceed(request)

        // 3. 检查响应状态码
        if (response.code == 401) {
            // 4. 处理 401 错误（同步或异步）
            //handleUnauthorized(chain)
        }

        return response
    }

//    private fun handleUnauthorized(chain: Interceptor.Chain): Response {
//        // 方式一：直接跳转登录页（简单粗暴）
//        //navigateToLogin()
//        return Response.Builder()
//            .request(chain.request())
//            .protocol(Protocol.HTTP_1_1)
//            .code(401)
//            .message("Unauthorized")
//            .build()
//
//        // 方式二：尝试刷新 Token（推荐）
//        // return refreshTokenAndRetry(chain)
//    }
}