package com.ai302.app.ui

import SettingDialog
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ai302.app.MyApplication
import com.ai302.app.R
import com.ai302.app.adapter.EmojiAdapter
import com.ai302.app.data.AppStoreItem
import com.ai302.app.databinding.ActivityMainBinding
import com.ai302.app.databinding.ActivitySettingBinding
import com.ai302.app.datastore.DataStoreManager
import com.ai302.app.http.ApiService
import com.ai302.app.http.NetworkFactory
import com.ai302.app.infa.OnSettingDialogClickListener
import com.ai302.app.room.ChatItemRoom
import com.ai302.app.utils.CustomUrlSpan
import com.ai302.app.utils.ScreenUtils.onClickColor
import com.ai302.app.utils.SystemUtils
import com.ai302.app.utils.ViewAnimationUtils
import com.ai302.app.viewModel.ChatViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.lzyzsd.jsbridge.BridgeWebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class SettingActivity : AppCompatActivity(), OnSettingDialogClickListener {
    private lateinit var binding: ActivitySettingBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private var imageUrlServiceResult = ""
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var emojiAdapter: EmojiAdapter
    private lateinit var fadeInAnimation: Animation
    private lateinit var fadeOutAnimation: Animation
    private var isEmojiPanelVisible = false
    private var isClickTest = false
    private val BASE_URL = "https://api.302.ai/"
    private var CUSTOMIZE_URL_TWO = "https://api.siliconflow.cn/"
    private var apiService = NetworkFactory.createApiService(ApiService::class.java,BASE_URL)
    private var serviceProvider = "302AI"

    private var localChatType = ""
    private var localChatPrompt = "这是删除过的内容变为空白"
    private var localIsPrompt = false

    private var mWebView: BridgeWebView? = null
    private var mDialog:SettingDialog?=null

    // 初始化表情列表
    val emojis = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
        "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
        "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤯",
        "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔",
        "🤭", "🤫", "🤥", "😶", "😐", "😑", "😒", "🙄", "😳", "🤤",
        "😪", "😴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠",
        "😈", "👿", "👹", "👺", "💀", "☠️", "👻", "👽", "👾", "🤖",
        "💩", "👻", "🎃", "😺", "😸", "😹", "😻", "😼", "😽", "🙀",
        "😿", "😾", "👐", "👏", "🤝", "👍", "👎", "✊", "👊", "🤛", "🤜",
        "🤞", "✌️", "🤟", "👌", "👈", "👉", "👆", "👇", "☝️", "✋",
        "🤚", "🖐️", "🖖", "👋", "🤙", "💪", "🦾", "👂", "🦻", "👃",
        "👁️", "👀", "👅", "👄", "👶", "🧒", "👦", "👧", "👨", "👩",
        "🧑", "👴", "👵", "👱", "👮", "🕵️", "👩‍⚕️", "👨‍⚕️", "👩‍🌾", "👨‍🌾",
        "👩‍🍳", "👨‍🍳", "👩‍🎓", "👨‍🎓", "👩‍🎤", "👨‍🎤", "👩‍💻", "👨‍💻", "👩‍🏫", "👨‍🏫",
        "👩‍⚖️", "👨‍⚖️", "👩‍🚒", "👨‍🚒", "👩‍✈️", "👨‍✈️", "👩‍🚀", "👨‍🚀", "👩‍⚙️", "👨‍⚙️",
        "👩‍🔧", "👨‍🔧", "👩‍🎨", "👨‍🎨", "👩‍🏭", "👨‍🏭", "👩‍💼", "👨‍💼", "👩‍🔬", "👨‍🔬",
        "👩‍💻", "👨‍💻", "👩‍🎓", "👨‍🎓", "👩‍🏫", "👨‍🏫", "👩‍⚕️", "👨‍⚕️", "👩‍🔧", "👨‍🔧",
        "👩‍🎤", "👨‍🎤", "👩‍🚒", "👨‍🚒", "👩‍✈️", "👨‍✈️", "👩‍🚀", "👨‍🚀", "👩‍⚖️", "👨‍⚖️"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        // 在 super.onCreate() 和 setContentView() 之前切换回正常主题
        setTheme(R.style.Theme_Ai302)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_setting)
        dataStoreManager = DataStoreManager(MyApplication.myApplicationContext)
        binding = ActivitySettingBinding.inflate(layoutInflater)

        lifecycleScope.launch((Dispatchers.IO)) {

            val readAppEmojisData = dataStoreManager.readAppEmojisData.first()
            readAppEmojisData?.let {
                Log.e("setting","readAppEmojisData是多少：$it")
                binding.imageProfile.text = it
            }

        }

        /*chatViewModel.imageUrlServiceResult.observe(this){
            Log.e("ceshi","返回的图片地址回复：$it")
            it?.let {
                imageUrlServiceResult = it

                lifecycleScope.launch(Dispatchers.IO) {
                    Log.e("ceshi","输入：${imageUrlServiceResult}")
                    dataStoreManager.saveImageUrl(imageUrlServiceResult)
                }
                // 方法1：使用内置的CircleCrop变换
                Glide.with(this)
                    .load(imageUrlServiceResult)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.imageProfile)


            }
        }*/

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initListener()


    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onResume() {
        super.onResume()
        binding.settingBack.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        binding.editImage.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        lifecycleScope.launch((Dispatchers.IO)) {
            val readServiceProviderData = dataStoreManager.readServiceProviderData.first()
            val readServiceUrl = dataStoreManager.readServiceUrl.first()?:""
            val readCueWordsSwitch = dataStoreManager.readCueWordsSwitch.first()
            val readCueWords = dataStoreManager.readCueWords.first()
            val readModelType = dataStoreManager.readModelType.first()
            readCueWordsSwitch?.let {
                localIsPrompt = it
            }
            readCueWords?.let {
                if (localIsPrompt){
                    localChatPrompt = it
                }
            }
            readModelType?.let {
                localChatType = it
            }

            if (CustomUrlSpan.UrlValidator.isValid302Url(readServiceUrl)){
                CUSTOMIZE_URL_TWO = readServiceUrl
            }else{

            }
            readServiceProviderData?.let {
                Log.e("ceshi","服务商是多少：$readServiceProviderData")
                serviceProvider = it
                if (it=="302.AI"){
                    apiService = NetworkFactory.createApiService(ApiService::class.java,BASE_URL)
                }else if (it=="自定义"){
                    apiService = NetworkFactory.createApiService(ApiService::class.java,CUSTOMIZE_URL_TWO)
                }
            }



            val data = dataStoreManager.readData.first()
            data?.let {
                Log.e("setting","appKey是多少：$it")
                //chatViewModel.get302AiModelList(it,apiService)
            }

            val readAppEmojisData = dataStoreManager.readAppEmojisData.first()
            readAppEmojisData?.let {
                Log.e("setting","readAppEmojisData是多少：$it")
                binding.imageProfile.text = it
            }

        }
    }

    private fun initListener(){
        // 初始化动画
        fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeOutAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)


        // 设置RecyclerView
//        binding.emojiRecyclerView.layoutManager = GridLayoutManager(this, 5)
//        emojiAdapter = EmojiAdapter(emojis) { selectedEmoji ->
//            // 表情被选中时更新ImageView并隐藏面板
//            updateSelectedEmoji(selectedEmoji)
//            toggleEmojiPanelVisibility()
//        }
//        binding.emojiRecyclerView.adapter = emojiAdapter


        val settingDialog = SettingDialog(this,this)
        mDialog = settingDialog
        chatViewModel.modelListResult.observe(this){
            Log.e("setting","模型列表回复：$it")
            if (isClickTest){
                isClickTest = false
                if (it.isEmpty()){
                    //isTrueApiKey = false
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@SettingActivity, "返回模型为空，请检查域名或者apikey并重试", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    //isTrueApiKey = true
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@SettingActivity, "域名或者apikey正确，谢谢", Toast.LENGTH_SHORT).show()
                    }
                }
            }


            it?.let {
                settingDialog.setModelList(it)
            }

        }
        binding.editImage.setOnClickListener {
            onClickColor(it)

            settingDialog.showDialog()
        }

        settingDialog.setOnDismissListener {
            binding.editImage.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        }

        binding.settingBack.setOnClickListener {
            onClickColor(it)
            //finish()
            val intent = Intent(this@SettingActivity, ChatActivity::class.java)
            if (localIsPrompt){
                //localChatPrompt
            }else{
                localChatPrompt = "这是删除过的内容变为空白"
            }
            intent.putExtra("chat_prompt", localChatPrompt)
            //intent.putExtra("chat_type", )
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }

        binding.imageProfile.setOnClickListener {
            // 调用相册选择器
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(intent, PICK_IMAGE_REQUEST)

            //toggleEmojiPanelVisibility()
            showEmojiPickerDialog()

        }

        lifecycleScope.launch(Dispatchers.IO) {
            val data = dataStoreManager.readCueWordsSwitch.first()?: true
            data?.let {
                Log.e("ceshi", "提示词开关：$it")
                lifecycleScope.launch(Dispatchers.Main) {
                    if (it){
                        binding.cueWordsSwitch.isChecked = true
                        localIsPrompt = true
                    }else{
                        binding.cueWordsSwitch.isChecked = false
                        localIsPrompt = false
                    }
                }

            }

            val dataCueWord = dataStoreManager.readCueWords.first()?: "你好，有什么问题都可以问我。"
            dataCueWord?.let {
                Log.e("ceshi", "提示词是：$it")
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.editCueWord.setText(it)
                }

            }

            val dataOfficialCueWord = dataStoreManager.readOfficialWordsSwitch.first()?: true
            dataOfficialCueWord?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                   binding.officialWordsSwitch.isChecked = it
                }

            }

            val dataClearWordsSwitch = dataStoreManager.readClearWordsSwitch.first()?: true
            dataClearWordsSwitch?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.clearSwitch.isChecked = it
                }

            }

            val dataExtractSwitch = dataStoreManager.readExtractSwitch.first()?: true
            dataExtractSwitch?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.extractSwitch.isChecked = it
                }

            }

            val dataPreSwitch = dataStoreManager.readPreSwitch.first()?: true
            dataPreSwitch?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.preSwitch.isChecked = it
                }

            }

            val dataSearchSwitch = dataStoreManager.readSearchSwitch.first()?: true
            dataSearchSwitch?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.searchSwitch.isChecked = it
                }

            }

        }


        binding.cueWordsSwitch.setOnCheckedChangeListener { _, isChecked ->

            lifecycleScope.launch(Dispatchers.IO) {
                Log.e("ceshi","输入开关：${isChecked}")
                dataStoreManager.saveCueWordsSwitch(isChecked)
            }

        }

        binding.officialWordsSwitch.setOnCheckedChangeListener { _, isChecked ->

            lifecycleScope.launch(Dispatchers.IO) {
                Log.e("ceshi","输入开关：${isChecked}")
                dataStoreManager.saveOfficialWordsSwitch(isChecked)
            }

        }

        binding.clearSwitch.setOnCheckedChangeListener { _, isChecked ->

            lifecycleScope.launch(Dispatchers.IO) {
                Log.e("ceshi","输入开关：${isChecked}")
                dataStoreManager.saveClearWordsSwitch(isChecked)
            }

        }

        binding.preSwitch.setOnCheckedChangeListener { _, isChecked ->

            lifecycleScope.launch(Dispatchers.IO) {
                Log.e("ceshi","输入开关：${isChecked}")
                dataStoreManager.savePreSwitch(isChecked)
            }

        }

        binding.searchSwitch.setOnCheckedChangeListener { _, isChecked ->

            lifecycleScope.launch(Dispatchers.IO) {
                Log.e("ceshi","输入开关：${isChecked}")
                dataStoreManager.saveSearchSwitch(isChecked)
            }

        }

        binding.extractSwitch.setOnCheckedChangeListener { _, isChecked ->

            lifecycleScope.launch(Dispatchers.IO) {
                Log.e("ceshi","输入开关：${isChecked}")
                dataStoreManager.saveExtractSwitch(isChecked)
            }

        }

        binding.editCueWord.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("ceshi","输入：${s}")
                    dataStoreManager.saveCueWords(s.toString())
                    localChatPrompt = s.toString()
                }
            }
        })


        /*lifecycleScope.launch((Dispatchers.IO)) {
            val data = dataStoreManager.readImageUrl.first()
            data?.let {
                Log.e("ceshi", "imageurl是个多少：$it")
                lifecycleScope.launch(Dispatchers.Main) {
                    // 方法1：使用内置的CircleCrop变换
                    Glide.with(this@SettingActivity)
                        .load(it)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(binding.imageProfile)
                }

            }
        }*/

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data!!

            Toast.makeText(this, "正在上传图片，请稍后", Toast.LENGTH_SHORT).show()
            Log.e("ceshi","设置界面返回图片${selectedImageUri}")

            //上传图片到服务器
            lifecycleScope.launch(Dispatchers.IO) {
                chatViewModel.upLoadImage(
                    this@SettingActivity,
                    SystemUtils.uriToTempFile(this@SettingActivity, selectedImageUri), "imgs", false,apiService
                )
            }


        }
    }



    private fun showEmojiPickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_emoji_picker)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        var recyclerView = dialog.findViewById<RecyclerView>(R.id.emojiRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = EmojiAdapter(emojis) { selectedEmoji ->
            // 更新TextView显示选中的表情
            binding.imageProfile.text = selectedEmoji
            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.saveAppEmojisData(selectedEmoji)
            }

            // 添加选中动画效果
            binding.imageProfile.scaleX = 0.8f
            binding.imageProfile.scaleY = 0.8f
            binding.imageProfile.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .withEndAction {
                    binding.imageProfile.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                }
                .start()

            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onModelTypeClick(modelType: String,mServiceProvider:String) {

        Log.e("ceshi","服务商：$mServiceProvider,,$CUSTOMIZE_URL_TWO,,$BASE_URL")
        if (modelType == "testApiKey"){



            isClickTest = true
            lifecycleScope.launch((Dispatchers.IO)) {
                if (mServiceProvider=="302.AI"){
                    apiService = NetworkFactory.createApiService(ApiService::class.java,BASE_URL)
                    val data = dataStoreManager.readData.first()
                    data?.let {
                        Log.e("setting","appKey是多少：$it")
                        chatViewModel.get302AiModelList(it,apiService)
                    }
                }else if (mServiceProvider=="自定义"){
                    apiService = NetworkFactory.createApiService(ApiService::class.java,CUSTOMIZE_URL_TWO)
                    val readCustomizeKeyData = dataStoreManager.readCustomizeKeyData.first()
                    readCustomizeKeyData?.let {
                        Log.e("setting","0appKey是多少：$it")
                        chatViewModel.get302AiModelList(it,apiService)
                    }
                }


            }
            Toast.makeText(this, "正在检测中，请稍后", Toast.LENGTH_SHORT).show()
        }else if (modelType == "login"){
            showLoginPickerDialog(mServiceProvider)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.e("ceshi","Setting拦截返回")
        val intent = Intent(this@SettingActivity, ChatActivity::class.java)
        if (localIsPrompt){
            //localChatPrompt
        }else{
            localChatPrompt = "这是删除过的内容变为空白"
        }
        intent.putExtra("chat_prompt", localChatPrompt)
        //intent.putExtra("chat_type", )
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }


    private fun showLoginPickerDialog(url:String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_login_picker)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        mWebView = dialog.findViewById<BridgeWebView>(R.id.webView_login)
        mWebView?.settings?.javaScriptEnabled = true
//        mWebView?.settings?.databaseEnabled = true //数据库缓存
        mWebView?.settings?.setGeolocationEnabled(true) // 允许网页定位
        mWebView?.settings?.loadsImagesAutomatically = true // 加快网页加载完成的速度，等页面完成再加载图片
        mWebView?.settings?.domStorageEnabled = true       // 开启 localStorage
        /*mWebView?.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")
        mWebView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 在页面加载完成后注入监听 window.postMessage 的 JavaScript 代码
                mWebView?.evaluateJavascript(
                    """
                window.addEventListener('info', function(info) {
                    AndroidInterface.receiveMessage(JSON.stringify(info.data));
                });
            """.trimIndent(), null
                )
            }
        }*/
        mWebView?.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                // 先调用父类方法获取原始响应（也可自己重新请求网络获取响应，按需选择）
                val originalResponse = super.shouldInterceptRequest(view, request)

                if (request?.url.toString().contains("apikey=")) {
                    // 解析 JSON 数据，假设返回的是 JSON 格式
                    try {
                        Log.e("ceshi","不是空${request?.url.toString()}")
                        Log.e("ceshi","截取的key：${extractApiKey(request?.url.toString())}")
                        lifecycleScope.launch(Dispatchers.IO) {
                            dataStoreManager.saveData(extractApiKey(request?.url.toString())!!)
                        }
                        lifecycleScope.launch(Dispatchers.Main) {
                            mDialog?.findViewById<EditText>(R.id.edit_apiKey)?.setText(extractApiKey(request?.url.toString()))
                            Toast.makeText(this@SettingActivity, "登录成功，谢谢", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return originalResponse
            }
        }



        val html = "https://dash.302.ai/sso/login?app=302+AI+Studio&name=302+AI+Studio&icon=https://file.302.ai/gpt/imgs/5b36b96aaa052387fb3ccec2a063fe1e.png&weburl=https://302.ai/&redirecturl=https://dash.302.ai/dashboard/overview&lang=zh-CN"
        val htmlTest = "https://test-dash.gpt302.com/sso/login?app=302.ai&name=302AI&icon=https://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/3ee5f13fb09879ecb5185e440cef6eb9.png~tplv-uwbnlip3yd-webp.webp&weburl=https://baidu.com&redirecturl=https://test-dash.gpt302.com/dashboard/overview&lang=zh-CN"
        mWebView?.loadUrl(url)


        dialog.findViewById<Button>(R.id.cancelLoginButton).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            dialog.dismiss()
        }

        dialog.show()
    }

    class WebAppInterface(private val context: Context) {
        @JavascriptInterface
        fun receiveMessage(info: WebBackInfo) {
            Log.e("ceshi", "0接收到来自 auth 的数据: ${info.data.api_key}")
            try {
                //val jsonData = JSONObject(message)

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    data class WebBackInfo(
        val data:InfoBack
    )
    data class InfoBack(
        val api_key:String
    )

    inner class JavaScriptInterface {
        @JavascriptInterface
        fun onReceiveApiKey(apiKey: String) {
            // 在主线程处理拿到的 api_key
            runOnUiThread {
                println("通过 JS 注入获取到 api_key: $apiKey")
                // 后续业务逻辑...
            }
        }
    }

    private fun extractApiKey(url: String): String? {
        // 查找问号（参数起始位置）
        val queryStart = url.indexOf('?')
        if (queryStart == -1) {
            return null // 没有参数部分
        }

        // 提取所有参数（问号后面的部分）
        val queryParams = url.substring(queryStart + 1)

        // 分割参数（按 & 符号）
        val params = queryParams.split("&")

        // 遍历参数，找到 apikey
        for (param in params) {
            val keyValue = param.split("=", limit = 2) // 限制分割为2部分（防止值中包含=）
            if (keyValue.size == 2 && keyValue[0] == "apikey") {
                return keyValue[1] // 返回apikey的值
            }
        }

        return null // 未找到apikey参数
    }


}