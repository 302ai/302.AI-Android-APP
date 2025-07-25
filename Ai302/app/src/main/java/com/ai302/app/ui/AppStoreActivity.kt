package com.ai302.app.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.webkit.JavascriptInterface
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ai302.app.R
import com.ai302.app.data.AppStoreItem
import com.ai302.app.databinding.ActivityAppStoreBinding
import com.ai302.app.databinding.ActivityBottomDialogBinding
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.BridgeWebView
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.github.lzyzsd.jsbridge.DefaultHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class AppStoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppStoreBinding
    private var mWebView: BridgeWebView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        // 在 super.onCreate() 和 setContentView() 之前切换回正常主题
        setTheme(R.style.Theme_Ai302)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAppStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_app_store)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadWebHandler("https://gpts.302.ai/?simple_version=1")
        inListener()
    }


    private fun inListener(){
        binding.closeBtn.setOnClickListener {
            finish()
        }

        lifecycleScope.launch(Dispatchers.Main) {
            val rotateAnimation = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            rotateAnimation.duration = 1000
            rotateAnimation.repeatCount = Animation.INFINITE
            rotateAnimation.interpolator = LinearInterpolator()
            binding.loadingAppStore.startAnimation(rotateAnimation)
            delay(1000)
            binding.loadingAppStoreLine.visibility = View.GONE
        }

    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun loadWebHandler(htmlUrl: String) {
        mWebView = binding.appStoreWebView
        mWebView?.settings?.javaScriptEnabled = true
//        mWebView?.settings?.databaseEnabled = true //数据库缓存
        mWebView?.settings?.setGeolocationEnabled(true) // 允许网页定位
        mWebView?.settings?.loadsImagesAutomatically = true // 加快网页加载完成的速度，等页面完成再加载图片
        mWebView?.settings?.domStorageEnabled = true       // 开启 localStorage
        mWebView?.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")
        //mWebView?.setDefaultHandler(DefaultHandler())
        //mWebView?.webChromeClient = WebChromeClient()

        mWebView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 在页面加载完成后注入监听 window.postMessage 的 JavaScript 代码
                mWebView?.evaluateJavascript(
                    """
                window.addEventListener('message', function(event) {
                    AndroidInterface.receiveMessage(JSON.stringify(event.data));
                });
            """.trimIndent(), null
                )
            }
        }



        Log.e("ceshi  url是什么=================：", htmlUrl)
        mWebView?.loadUrl(htmlUrl)

        /*mWebView?.registerHandler("testiOSCallback", object : BridgeHandler {
            override fun handler(data: String?, function: CallBackFunction?) {
                Log.e("测试","ffffffffffff")
                Log.e("ceshi 是否有数据过来============:", data.toString())
                //showHtmlAnalysisData(data)
            }
        })

        mWebView?.setDefaultHandler(object : DefaultHandler() {
            override fun handler(data: String, function: CallBackFunction) {
                function.onCallBack("Native已收到消息！")
                Log.e("ceshi 是否有数据过来============:", data.toString())
            }
        })*/




    }


    private fun showHtmlAnalysisData(){

    }

    class WebAppInterface(private val context: Context) {
        @JavascriptInterface
        fun receiveMessage(message: String) {
            Log.e("ceshi", "0接收到来自 auth 的数据: $message")
            try {
                val jsonData = JSONObject(message)
                if (jsonData.has("from") && jsonData.getString("from") == "auth") {
                    val data = jsonData.get("data")
                    Log.e("ceshi", "接收到来自 auth 的数据: $data")

                    try {
                        val jsonObject = JSONObject(data.toString())
                        val uuid = jsonObject.getString("uuid")
                        val displayName = jsonObject.getString("display_name")
                        val description = jsonObject.getString("description")
                        val displayUrl = jsonObject.getString("display_url")

                        val intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra("chat_appStore", AppStoreItem(uuid,displayName,description,displayUrl))
                        intent.putExtra("chat_type", "新的聊天")
                        context.startActivity(intent)


                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // 处理接收到的数据
                    (context as? Activity)?.runOnUiThread {
                        // 如果需要更新 UI，需要在主线程执行
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }


}