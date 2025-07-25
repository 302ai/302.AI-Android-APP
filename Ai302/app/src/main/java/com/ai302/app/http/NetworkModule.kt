package com.ai302.app.http

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/4/9
 * desc   :
 * version: 1.0
 */
import android.util.Log
import com.ai302.app.MyApplication.Companion.myApplicationContext
import okhttp3.Cache
import okhttp3.Call
import okhttp3.CipherSuite
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.Dns
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://api.302.ai/"
    private const val BASE_URL_TEST = "https://test4chatgpt-chat.302.ai/"
    private const val BASE_URL_UPLOAD_IMAGE = "https://test-api2.proxy302.com/"
    private const val CUSTOMIZE_URL = "https://openrouter.ai/api/"
    private const val CUSTOMIZE_URL_TWO = "https://api.siliconflow.cn/"
    // 1. 配置OkHttpClient
    val cacheSize = 10 * 1024 * 1024 // 10 MB
    val cacheDir = File(myApplicationContext.cacheDir, "http_cache")
    val cache = Cache(cacheDir, cacheSize.toLong())

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        OkHttpClient.Builder()
            .cache(cache) // 启用缓存
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1)) // 优先 HTTP/2
            .addInterceptor(CacheInterceptor()) // 新增缓存拦截器
            .connectionPool(ConnectionPool(
                maxIdleConnections = 5, // 最大空闲连接数
                keepAliveDuration = 5, TimeUnit.MINUTES // 连接存活时间
            ))
            .addInterceptor(loggingInterceptor)
            // 在 OkHttpClient 添加事件监听器
            .eventListener(object : EventListener() {
                private var connectStartNs = 0L
                override fun callStart(call: Call) {
                    super.callStart(call)
                    Log.e("Network", "Request started")
                }

                override fun dnsStart(call: Call, domainName: String) {
                    super.dnsStart(call, domainName)
                    Log.d("Network", "DNS start: $domainName")
                }

                override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
                    super.connectStart(call, inetSocketAddress, proxy)
                    connectStartNs = System.nanoTime()
                    Log.d("Network", "Connect start")
                }

                override fun responseBodyStart(call: Call) {
                    super.responseBodyStart(call)
                    Log.d("Network", "First byte received")
                }
                override fun secureConnectStart(call: Call) {
                    Log.d("Network", "SSL handshake start")
                }

                override fun secureConnectEnd(call: Call, handshake: Handshake?) {
                    Log.d("Network", "SSL handshake end: ${elapsedNs()}ns")
                }

                override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {
                    Log.d("Network", "TCP connect end: ${elapsedNs()}ns")
                }

                private fun elapsedNs() = System.nanoTime() - connectStartNs
            })
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept-Encoding", "gzip") // 启用GZIP压缩
                    .build()
                val response = chain.proceed(request)
                Log.d("Network", "使用协议: ${response.protocol}")
                //Log.e("ceshi","错误代码：${response.code}")
                //chain.proceed(request)
                response
            }
            .dns(SmartDns())
            .build()
    }

    // 新增：根据传入的 baseUrl 动态创建 Retrofit 实例
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient) // 复用同一个 OkHttpClient
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return createRetrofit(BASE_URL).create(serviceClass)
    }

    // 新增：根据 baseUrl 和服务类创建服务
    fun <T> createServiceWithBaseUrl(serviceClass: Class<T>, baseUrl: String): T {
        return createRetrofit(baseUrl).create(serviceClass)
    }


    // 改进 DNS 缓存策略（带 TTL 和异步更新）
    class SmartDns : Dns {
        private val systemDns = Dns.SYSTEM
        private val cache = ConcurrentHashMap<String, Pair<Long, List<InetAddress>>>()
        private val cacheTTL = 300_000L // 5分钟

        override fun lookup(hostname: String): List<InetAddress> {
            val now = System.currentTimeMillis()

            cache[hostname]?.let {
                if (now - it.first < cacheTTL) return it.second
            }

            return synchronized(this) {
                systemDns.lookup(hostname).also {
                    cache[hostname] = now to it
                }
            }
        }
    }

    class FastDns : Dns {
        private val dnsCache = mutableMapOf<String, List<InetAddress>>()

        override fun lookup(hostname: String): List<InetAddress> {
            // 优先从缓存获取
            dnsCache[hostname]?.let { return it }

            // 自定义解析（如直接使用 IP）
            val addresses = InetAddress.getAllByName(hostname)
            dnsCache[hostname] = addresses.toList()
            return addresses.toList()
        }
    }


    // 缓存拦截器（控制缓存有效期、离线策略等）
    private class CacheInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            // 在线时：优先使用网络，缓存有效期 1 分钟
            val onlineCacheControl = "Cache-Control: max-age=60"
            // 离线时：使用缓存，最大有效期 7 天（根据需求调整）
            val offlineCacheControl = "Cache-Control: only-if-cached, max-stale=604800"

            try {
                return if (isNetworkAvailable()) { // 检查网络状态
                    chain.proceed(request.newBuilder().header("Cache-Control", onlineCacheControl).build())
                } else {
                    chain.proceed(request.newBuilder().header("Cache-Control", offlineCacheControl).build())
                }
            } catch (e: Exception) {
                Log.e("ceshi", "网络拦截器异常: ${e.message}", e)
                throw e // 继续抛出异常让上层处理
            }


        }

        // 检查网络状态（需结合实际实现，如使用 ConnectivityManager）
        private fun isNetworkAvailable(): Boolean {
            // ... 实现网络检测逻辑
            return true // 示例默认有网络，需根据实际情况修改
        }
    }


}