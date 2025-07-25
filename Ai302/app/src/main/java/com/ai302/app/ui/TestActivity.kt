package com.ai302.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Spannable
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ai302.app.R
import com.ai302.app.databinding.ActivityTestBinding
import com.ai302.app.utils.PermissionUtils
import com.ai302.app.utils.StringObjectUtils
import com.ai302.app.utils.SystemUtils.uriToTempFile
import com.ai302.app.view.SlideSwitchView
import com.ai302.app.viewModel.ChatViewModel
import com.cczhr.TTS
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.commonmark.node.Image
import pub.devrel.easypermissions.EasyPermissions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


@RequiresApi(Build.VERSION_CODES.S)
class TestActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityTestBinding
    private val PERMISSION_REQUEST_CODE = 123
    private val chatViewModel: ChatViewModel by viewModels()
    //    private lateinit var fullSpannable:Spannable
//    var currentLength = 0
    //private val PERMISSION_REQUEST_CODE = 1
    private val PICK_IMAGE_REQUEST = 2
    private val handler = Handler(Looper.getMainLooper())

    private var currentLength = 0 // 当前已显示的字符数
    private var fullSpannable: Spannable? = null // 完整解析后的 Markdown 内容（带样式）

    //录音参数
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var mediaPlayer1: MediaPlayer? = null
    private var isRecording = false
    private var isPlaying = false
    private val audioFilePath: String by lazy {
        "${getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)}/temp_audio.mp3"
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 在 super.onCreate() 和 setContentView() 之前切换回正常主题
        setTheme(R.style.Theme_Ai302)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_test)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PermissionUtils.checkRecordPermission(this)

        var webSettings: WebSettings = binding.webTest.settings
        webSettings.javaScriptEnabled = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (checkPermissions()) {
            // 权限已授予，执行相关操作
            Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            // 请求权限
            //requestPermissions()
            request()
        }



        binding.buttonTest.setOnClickListener {
            val imageUri =
                Uri.parse("file:///storage/emulated/0/Pictures/WeiXin/wx_camera_1744965074513.jpg") // 或者使用content Uri
            //loadImageWithMediaStore("wx_camera_1744965074513.jpg")
            //binding.imageTest.setImageURI(imageUri)
            openImagePicker()
        }

        binding.buttonUpLoadImage.setOnClickListener {
            val imageUri =
                Uri.parse("file:///storage/emulated/0/Pictures/WeiXin/wx_camera_1744965074513.jpg") // 或者使用content Uri
            //loadImageWithMediaStore("wx_camera_1744965074513.jpg")
            //binding.imageTest.setImageURI(imageUri)
            openImagePicker()
        }


        binding.webViewTest.settings.javaScriptEnabled = true
        binding.webViewTest.settings.domStorageEnabled = true
        binding.webViewTest.setBackgroundColor(Color.TRANSPARENT)
        //holder.webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        // 设置缓存模式为优先使用缓存，缓存不存在时从网络获取
        binding.webViewTest.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        binding.webViewTest.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                //injectXmlBackgroundCss()
                // 获取内容高度
//                view?.evaluateJavascript("document.getElementById('content').scrollHeight") { heightStr ->
//                    val contentHeight = heightStr.removeSurrounding("\"").toInt()
//
//                    // 更新 WebView 高度（主线程）
//                    binding.webViewTest.post {
//                        val layoutParams = binding.webViewTest.layoutParams.apply {
//                            height = contentHeight
//                        }
//                        binding.webViewTest.layoutParams = layoutParams
//                    }
//                }

                // 获取内容宽度
                view?.evaluateJavascript("document.getElementById('content').scrollWidth") { widthStr ->
                    val contentWidthStr = widthStr.removeSurrounding("\"").toInt()

                    // 更新 WebView 高度（主线程）
                    binding.webViewTest.post {
                        val layoutParams = binding.webViewTest.layoutParams.apply {
                            width = contentWidthStr
                        }
                        binding.webViewTest.layoutParams = layoutParams
                    }
                }

            }
        }
        //binding.webViewTest.setBackgroundColor(ContextCompat.getColor(this, R.color.dividing_line))
//        binding.webViewTest.setBackgroundResource(R.drawable.shape_select_site_bg_blue_line)
        binding.webViewTest.loadDataWithBaseURL(
            null,
            getFullHtml("你好,我的朋友111111111"),
            "text/html",
            "utf-8",
            null
        )

        binding.webViewTest.setOnClickListener {
            Log.e("ceshi", "0点击webView")
        }

        // 监听加载进度（控制动画）
        binding.webViewTest.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // 进度 0-100，进度 < 100 时显示加载动画
                binding.loadingLayout.visibility =
                    if (newProgress < 100) View.VISIBLE else View.GONE
            }
        }

        // 监听加载完成/失败（备用方案，防止 onProgressChanged 未覆盖所有情况）
        binding.webViewTest.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.loadingLayout.visibility = View.GONE // 加载完成后隐藏动画
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                binding.loadingLayout.visibility = View.GONE // 加载失败时隐藏动画
                // 可选：显示错误提示（如 Toast 或自定义错误页面）
            }
        }



        binding.webViewTest.setOnTouchListener(OnTouchListener { v, event ->
            // 处理触摸事件，例如记录点击位置等
//            Log.e("ceshi","1点击webView")
//            false // 返回false表示事件没有被完全消费，可以继续传递到WebView内部处理（如点击链接）
            // 只处理按下（ACTION_DOWN）或抬起（ACTION_UP）事件（根据需求选择）
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 按下时记录日志（仅触发1次）
                    Log.e("ceshi", "WebView按下事件")
                    false  // 让事件继续传递给WebView内部处理
                }

                MotionEvent.ACTION_UP -> {
                    // 抬起时记录日志（仅触发1次）
                    Log.e("ceshi", "WebView抬起事件")
                    false  // 让事件继续传递给WebView内部处理
                }

                else -> {
                    // 其他事件（如ACTION_MOVE）不处理
                    false
                }
            }
        })


        //testMarkWon()

        binding.loadThreeView.startAnim()
        binding.stopView.setOnClickListener {
            binding.loadThreeView.stopAnim()
        }


        //initRecodAudio()
        testLookWeb()
        binding.slideView.setOnSwitchClickListener(object : SlideSwitchView.OnSwitchClickListener{
            override fun onClick(side: String) {
                when (side) {
                    "preview" -> {
                        // 处理预览点击事件
                        println("用户点击了预览区域")
                    }
                    "code" -> {
                        // 处理代码点击事件
                        println("用户点击了代码区域")
                    }
                }
            }

        })
    }


    //权限方法
    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestPermissions() {
        Log.e("ceshi", "权限申请")
//        requestPermissions(
//            arrayOf(
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ),
//            1
//        )


//        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
//        requestPermissions(permissions,1)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                0
            )

            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0
            )
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0
            )
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 0
            )

//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ),
//                1
//            )
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("ceshi", "第一个${grantResults.isNotEmpty()}")
        Log.e("ceshi", "第2个${grantResults[0]}")
        //Log.e("ceshi","第2个${grantResults[1]}")
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // 权限已授予，执行相关操作
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()

            } else {
                // 权限被拒绝
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
                //requestPermissions()
                // 权限被永久拒绝，引导用户到应用设置页面
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        for (temp in perms) {

            if (temp == android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                Log.e("ceshi", "CAMERA onPermissionsGranted")
            }

        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d("onPermissionsDenied", "onPermissionsDenied:" + requestCode + ":" + perms.size)
    }

    private fun request() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            //可以添加其他的权限，用来判断
        )
        //判断有没有权限
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            // 有权限，需要做什么
        } else {
            // 没有权限, 申请权限
            EasyPermissions.requestPermissions(
                this,
                "摄像机需要用户允许才能调用，请开启相关权限（理由）",
                1,
                *permissions
            )
        }
    }


    // 通过 MediaStore 查询图片 URI
    private fun loadImageWithMediaStore(fileName: String) {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        contentResolver.query(
            contentUri,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val id = cursor.getLong(idColumn)
                val imageUri = ContentUris.withAppendedId(contentUri, id)
                // 加载图片到 ImageView
                binding.imageTest.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "文件未找到", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            Log.e("ceshi", "返回的URL：${selectedImageUri}")
            //binding.imageTest.setImageURI(selectedImageUri)
            binding.webTest.loadDataWithBaseURL(
                null,
                getFullHtml("$selectedImageUri"),
                "text/html",
                "utf-8",
                null
            )
            lifecycleScope.launch(Dispatchers.IO) {
                //chatViewModel.upLoadImage(this@TestActivity, uriToTempFile(this@TestActivity,selectedImageUri!!),"imgs",false)
            }

        }
    }


    private fun getFullHtml(content: String): String {
        return getBaseHtml(content)
    }

    private fun getBaseHtml(content: String): String {
        val directionStyle = if (true) "" else "direction: rtl;"
        val Style =
            if (false) "<img src=\"$content\"width=100,height=200 alt=\"Sample image\">" else "<div id=\"content\">$content</div>"

        Log.e("ceshi", "0图片地址$Style")

        var htmlContent = ""
        htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            body { margin:0; padding:0; }
            #content {
                background-color: #FFA500; // 背景颜色
                display:inline-block;
            }
        </style>
                <style>
                    body {
                        $directionStyle
                    }
                </style>
                
                 <script>
                    function updateContent(text) {
                        document.getElementById('content').innerHTML = text;
                        MathJax.Hub.Queue(['Typeset', MathJax.Hub]);
                    }
                </script>
                
                <script type="text/javascript" async
                        src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML">
                </script>
            </head>
            <body>
       
                <div id="content">${Style}</div>
            </body>
            </html>
        """.trimIndent()
        return htmlContent

    }


    private fun injectXmlBackgroundCss() {
        val drawable =
            ResourcesCompat.getDrawable(resources, R.drawable.shape_select_site_bg_blue_line, null)
                ?: run {
                    Toast.makeText(this, "加载 XML 资源失败", Toast.LENGTH_SHORT).show()
                    return
                }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),  // 避免宽高为 0
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        outputStream.close()

        val css = """
            <style>
                body * {
                    background-image: url('file:///android_asset/test.png');
                    background-size: cover;
                    background-clip: text;
                    -webkit-background-clip: text;
                    color: transparent;
                }
            </style>
        """.trimIndent()

        val js = "document.head.innerHTML += '$css';"
        binding.webTest.evaluateJavascript(js, null)
    }


    private fun testMarkWon(){

        // 初始化Markwon（包含常用插件）
        val markwon = Markwon.builder(this)
            .usePlugin(CorePlugin.create()) // 核心解析插件
            .usePlugin(HtmlPlugin.create()) // 支持HTML标签
            .usePlugin(StrikethroughPlugin.create()) // 支持删除线
            .usePlugin(TaskListPlugin.create(this)) // 支持任务列表
            .usePlugin(TablePlugin.create(this)) // 支持表格
            .usePlugin(GlideImagesPlugin.create(this)) // 使用Glide加载图片（需添加Glide依赖）
            .build()

        // 要渲染的Markdown内容（示例包含多种语法）
        val markdown = """
            # 欢迎使用 Markwon
            ![示例图片](content://media/external/images/media/99)
            ## 基础语法
            
            - 这是无序列表
            - 支持**加粗**、*斜体*、~~删除线~~
            - 任务列表：
              - [x] 已完成任务
              - [ ] 未完成任务
            
            ## 代码块
            ```kotlin
            fun hello() {
                println("Hello Markwon!")
            }
            ```
            
            ## 表格
            | 标题1 | 标题2 |
            |------|------|
            | 内容1 | 内容2 |
            
            ## 图片
            ![示例图片](https://picsum.photos/400/200)
            
            ## HTML 支持
            <span style="color: #2196F3; font-weight: bold;">这是蓝色加粗的HTML文本</span>
        """.trimIndent()

        // 将Markdown渲染到TextView
        markwon.setMarkdown(binding.markDownTv, markdown)

        fullSpannable = (binding.markDownTv.text as? Spannable)!! // 获取解析后的 Spannable
        // 2. 清空 TextView，准备流式显示
        binding.markDownTv.text = ""
        currentLength = 0
        // 3. 启动流式显示动画（50ms / 字符）
        startTypingAnimation()

        lifecycleScope.launch(Dispatchers.Main) {

        }

    }


    private fun startTypingAnimation (delayPerChar: Long = 50) {
        val totalLength = fullSpannable?.length ?: 0
        if (totalLength == 0) return
        // 使用 Handler 定时更新显示内容
        handler.postDelayed (object : Runnable {
            override fun run () {
                if (currentLength <= totalLength) {
        // 截取完整 Spannable 的前 currentLength 个字符（保留样式）
                    val currentText = fullSpannable?.subSequence (0, currentLength)
                    binding.markDownTv.text = currentText
            // 滚动到最新显示的位置（长文本时有用）
                    binding.markDownTv.scrollTo (0, binding.markDownTv.lineHeight * binding.markDownTv.lineCount)
                    currentLength++
                    handler.postDelayed (this, delayPerChar) // 继续下一个字符
                }
            }
        }, delayPerChar) // 初始延迟（与单个字符间隔相同）
    }

    override fun onDestroy() {
        super.onDestroy()
        // 销毁时停止动画，避免内存泄漏
        handler.removeCallbacksAndMessages (null)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun initRecodAudio(){
        binding.recordAudio.setOnClickListener {
            startRecording()
        }
        binding.stopRecord.setOnClickListener {
            stopRecording()
        }
        binding.playAudio.setOnClickListener {
            startPlaying()
        }
        binding.stopAudio.setOnClickListener {
            stopPlaying()
        }
        binding.playUrlAudio.setOnClickListener {
            // 需要申请的权限（网络权限）
            val requiredPermissions = arrayOf(Manifest.permission.INTERNET)
            // 检查权限（Android 12+ 需声明网络权限）
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, requiredPermissions, 1001)
            }
            val url1 = "https://raw.githubusercontent.com/SWivid/F5-TTS/refs/heads/main/src/f5_tts/infer/examples/basic/basic_ref_zh.wav"
            val url2 = "https://www2.cs.uic.edu/~i101/SoundFiles/GoodMorningSunshine.wav"
            startPlayer(url2)
        }

        binding.playTTS.setOnClickListener {
            val tts = TTS.getInstance()
            tts.init(this,"李华",50,150)
            tts.init(this)
            tts.speakText("你好啊朋友,你是谁？你在这里做什么？一起来玩一下。")
        }

    }


    // 开始录音
    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)       // 音频源：麦克风
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)  // 输出格式（支持MP3编码）
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)     // 音频编码器（AAC兼容MP3）
            setOutputFile(audioFilePath)                        // 输出路径
            setAudioSamplingRate(44100)                         // 采样率（标准CD音质）
            setAudioEncodingBitRate(192000)                     // 比特率（192kbps高品质）

            try {
                prepare()
                start()
                isRecording = true
//                btnRecord.text = "停止录音"
//                tvStatus.text = "状态：录音中..."
                //binding.recordAudio.isEnabled = false
            } catch (e: Exception) {
                Toast.makeText(this@TestActivity, "录音初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 停止录音
    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                // 防止异常崩溃
            }
            mediaRecorder = null
            isRecording = false
//            btnRecord.text = "开始录音"
//            tvStatus.text = "状态：录音完成，文件路径：$audioFilePath"
            //btnPlay.isEnabled = true  // 录音完成后启用播放按钮
        }
    }

    // 开始播放
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun startPlaying() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
                //isPlaying = true
//                btnPlay.text = "停止播放"
//                tvStatus.text = "状态：播放中..."
                setOnCompletionListener {
                    stopPlaying()  // 播放完成自动停止
                }

            } catch (e: Exception) {
                Toast.makeText(this@TestActivity, "播放失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        val job = lifecycleScope.launch(Dispatchers.IO) {
            val requestFile = RequestBody.create(
                "audio/mpeg".toMediaTypeOrNull(),  // MP3文件的MIME类型
                File(audioFilePath)  // 待上传的文件对象
            )

            val filePart = MultipartBody.Part.createFormData(
                "file",  // API接口要求的文件参数名（需与后端约定）
                "temp_audio.mp3",  // 上传时的文件名（可选，后端可自定义）
                requestFile  // 前面生成的RequestBody
            )

            //chatViewModel.audioToText(filePart,"")
        }

    }

    // 停止播放
    private fun stopPlaying() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        isPlaying = false
//        btnPlay.text = "播放录音"
//        tvStatus.text = "状态：播放停止"
    }

    private fun startPlayer(audioUrl:String) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            Log.e("ceshi","播放$audioUrl")
//            try {
//                mediaPlayer1 = MediaPlayer()
//                mediaPlayer1?.reset()
//                mediaPlayer1?.setDataSource(this@TestActivity, Uri.parse(audioUrl))
//                mediaPlayer1?.prepareAsync() // 异步准备（适合网络音频）
//            } catch (e: IOException) {
//                e.printStackTrace()
//                Log.e("ceshi","播放失败：${e.toString()}")
//                lifecycleScope.launch(Dispatchers.Main) {
//                    //tvStatus.text = "状态：初始化失败（${e.message})"
//                }
//            }
//        }

        lifecycleScope.launch(Dispatchers.IO) {
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(this@TestActivity, Uri.parse(audioUrl))
                    prepare()
                    start()
                    //isPlaying = true
//                btnPlay.text = "停止播放"
//                tvStatus.text = "状态：播放中..."
                    setOnCompletionListener {
                        stopPlaying()  // 播放完成自动停止
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@TestActivity, "播放失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    private fun testLookWeb(){
        binding.lookWeb.settings.javaScriptEnabled = true// 启用JavaScript（可选，根据HTML内容需求）
        binding.lookWeb.settings.domStorageEnabled = true// 启用DOM存储（可选）
        // 定义要展示的HTML内容（包含小鸟形状的SVG）
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <body>
                <h3>HTML 绘制的小鸟</h3>
                <svg width="300" height="300">
                    <!-- 小鸟身体（圆形） -->
                    <circle cx="150" cy="150" r="50" fill="#FFD700" /> 
                    
                    <!-- 小鸟头部（小圆形） -->
                    <circle cx="180" cy="130" r="20" fill="#FFA500" />
                    
                    <!-- 小鸟嘴巴（三角形） -->
                    <polygon points="195,130 215,140 195,150" fill="#FF4500" />
                    
                    <!-- 小鸟眼睛（圆形） -->
                    <circle cx="185" cy="125" r="3" fill="black" />
                    
                    <!-- 小鸟翅膀（椭圆） -->
                    <ellipse cx="120" cy="160" rx="30" ry="20" fill="#FFD700" />
                </svg>
            </body>
            </html>
        """.trimIndent()

        // 加载HTML内容到WebView
        binding.lookWeb.loadDataWithBaseURL(
            null,
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )

        val test = "你好啊，这是一个HTML语言：```html<div>这是小鸟的HTML代码</div>```，结束。"
        val extractedHtml = StringObjectUtils.extractHtmlFromMarkdown(test)
        Log.e("ceshi","输出代码：$extractedHtml")


    }


}