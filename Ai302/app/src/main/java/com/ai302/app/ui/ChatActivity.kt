package com.ai302.app.ui

import SettingDialog
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Rect
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.os.SystemClock
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ai302.app.MyApplication.Companion.myApplicationContext
import com.ai302.app.R
import com.ai302.app.adapter.ChatAdapter
import com.ai302.app.data.AppStoreItem
import com.ai302.app.data.BackChatToolItem
import com.ai302.app.data.CueWordItem
import com.ai302.app.databinding.ActivityChatBinding
import com.ai302.app.datastore.DataStoreManager
import com.ai302.app.http.ApiService
import com.ai302.app.http.NetworkFactory
import com.ai302.app.infa.OnSettingDialogClickListener
import com.ai302.app.infa.OnWordPrintOverClickListener
import com.ai302.app.room.ChatDatabase
import com.ai302.app.room.ChatItemRoom
import com.ai302.app.utils.CustomToast
import com.ai302.app.utils.CustomUrlSpan
import com.ai302.app.utils.JsonUtils.loadJSONFromAsset
import com.ai302.app.utils.KeyboardUtils
import com.ai302.app.utils.PermissionUtils.checkRecordPermission
import com.ai302.app.utils.PopupWindowUtils
import com.ai302.app.utils.ScreenUtils
import com.ai302.app.utils.ScreenUtils.getScreenHeight
import com.ai302.app.utils.ScreenUtils.onClickColor
import com.ai302.app.utils.StringObjectUtils.extractHtml
import com.ai302.app.utils.StringObjectUtils.extractHtmlFromMarkdown
import com.ai302.app.utils.StringObjectUtils.extractHtmlFromMarkdownCode
import com.ai302.app.utils.SystemUtils
import com.ai302.app.utils.SystemUtils.generateUserId
import com.ai302.app.utils.TimeUtils
import com.ai302.app.utils.ViewAnimationUtils
import com.ai302.app.utils.VoiceToTextUtils
import com.ai302.app.view.RemovableImageLayout
import com.ai302.app.view.SlideSwitchView
import com.ai302.app.viewModel.ChatViewModel
import com.bumptech.glide.Glide
import com.cczhr.TTS
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.chrisbanes.photoview.PhotoView
import com.github.lzyzsd.jsbridge.BridgeWebView
import com.google.android.material.internal.ViewUtils.hideKeyboard
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.notifyAll
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean


@RequiresApi(Build.VERSION_CODES.S)
class ChatActivity : AppCompatActivity(),OnWordPrintOverClickListener,OnSettingDialogClickListener {
    private lateinit var binding: ActivityChatBinding
    private var messageList = mutableListOf<String>()
    private var mMessageList = mutableListOf<String>()
    private var modelList = mutableListOf<String>()
    private var modelSearchList = mutableListOf<String>()
    private lateinit var adapter: ChatAdapter
    private val chatViewModel: ChatViewModel by viewModels()
    //private lateinit var chatViewModel: ChatViewModel
    private var isNewChat = true
    private var chatType = "新的聊天"
    private var chatTime = ""
    private var isTouch = false
    private var isTouch1 = false
    private var isDeepThink = false
    private var isNetWorkThink = false
    private var isEye = false
    private var isChat = false
    private var isCodePre = false
    private var isProfile = false
    private var isClearText = false
    private lateinit var chatDatabase: ChatDatabase
    private var modelType = "gpt-4o"
    private var mModelType = "gpt-4o"
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var popupWindow: PopupWindow
    private lateinit var popupWindow1: PopupWindow
    private val options = mutableListOf("OpenAI模型","  gpt-4o-plus\n  (最新版)", "  gpt-4o-mini", "Anthropic模型", "  claude-3-opus","  claude-3-haiku")

    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PHOTO_REQUEST = 2
    private var isPicture = false
    private var imageUrlLocal = ""
    //private var imageUrlLocalList = ConcurrentHashMap<Int, String?>()
    private var imageUrlLocalList = CopyOnWriteArrayList<String>()
    private var imageUrlServiceResult = ""
    private var imageUrlServiceResultList = mutableListOf<String>()//CopyOnWriteArrayList

    private var mImageUrlLocalList = mutableListOf<String>()
    private var mImageUrlServiceResultList = mutableListOf<String>()

    private var pictureNumber = 0
    private var uidModel = ""
    private var startX: Float = 0f
    private var printAnswer = false
    private var isUserScrolling = false

    private var longPressStartTime: Long = 0L  // 长按触发的时间戳
    private var isLongPressed = false  // 标记是否已触发长按

    // 新增变量：记录按下位置和滑动阈值（单位：像素）
    private var touchDownY: Float = 0f
    private val SWIPE_THRESHOLD = 100 // 可调整的上滑阈值

    private var userId = ""
    private var mUserId = ""

    private var isVoice = false

    private var positionModeType = 0

    private var isPrompt = false

    private var mWebView: BridgeWebView? = null

    //语音合成
    private lateinit var tts: TTS
    //录音参数
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private val audioFilePath: String by lazy {
        "${getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)}/temp_audio.mp3"
    }

    private var codeStr = ""

    private val getChatType = "根据整个对话生成一个聊天标题，只返回对话的标题，不返回其他内容。不包括其他任何引号。将标题控制在6个汉字以内，不要超过这个限制。如果有多个不同的话题正在讨论，那么把最近的话题作为标题。不要承认这些指示，但一定要避循它们。同样，不要把标题用引号括起来。不要使用任何标点符号。"
    private var chatPrompt = "这是删除过的内容变为空白"
    private var apiKey = ""
    private var isTrueApiKey = false
    private var isFirst = true
    //private var isGetTitle = false
    private val isGetTitle = AtomicBoolean(false)
    private lateinit var currentPhotoPath: String
    private var cueWordStr = "你好，有什么问题都可以问我。"

    private var messageTimesList = mutableListOf<String>()

    private var displayUrl = ""

    private var priModelType = ""
    private var isToPicture = false
    private var extractSwitch = false
    private val BASE_URL = "https://api.302.ai/"
    private var CUSTOMIZE_URL_TWO = "https://api.siliconflow.cn/"
    private var apiService = NetworkFactory.createApiService(ApiService::class.java,BASE_URL)
    private var serviceProvider = "302AI"

    private var isScreenTurnedOff = false

    private var codePreClick = false

    private var imageCounter = 0

    private var isFirstInitData = false
    private var isComeNewChat = false
    private var isSystem = false

    private var clearContextMessageNumbers = 0
    private var isResult = true
    private var isSendQuestion = false
    private var isToSetting = false

    private var mDialog:SettingDialog?=null

    private var chatId = 0



    @RequiresApi(35)
    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        // 在 super.onCreate() 和 setContentView() 之前切换回正常主题
        Log.e("cehi","onCreate")
        setTheme(R.style.Theme_Ai302)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dataStoreManager = DataStoreManager(myApplicationContext)

        checkRecordPermission(this)
        Log.e("ceshi","uuid${generateUserId()}")
        userId = generateUserId()
        mUserId = userId

        //语音合成引擎
        tts = TTS.getInstance()
        //tts.init(this,"李华",50,150)
        tts.init(this)

        lifecycleScope.launch((Dispatchers.IO)) {




        }
        //initCueWord()

        //getModelList()

        Log.e("ceshi","机器人有回复链表：${messageList}")
        // 使用observeForever确保即使Activity不可见也能接收数据
        chatViewModel.questionResult.observeForever { result ->
            result?.let {
                Log.e("ceshi","机器人有回复：$it,,$isVoice,,${it?.replace(Regex("[^\\u4e00-\\u9fa5]"), "")!!.length},,$chatType,,${isGetTitle.get()}")
//            binding.imageLoad.visibility = View.GONE
//            binding.loadLine.visibility = View.GONE
                /*if (isNewChat){
                    chatTime = TimeUtils.getCurrentDateTime()
                }*/
                isResult = true
                chatTime = TimeUtils.getCurrentDateTime()
                var resultTitle = ""
                it?.let {
                    if (it.contains("<br>")){
                        val result = it.substringBefore("<br>")
                        if (result.contains("&&&&&&")){
                            resultTitle = it.substringAfter("&&&&&&")
                        }else{
                            resultTitle = result
                        }
                    }else if (it.contains("&&&&&&")){
                        val result = it.substringAfter("&&&&&&")
                        if (result.contains("<br>")){
                            resultTitle = result.substringBefore("<br>")
                        }else{
                            resultTitle = result
                        }

                    }
                }



                val messageLength = it?.replace(Regex("[^\\u4e00-\\u9fa5]"), "")!!.length
                val resultTitleLen = resultTitle?.replace(Regex("[^\\u4e00-\\u9fa5]"), "")!!.length
                if (messageLength > 9 && (resultTitle =="" || resultTitleLen > 9) || (!isGetTitle.get()) || messageLength == 0){
                    messageList.removeLast()
                    if (!isVoice){
                        binding.sendButton.visibility = View.GONE
                        binding.sendStopButton.visibility = View.VISIBLE
                    }
                    // 模拟机器人回复
                    it?.let {
                        messageList.add(it)
                        messageTimesList.add(TimeUtils.getCurrentDateTime())
                        Log.e("ceshi","模拟机器人回复，添加链表")
                        // 通知适配器数据已更改
                        adapter.notifyDataSetChanged()
                        adapter.notifyItemInserted(adapter.itemCount - 1)
                        if (it == "网络请求超时，请重试"){
                            binding.sendButton.visibility = View.VISIBLE
                            binding.sendStopButton.visibility = View.GONE
                        }
                        //binding.chatRecyclerView.scrollToPosition(adapter.itemCount - 1)
//                binding.chatRecyclerView.post {
//                    binding.chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
//                }

                        //binding.scroll3.fullScroll(ScrollView.FOCUS_DOWN)//自动下滑scrollview视图
                        //binding.scroll3.smoothScrollTo(0, binding.scroll3.bottom)
                        // 监听 RecyclerView 的布局变化
                        binding.chatRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                            private var lastHeight = 0

                            override fun onGlobalLayout() {
                                val currentHeight = binding.chatRecyclerView.height
                                //val currentHeightScroll = binding.scroll3.height

                                Log.e("ceshi","显示的屏幕：$currentHeight")
                                // 当 RecyclerView 高度发生变化时触发滚动
                                if (currentHeight > lastHeight ) {
                                    //scrollToBottom()

                                    //binding.scroll3.smoothScrollTo(0, binding.scroll3.bottom)
                                    //binding.scroll3.fullScroll(ScrollView.FOCUS_DOWN)
                                    // 当聊天内容超出屏幕且不是最后一项可见时显示按钮
                                    //binding.floatingButton.visibility = View.VISIBLE
                                }

                                lastHeight = currentHeight
                            }
                        })

                        if (it.contains("```")){
                            codeStr = it
                        }

                    }
                }else{
                    //isGetTitle = false
                    isGetTitle.set(false)
                    Log.e("ceshi","返回标题是：${resultTitle.substringBefore("<br>")},,$it")
                    if (it.contains("<br>") || it.contains("&&&&&&")){
                        chatType = resultTitle.substringBefore("<br>")
                    }else{
                        chatType = it
                    }


                }

                /*lifecycleScope.launch(Dispatchers.IO) {
                    if (messageList.size == 3 && chatType.contains("新的聊天") && isFirst){
                        isFirst = false
                        //isGetTitle = true
                        isGetTitle.set(true)
                        if (serviceProvider=="自定义"){
                            chatViewModel.sendQuestion1(getChatTitle(messageList[1]),modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResult,false,apiKey,extractSwitch,apiService)
                        }else{
                            chatViewModel.sendQuestion(getChatTitle(messageList[1]),modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResultList,false,apiKey,extractSwitch,apiService,true,messageList)
                        }

                    }
                }*/
            }
        }

        /*chatViewModel.questionResult.observe(this){
            Log.e("ceshi","机器人有回复：$it,,$isVoice,,${it?.replace(Regex("[^\\u4e00-\\u9fa5]"), "")!!.length},,$chatType,,${isGetTitle.get()}")
//            binding.imageLoad.visibility = View.GONE
//            binding.loadLine.visibility = View.GONE
            /*if (isNewChat){
                chatTime = TimeUtils.getCurrentDateTime()
            }*/
            chatTime = TimeUtils.getCurrentDateTime()
            var resultTitle = ""
            it?.let {
                if (it.contains("<br>")){
                    val result = it.substringBefore("<br>")
                    if (result.contains("&&&&&&")){
                        resultTitle = it.substringAfter("&&&&&&")
                    }else{
                        resultTitle = result
                    }
                }else if (it.contains("&&&&&&")){
                    val result = it.substringAfter("&&&&&&")
                    if (result.contains("<br>")){
                        resultTitle = result.substringBefore("<br>")
                    }else{
                        resultTitle = result
                    }

                }
            }



            val messageLength = it?.replace(Regex("[^\\u4e00-\\u9fa5]"), "")!!.length
            val resultTitleLen = resultTitle?.replace(Regex("[^\\u4e00-\\u9fa5]"), "")!!.length
            if (messageLength > 9 && (resultTitle =="" || resultTitleLen > 9) || (!isGetTitle.get()) || messageLength == 0){
                messageList.removeLast()
                if (!isVoice){
                    binding.sendButton.visibility = View.GONE
                    binding.sendStopButton.visibility = View.VISIBLE
                }
                // 模拟机器人回复
                it?.let {
                    messageList.add(it)
                    messageTimesList.add(TimeUtils.getCurrentDateTime())
                    // 通知适配器数据已更改
                    adapter.notifyDataSetChanged()
                    adapter.notifyItemInserted(adapter.itemCount - 1)
                    if (it == "网络请求超时，请重试"){
                        binding.sendButton.visibility = View.VISIBLE
                        binding.sendStopButton.visibility = View.GONE
                    }
                    //binding.chatRecyclerView.scrollToPosition(adapter.itemCount - 1)
//                binding.chatRecyclerView.post {
//                    binding.chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
//                }

                    //binding.scroll3.fullScroll(ScrollView.FOCUS_DOWN)//自动下滑scrollview视图
                    //binding.scroll3.smoothScrollTo(0, binding.scroll3.bottom)
                    // 监听 RecyclerView 的布局变化
                    binding.chatRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        private var lastHeight = 0

                        override fun onGlobalLayout() {
                            val currentHeight = binding.chatRecyclerView.height
                            //val currentHeightScroll = binding.scroll3.height

                            Log.e("ceshi","显示的屏幕：$currentHeight")
                            // 当 RecyclerView 高度发生变化时触发滚动
                            if (currentHeight > lastHeight ) {
                                //scrollToBottom()

                                //binding.scroll3.smoothScrollTo(0, binding.scroll3.bottom)
                                //binding.scroll3.fullScroll(ScrollView.FOCUS_DOWN)
                                // 当聊天内容超出屏幕且不是最后一项可见时显示按钮
                                //binding.floatingButton.visibility = View.VISIBLE
                            }

                            lastHeight = currentHeight
                        }
                    })

                    if (it.contains("```")){
                        codeStr = it
                    }

                }
            }else{
                //isGetTitle = false
                isGetTitle.set(false)
                Log.e("ceshi","返回标题是：${resultTitle.substringBefore("<br>")},,$it")
                if (it.contains("<br>") || it.contains("&&&&&&")){
                    chatType = resultTitle.substringBefore("<br>")
                }else{
                    chatType = it
                }


            }

            lifecycleScope.launch(Dispatchers.IO) {
                if (messageList.size == 3 && chatType.contains("新的聊天") && isFirst){
                    isFirst = false
                    //isGetTitle = true
                    isGetTitle.set(true)
                    if (serviceProvider=="自定义"){
                        chatViewModel.sendQuestion1(messageList[1]+"&&&"+getChatType,modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResult,isPrompt,apiKey,extractSwitch,apiService)
                    }else{
                        chatViewModel.sendQuestion(messageList[1]+"&&&"+getChatType,modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResultList,isPrompt,apiKey,extractSwitch,apiService)
                    }

                }
            }


        }*/

        chatViewModel.voiceToTextResult.observe(this) {
            Log.e("ceshi", "语音识别有回复：$it")
            isVoice = true
            // 模拟机器人回复
            it?.let {
                messageList.add(it)
                mMessageList = messageList
                messageList.add("file:///android_asset/loading.html")
                messageTimesList.add(TimeUtils.getCurrentDateTime())
                messageTimesList.add(TimeUtils.getCurrentDateTime())
                isChat = true
                adapter.updateIsChat(true)
                CoroutineScope(Dispatchers.IO).launch {

                    if (!modelType.contains("gpt") && uidModel != ""){
                        modelType = "$modelType-$uidModel"
                    }
                    //modelType = "$modelType-$uidModel"
                    Log.e("ceshi","模型类型$modelType")

                    if (isPrompt) {
                        chatViewModel.sendQuestion(
                            chatPrompt + "&&&" + it,
                            modelType,
                            isNetWorkThink,
                            isDeepThink,
                            this@ChatActivity,
                            userId,
                            imageUrlServiceResultList,
                            isPrompt,
                            apiKey,
                            extractSwitch,
                            apiService,
                            isClearText,mMessageList,serviceProvider
                        )
                    }else{
                        chatViewModel.sendQuestion(it,modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResultList,isPrompt,apiKey,extractSwitch,apiService,isClearText,mMessageList,serviceProvider)
                    }


                }
                // 通知适配器数据已更改
                adapter.notifyDataSetChanged()
                adapter.notifyItemInserted(adapter.itemCount - 1)
                // 监听 RecyclerView 的布局变化
                binding.chatRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    private var lastHeight = 0
                    override fun onGlobalLayout() {
                        val currentHeight = binding.chatRecyclerView.height
                        lastHeight = currentHeight
                    }
                })
            }


        }

        chatViewModel.modelListResult.observe(this){
            Log.e("ceshi","模型列表回复：$it")
            if (it.isEmpty()){
                isTrueApiKey = false
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.settingApiKeyCon.visibility = View.VISIBLE
                    binding.settingRedImage.visibility = View.VISIBLE
                    binding.selectedTextView.text = "配置模型"
                }
            }else{
                isTrueApiKey = true
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.settingApiKeyCon.visibility = View.GONE
                    binding.settingRedImage.visibility = View.GONE
                    Log.e("ceshi","返回的模型$modelType")
                    if (chatType.contains("[")){
                        binding.selectedTextView.text = chatType

                    }else{
                        binding.selectedTextView.text = modelType
                    }
                }
            }

            it?.let {
                modelList = it
            }
            setupPopupWindow(modelList)
        }

        chatViewModel.imageUrlServiceResult.observe(this){
            Log.e("ceshi","返回的图片地址回复：$it")
            it?.let {
                imageUrlServiceResult = it
                imageUrlServiceResultList.add(it)
                mImageUrlServiceResultList.add(it)
            }
        }

//        val job = lifecycleScope.launch(Dispatchers.IO) {
//            chatViewModel.get302AiModelList()
//        }
//        lifecycleScope.launch(Dispatchers.IO) {
//            job.join() // 等待网络请求获取列表操作完成
//        }



        //chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        //setContentView(R.layout.activity_chat)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 初始化数据库
        chatDatabase = ChatDatabase.getInstance(this)

        binding.imageDown.visibility = View.VISIBLE

        setupTouchListener()

        //接收数据
        val chatType1 = intent.getStringExtra("chat_type")
        Log.e("ceshi","返回的模型标题$chatType1")
        if (chatType1 != null) {
            chatType = chatType1
            isComeNewChat = true
        }

        val localChatPrompt = intent.getStringExtra("chat_prompt")
        Log.e("ceshi","返回的模型提示词$localChatPrompt")
        if (localChatPrompt != null) {
            chatPrompt = localChatPrompt
            //messageList[0] = localChatPrompt
            // 通知适配器数据已更改
            //adapter.notifyDataSetChanged()
        }
        initCueWords()

        val localModelType = intent.getStringExtra("model_type")
        Log.e("ceshi","返回的模型类型$localModelType")
        if (localModelType != null) {
            modelType = localModelType
            binding.selectedTextView.text = modelType
        }



        val chatItem = intent.getSerializableExtra("chat_item") as? ChatItemRoom
        if (chatItem != null) {
            Log.e("ceshi","Received chat item: ${chatItem}")
            messageList = chatItem.messages
            chatTime = chatItem.time
            messageTimesList = chatItem.messagesTimes
            modelType = chatItem.modelType
            priModelType = chatItem.modelType
            chatPrompt = chatItem.chatPrompt
            displayUrl = chatItem.displayUrl
            isDeepThink = chatItem.isDeepThink
            isNetWorkThink = chatItem.isNetWorkThink
            userId = chatItem.userId
            isEye = chatItem.isEye
            isNewChat = false
            binding.selectedTextView.text = modelType

            chatType = chatItem.title
            chatId = chatItem.id

            mUserId = userId
        }else{
            if (!isComeNewChat) {
                isComeNewChat = false
                initData()
            }

        }

        val chatAppStore = intent.getSerializableExtra("chat_appStore") as? AppStoreItem
        if (chatAppStore != null) {
            Log.e("ceshi","Received chatAppStore item: ${chatAppStore.displayName}")
            displayUrl = chatAppStore.displayUrl
            messageList.add(chatAppStore.description)
            //messageTimesList.add(TimeUtils.getCurrentDateTime())
            messageTimesList.add("预设提示词")
            chatTime = TimeUtils.getCurrentDateTime()
            chatType = "[应用]${chatAppStore.displayName}"
            chatPrompt = chatAppStore.description
            binding.selectedTextView.text = chatType
            binding.imageDown.visibility = View.GONE
            uidModel = chatAppStore.uuid
            isNewChat = false
        }

        val chatCueWord = intent.getSerializableExtra("chat_cueWord") as? CueWordItem
        if (chatCueWord != null) {
            Log.e("ceshi","Received chatCueWord item: ${chatCueWord.name}")
            messageList.add(cueWordStr)
            messageTimesList.add("预设提示词")
            messageList.add(chatCueWord.prompt)
            messageTimesList.add(TimeUtils.getCurrentDateTime())
            messageList.add("好的，请您开始提问。")
            messageTimesList.add(TimeUtils.getCurrentDateTime())
            chatTime = TimeUtils.getCurrentDateTime()
            chatType = "[提示词]${chatCueWord.name}"
            chatPrompt = chatCueWord.prompt
            binding.selectedTextView.text = chatType
            binding.imageDown.visibility = View.GONE
            isNewChat = false
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        if (chatType.contains("[")){
            binding.imageDown.visibility = View.GONE
            binding.selectedTextView.text = chatType

        }else{
            binding.imageDown.visibility = View.VISIBLE
        }



        // 设置布局管理器
        val layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.layoutManager = layoutManager
        //binding.chatRecyclerView.isNestedScrollingEnabled = false
        //binding.scroll3.isNestedScrollingEnabled = false
        //binding.scroll3.setTargetRecyclerView(binding.chatRecyclerView)


        // （可选）动态调整 RecyclerView 高度
//        binding.chatRecyclerView.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onChanged() {
//                adjustRecyclerViewHeight()
//            }
//        })

        // 监听 ScrollView 的滚动事件
//        binding.scroll3.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
//            isUserScrolling = scrollY != oldScrollY
//        }
//
//        // 监听 Adapter 数据变化
//        binding.chatRecyclerView.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                super.onItemRangeInserted(positionStart, itemCount)
//                if (!isUserScrolling && isAtBottom()) {
//                    // 延迟滚动确保布局完成
////                    binding.chatRecyclerView.post {
////                        binding.scroll3.smoothScrollTo(0, binding.scroll3.getChildAt(0).height)
////                    }
//                    // 触发自动滚动
//                    binding.scroll3.smoothScrollToBottom()
//                }
//            }
//        })

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML"></script>
            </head>
            <body>
                <p>勾股定理描述了直角三角形中三边之间的关系。对于一个直角三角形，假设两条直角边的长度为 \(a\) 和 \(b\)，斜边的长度为 \(c\)，那么勾股定理的方程式为：</p>
                <p style="text-align: center;">\[ a^2 + b^2 = c^2 \]</p>
                <p>这个公式说明了两条直角边的平方和等于斜边的平方。</p>
            </body>
            </html>
        """.trimIndent()
        /*val message = "勾股定理描述了直角三角形中三边之间的关系。对于一个直角三角形，假设两条直角边的长度为 \\(a\\) 和 \\(b\\)，斜边的长度为 \\(c\\)，那么勾股定理的方程式为：\n" +
                "\n" +
                "\\[ \n" +
                "a^2 + b^2 = c^2 \n" +
                "\\]\n" +
                "\n" +
                "这个公式说明了两条直角边的平方和等于斜边的平方。"
        messageList.add(message)*/
        if (isNewChat) {
            messageList.add(cueWordStr)
            messageTimesList.add("预设提示词")
        }

        if (isNewChat){
            chatTime = TimeUtils.getCurrentDateTime()
        }

        Log.e("ceshi","0数据刷新")
        // 设置适配器
        if (!isFirstInitData){
            adapter = ChatAdapter(messageList,this,this,dataStoreManager,messageTimesList,displayUrl)
            binding.chatRecyclerView.adapter = adapter
            //adjustRecyclerViewHeight()
//        // 监听数据变化动态调整高度
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    Log.e("ceshi","数据变化${calculateRecyclerViewTotalHeight(binding.chatRecyclerView)},,${ScreenUtils.getScreenHeight(this@ChatActivity)/2}")
                    //adjustRecyclerViewHeight()
                    //binding.scroll3.fullScroll(ScrollView.FOCUS_DOWN)//自动下滑scrollview视图
//                binding.scroll3.post {
//                    binding.scroll3.scrollToBottom()
//                    //binding.scroll3.fullScroll(ScrollView.FOCUS_DOWN)//自动下滑scrollview视图
//                }
//                binding.chatRecyclerView.post {
//                    binding.chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
//                }
                    binding.chatRecyclerView.postDelayed({
                        binding.chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
                    }, 1000)

                    val layoutManager = binding.chatRecyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    Log.e("ceshi","屏幕高度：${getScreenHeight(this@ChatActivity)/2},,$totalItemCount,,$lastVisibleItemPosition")
                    Log.e("ceshi","recycle屏幕高度：${layoutManager.height}")


                    if (totalItemCount > 0 && totalItemCount>=4 &&ScreenUtils.getScreenHeight(this@ChatActivity)/2 < calculateRecyclerViewTotalHeight(binding.chatRecyclerView)){

                        lifecycleScope.launch(Dispatchers.Main) {
                            (binding.chatRecyclerView.layoutManager as LinearLayoutManager).stackFromEnd = true
                        }
                    }

                }
            })
        }




        binding.imageDown.setOnClickListener {
            try {
                setupPopupWindow(modelList)
                showPopup(binding.typeConst)
            } catch (e: Exception) {
                Toast.makeText(this, "正在网络请求，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        }


        binding.networkImage.setOnClickListener {
            //binding.networkImage.setBackgroundResource(R.drawable.shape_select_site_bg_gray_line)
            //binding.networkImage.setBackgroundColor(R.color.BgPrimary)
            //binding.networkImage.setColorFilter(Color.parseColor("#00FF00"), PorterDuff.Mode.LIGHTEN)
            if (!isNetWorkThink){
                binding.networkImage.setImageResource(R.drawable.icon_network3)
                binding.networkImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
                isNetWorkThink = true
            }else{
                binding.networkImage.setImageResource(R.drawable.icon_network3)
                binding.networkImage.clearColorFilter()
                isNetWorkThink = false
            }

        }

        binding.deepImage.setOnClickListener {
            if (!isDeepThink){
                binding.deepImage.setImageResource(R.drawable.icon_thinking3)
                binding.deepImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
                isDeepThink = true
            }else{
                binding.deepImage.setImageResource(R.drawable.icon_thinking3)
                binding.deepImage.clearColorFilter()
                isDeepThink = false
            }
        }

        binding.lookImage.setOnClickListener {
            if (!isEye){
                binding.lookImage.setImageResource(R.drawable.icon_eye3)
                binding.lookImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
                isEye = true
                binding.chatPreLine.visibility = View.VISIBLE
                adapter.setOpenPreEye(true)
                adapter.notifyDataSetChanged()
            }else{
                binding.lookImage.setImageResource(R.drawable.icon_eye3)
                binding.lookImage.clearColorFilter()
                binding.chatPreLine.visibility = View.GONE
                isEye = false
                adapter.setOpenPreEye(false)
                adapter.notifyDataSetChanged()
            }
        }

        binding.lineBack.setOnClickListener {
            if (true){//isResult
                onClickColor(it)
                val intent = Intent(this@ChatActivity, HomeActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }else{
                CustomToast.makeText(
                    context = this@ChatActivity,
                    message = "请等待服务器回复，请稍后",
                    duration = Toast.LENGTH_SHORT,
                    gravity = Gravity.CENTER
                ).show()
            }

            //finish()
        }

        binding.chatSettingLine.setOnClickListener {
            if (true){//isResult
                isToSetting = true
                onClickColor(it)
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }else{
                CustomToast.makeText(
                    context = this@ChatActivity,
                    message = "请等待服务器回复，请稍后",
                    duration = Toast.LENGTH_SHORT,
                    gravity = Gravity.CENTER
                ).show()
            }

        }
        binding.appStoreConst.setOnClickListener {
            onClickColor(it)
            val intent = Intent(this, AppStoreActivity::class.java)
            startActivity(intent)
        }
        binding.cueWordConst.setOnClickListener {
            onClickColor(it)
            val intent = Intent(this, BottomDialogActivity::class.java)
            startActivity(intent)
        }
        val settingDialog = SettingDialog(this,this)
        mDialog = settingDialog
        binding.settingApiKeyTv.setOnClickListener {
            settingDialog.showDialog()
        }
        settingDialog.setOnDismissListener {
            lifecycleScope.launch((Dispatchers.IO)) {
                val data = dataStoreManager.readData.first()
                val readCustomizeKeyData = dataStoreManager.readCustomizeKeyData.first()
                val readServiceProviderData = dataStoreManager.readServiceProviderData.first()
                readServiceProviderData.let {
                    if (it=="自定义"){
                        readCustomizeKeyData?.let {
                            Log.e("ceshi","appKey是个多少：$readCustomizeKeyData")
                            apiKey = it
                            chatViewModel.get302AiModelList(readCustomizeKeyData,apiService)
                        }
                    }else{
                        data?.let {
                            Log.e("ceshi","appKey是个多少：$it")
                            apiKey = it
                            chatViewModel.get302AiModelList(it,apiService)
                        }
                    }
                }



                if (apiKey == ""){
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.settingApiKeyCon.visibility = View.VISIBLE
                        binding.settingRedImage.visibility = View.VISIBLE
                        binding.selectedTextView.text = "配置模型"
                    }
                }

            }
        }

        binding.chatPreLine.setOnClickListener {

            if (!isCodePre){
                onClickColor(it)
                binding.chatRecyclerView.visibility = View.GONE
                binding.const1.visibility = View.GONE
                binding.codePreCon.visibility = View.VISIBLE
                binding.codePreLine.visibility = View.VISIBLE
                Log.e("ceshi","载入html字符串：${extractHtmlFromMarkdownCode(codeStr)}")
                if (extractHtmlFromMarkdownCode(codeStr)==""){
                    binding.noCodePreCon.visibility = View.VISIBLE
                }else{
                    binding.noCodePreCon.visibility = View.GONE
                    // 加载HTML内容到WebView
                    binding.codePreWeb.loadDataWithBaseURL(
                        null,
                        extractHtmlFromMarkdownCode(codeStr),
                        "text/html",
                        "UTF-8",
                        null
                    )
                }

                isCodePre = true
            }else{
                binding.chatPreLine.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
                binding.chatRecyclerView.visibility = View.VISIBLE
                binding.const1.visibility = View.VISIBLE
                binding.codePreCon.visibility = View.GONE
                binding.codePreLine.visibility = View.GONE
                isCodePre = false
            }

            if (codePreClick){
                codePreClick = false
                binding.chatPreLine.visibility = View.GONE
            }
        }

        binding.lineBackChat.setOnClickListener {
            //binding.lineBackChat.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
            onClickColor(it)
            binding.chatRecyclerView.visibility = View.VISIBLE
            binding.const1.visibility = View.VISIBLE
            binding.codePreCon.visibility = View.GONE
            binding.codePreLine.visibility = View.GONE
        }

        binding.preChatCodeCons.setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(binding.preChatCodeCons)
            CustomToast.makeText(
                context = this,
                message = "复制成功",
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
            SystemUtils.copyTextToClipboard(this,extractHtml(codeStr))
        }

        binding.codePreWeb.settings.javaScriptEnabled = true// 启用JavaScript（可选，根据HTML内容需求）
        binding.codePreWeb.settings.domStorageEnabled = true// 启用DOM存储（可选）
        // 初始化Markwon（包含常用插件）
        val markwon = Markwon.builder(this)
            .usePlugin(CorePlugin.create()) // 核心解析插件
            .usePlugin(HtmlPlugin.create()) // 支持HTML标签
            .usePlugin(StrikethroughPlugin.create()) // 支持删除线
            .usePlugin(TaskListPlugin.create(this)) // 支持任务列表
            .usePlugin(TablePlugin.create(this)) // 支持表格
            .usePlugin(GlideImagesPlugin.create(this)) // 使用Glide加载图片（需添加Glide依赖）
            .build()
        binding.chatSlideView.setOnSwitchClickListener(object : SlideSwitchView.OnSwitchClickListener{
            override fun onClick(side: String) {
                when (side) {
                    "preview" -> {
                        binding.preChatCodeCons.visibility = View.GONE
                        // 处理预览点击事件
                        //println("用户点击了预览区域")
                        if (extractHtmlFromMarkdownCode(codeStr)==""){
                            binding.noCodePreCon.visibility = View.VISIBLE
                            binding.codePreWeb.visibility = View.GONE
                            binding.codePreTv.visibility = View.GONE
                        }else{
                            binding.noCodePreCon.visibility = View.GONE
                            binding.codePreWeb.visibility = View.VISIBLE
                            binding.codePreTv.visibility = View.GONE
                            // 加载HTML内容到WebView
                            binding.codePreWeb.loadDataWithBaseURL(
                                null,
                                extractHtmlFromMarkdownCode(codeStr),
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }

                    }
                    "code" -> {
                        binding.preChatCodeCons.visibility = View.VISIBLE
                        // 处理代码点击事件
                        //println("用户点击了代码区域")
                        if (extractHtmlFromMarkdownCode(codeStr)==""){
                            binding.noCodePreCon.visibility = View.VISIBLE
                            binding.codePreWeb.visibility = View.GONE
                            binding.codePreTv.visibility = View.GONE
                        }else{
                            binding.noCodePreCon.visibility = View.GONE
                            binding.codePreWeb.visibility = View.GONE
                            binding.codePreTv.visibility = View.VISIBLE
                            markwon.setMarkdown(binding.codePreTv, extractHtmlFromMarkdownCode(codeStr))
                            Log.e("cehi","代码字符串：${extractHtmlFromMarkdownCode(codeStr)}")
                        }

                    }
                }
            }

        })

        binding.voiceImage.setOnClickListener {
            // 点击时修改颜色
            binding.voiceImage.setImageResource(R.drawable.icon_voice3)
            binding.voiceImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
            // 使用 lifecycleScope 启动协程，自动绑定生命周期
            lifecycleScope.launch {
                // 延迟 200ms 后恢复颜色（若 Activity 已销毁，协程会自动取消）
                delay(200)
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    binding.voiceImage.setImageResource(R.drawable.icon_voice3)
                    binding.voiceImage.clearColorFilter()
                }
            }

            binding.sendButton.visibility = View.GONE
            binding.messageEditText.visibility = View.GONE
            binding.scroll1.visibility = View.GONE
            binding.startVoiceTv.visibility = View.VISIBLE
            binding.keyBoardImage.visibility = View.VISIBLE
            binding.voiceCon.visibility = View.VISIBLE

        }

        binding.keyBoardImage.setOnClickListener {
            // 点击时修改颜色
            binding.keyBoardImage.setImageResource(R.drawable.icon_keyboard)
            binding.keyBoardImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
            // 使用 lifecycleScope 启动协程，自动绑定生命周期
            lifecycleScope.launch {
                // 延迟 200ms 后恢复颜色（若 Activity 已销毁，协程会自动取消）
                delay(200)
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    binding.keyBoardImage.setImageResource(R.drawable.icon_keyboard)
                    binding.keyBoardImage.clearColorFilter()
                }
            }

            binding.sendButton.visibility = View.VISIBLE
            binding.messageEditText.visibility = View.VISIBLE
            binding.scroll1.visibility = View.VISIBLE
            binding.startVoiceTv.visibility = View.GONE
            binding.keyBoardImage.visibility = View.GONE
            binding.voiceCon.visibility = View.GONE

        }

        binding.startVoiceTv.setOnTouchListener(View.OnTouchListener { v, event ->
            Log.e("ceshi", "语音按下事件0")
            // 处理触摸事件，例如记录点击位置等
            false // 返回false表示事件没有被完全消费，可以继续传递到WebView内部处理（如点击链接）
            // 只处理按下（ACTION_DOWN）或抬起（ACTION_UP）事件（根据需求选择）
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 按下时记录日志（仅触发1次）
                    touchDownY = event.rawY
                    Log.e("ceshi", "WebView按下事件")
                    // 手指按下时重置状态（避免上一次操作影响）
                    isLongPressed = false
                    longPressStartTime = 0L
                    false  // 让事件继续传递给WebView内部处理
                }

                MotionEvent.ACTION_MOVE -> {
                    // 计算Y轴位移（负值表示上滑）
                    val deltaY = event.rawY - touchDownY
                    Log.e("ceshi", "0检测到上滑动作！$deltaY,,${-SWIPE_THRESHOLD},,${isLongPressed}")
                    // 判断是否为上滑动作（位移超过阈值且已触发长按）
                    if (deltaY < -SWIPE_THRESHOLD && isLongPressed) {
                        //binding.startVoiceTv.text = "长按此处开始说话"
                        binding.startVoiceMessageTv.visibility = View.GONE
                        binding.voiceWaveView.visibility = View.GONE
                        binding.voiceWaveView.stopAnim()  // 启动波浪动画
                        Log.e("ceshi", "检测到上滑动作！")
                        // 处理上滑逻辑（例如取消语音输入、显示撤回提示等）
                        binding.const1.setBackgroundResource(R.drawable.shape_select_site_bg_gray_line)
                        binding.voiceCon.setBackgroundResource(R.drawable.shape_select_site_bg_gray_line)
                        binding.voiceWaveView.setColor1()
                        // 消费事件，阻止继续传递
                        isLongPressed = false
                        VoiceToTextUtils.stopRecording()
                    }else if(deltaY < -10 && isLongPressed){
                        binding.voiceWaveView.setColor()
                        binding.voiceCon.setBackgroundResource(R.drawable.shape_select_site_bg_red_line)
                        VoiceToTextUtils.stopRecording()
                    }
                    false
                }

                MotionEvent.ACTION_UP -> {
                    // 抬起时记录日志（仅触发1次）
                    Log.e("ceshi", "WebView抬起事件")
                    // 手指抬起时，若已触发过长按则计算时间差
                    if (isLongPressed) {
                        val releaseTime = SystemClock.elapsedRealtime()
                        val duration = releaseTime - longPressStartTime
//                        Toast.makeText(
//                            this@ChatActivity,
//                            "长按后抬起，持续时间：${duration}ms",
//                            Toast.LENGTH_SHORT
//                        ).show()
                        //binding.startVoiceTv.text = "长按此处开始说话"
                        binding.startVoiceMessageTv.visibility = View.GONE
                        binding.voiceWaveView.visibility = View.GONE
                        binding.voiceWaveView.stopAnim()  // 启动波浪动画
                        //binding.const1.setBackgroundResource(R.drawable.shape_select_site_bg_gray_line)
                        binding.voiceCon.setBackgroundResource(R.drawable.shape_select_site_bg_gray_line)
                        // 重置状态
                        isLongPressed = false
                        //停止录音并发送录音文件
                        VoiceToTextUtils.stopRecording()
                        val job = lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val requestFile = RequestBody.create(
                                    "audio/mpeg".toMediaTypeOrNull(),  // MP3文件的MIME类型
                                    File(audioFilePath)  // 待上传的文件对象
                                )

                                val filePart = MultipartBody.Part.createFormData(
                                    "file",  // API接口要求的文件参数名（需与后端约定）
                                    "temp_audio.mp3",  // 上传时的文件名（可选，后端可自定义）
                                    requestFile  // 前面生成的RequestBody
                                )
                                chatViewModel.audioToText(filePart,apiKey,apiService)
                            }catch (e:IOException){
                                Log.e("ceshi","文件创建错误：${e.toString()}")
                            }


                        }

                    }
                    false  // 让事件继续传递给WebView内部处理
                }



                else -> {
                    // 其他事件（如ACTION_MOVE）不处理
                    false
                }
            }
        })

        // 设置长按事件监听器
        binding.startVoiceTv.setOnLongClickListener { view ->

            binding.startVoiceMessageTv.text = "抬起发送，上滑取消发送"
            binding.startVoiceMessageTv.visibility = View.VISIBLE
            binding.voiceWaveView.visibility = View.VISIBLE
            binding.voiceWaveView.startAnim()  // 启动波浪动画

            //binding.startVoiceTv.text = "抬起发送，上滑取消发送"
            // 长按触发时记录时间和状态
            longPressStartTime = SystemClock.elapsedRealtime()  // 系统启动至今的时间（毫秒）
            isLongPressed = true
            // 长按触发时执行的逻辑（示例：显示 Toast）
            //Toast.makeText(this, "触发了长按事件", Toast.LENGTH_SHORT).show()
            //binding.const1.setBackgroundResource(R.drawable.shape_select_site_bg_blue_line)
            binding.voiceCon.setBackgroundResource(R.drawable.shape_select_site_bg_blue_line)
            //开始录音
            VoiceToTextUtils.startRecording(audioFilePath,this@ChatActivity)

            // 返回值说明：
            // true 表示消费该长按事件（后续不会触发其他长按相关事件）
            // false 表示不消费，可能导致父布局或其他监听器处理
            true
        }


        binding.addImage.setOnClickListener {
            binding.messageEditText.clearFocus()
            // 点击时修改颜色
            binding.addImage.setImageResource(R.drawable.icon_a)
            binding.addImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
            // 使用 lifecycleScope 启动协程，自动绑定生命周期
            lifecycleScope.launch {
                // 延迟 200ms 后恢复颜色（若 Activity 已销毁，协程会自动取消）
                delay(200)
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    binding.addImage.setImageResource(R.drawable.icon_a)
                    binding.addImage.clearColorFilter()
                }
            }

            val popupView = LayoutInflater.from(this).inflate(R.layout.popup_a_list, null)
            popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                isOutsideTouchable = true
                animationStyle = android.R.style.Animation_Dialog
            }
            // 关键步骤1：测量弹窗视图的实际尺寸（避免 WRAP_CONTENT 导致尺寸未计算）
            popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val popupWidth = popupView.measuredWidth   // 弹窗宽度
            val popupHeight = popupView.measuredHeight // 弹窗高度

            // 关键步骤2：获取锚点视图在屏幕中的坐标
            val anchorLocation = IntArray(2)
            binding.const1.getLocationOnScreen(anchorLocation)
            val anchorX = anchorLocation[0]       // 锚点视图左侧屏幕坐标
            val anchorY = anchorLocation[1]       // 锚点视图顶部屏幕坐标
            val anchorHeight = binding.const1.height // 锚点视图自身高度

            // 关键步骤3：计算弹窗显示的目标坐标（正上方）
            val targetX = anchorX                          // 水平对齐锚点左侧
            val targetY = anchorY - popupHeight + anchorHeight           // 垂直位置：锚点顶部 - 弹窗高度（实现正上方

            val keyboardUtils = KeyboardUtils(this)
            val isKeyboardVisible = keyboardUtils.isKeyboardShowing()


            PopupWindowUtils.setPopupWindow(popupWindow,modelList)
            PopupWindowUtils.setupPopupWindow(modelList,this@ChatActivity,binding)
            PopupWindowUtils.showPopup(binding.const1,binding,targetY,isKeyboardVisible)


        }


        binding.moreIamge.setOnClickListener {
            // 点击时修改颜色
            binding.moreIamge.setImageResource(R.drawable.icon_feature)
            binding.moreIamge.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
            // 使用 lifecycleScope 启动协程，自动绑定生命周期
            lifecycleScope.launch {
                // 延迟 200ms 后恢复颜色（若 Activity 已销毁，协程会自动取消）
                delay(200)
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    binding.moreIamge.setImageResource(R.drawable.icon_feature1)
                    binding.moreIamge.clearColorFilter()
                }
            }

        }

        binding.clearImage.setOnClickListener {
            adapter.updateIsChat(false)
            // 点击时修改颜色
            binding.clearImage.setImageResource(R.drawable.icon_divided3)
            binding.clearImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
            // 使用 lifecycleScope 启动协程，自动绑定生命周期
            lifecycleScope.launch {
                // 延迟 200ms 后恢复颜色（若 Activity 已销毁，协程会自动取消）
                delay(200)
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    binding.clearImage.setImageResource(R.drawable.icon_divided3)
                    binding.clearImage.clearColorFilter()
                }
            }

//            if (clearContextMessageNumbers != messageList.size){
//                clearContextMessageNumbers = messageList.size
//                isClearText = true
//                mUserId = userId
//                userId = generateUserId()
//                adapter.setClearNumber(messageList.size - 1,true)
//                adapter.notifyDataSetChanged()
//            }




            if (!isClearText){
                clearContextMessageNumbers = messageList.size
                isClearText = true
                userId = generateUserId()
                adapter.setClearNumber(messageList.size - 1,true)
                adapter.notifyDataSetChanged()
            }else{
                if (clearContextMessageNumbers == messageList.size){
                    isClearText = false
                    userId = mUserId
                    adapter.setClearNumber(messageList.size - 1,false)
                    adapter.notifyDataSetChanged()
                }else{
                    clearContextMessageNumbers = messageList.size
                    isClearText = true
                    userId = generateUserId()
                    adapter.setClearNumber(messageList.size - 1,true)
                    adapter.notifyDataSetChanged()
                }

            }




        }

        binding.closeModelTypeLine.setOnClickListener {
            binding.modelTypeLine.visibility = View.GONE
            modelType = mModelType

        }




        // 设置发送按钮的点击事件
        binding.sendButton.setOnClickListener {
            adapter.setIsStop(false)
            val message = binding.messageEditText.text.toString().trim()
            Log.e("ceshi","点击")
            if (message.isNotEmpty()) {
                isVoice = false
                /*if (!isTrueApiKey){
                    Toast.makeText(this, "请先填写正确的apikey~", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }*/
                if (isPicture){
                    if (imageCounter != imageUrlServiceResultList.size && imageUrlServiceResultList.isEmpty()){//imageUrlServiceResult == "" || imageUrlServiceResultList.isEmpty() || (imageUrlServiceResultList.size-1) != imageCounter
                        Toast.makeText(this, "正在解析图片请稍后或者重新发送", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    isPicture = false
                    if (messageList.isNotEmpty()) {
                        //messageList.removeLast()
                        //binding.photoPreImage.visibility = View.GONE
                        val imageUrlLocal1 = "![示例图片]($imageUrlLocal)"
                        var mImageUrlLocalStrBulder = StringBuilder()
                        for (imagelocal in imageUrlLocalList){
                           //mImageUrlLocalStrBulder.append("![示例图片]($imagelocal)")
                            mImageUrlLocalStrBulder.append("<img src=\"$imagelocal\" width=\"150\" height=\"150\">")

                        }
                        //Log.e("ceshi","返回的URL${imageUrlServiceResultList.size},,$imageCounter")
                        messageList.add("${mImageUrlLocalStrBulder.toString()}<br>$message")
                        messageList.add("file:///android_asset/loading.html")
                        messageTimesList.add(TimeUtils.getCurrentDateTime())
                        messageTimesList.add(TimeUtils.getCurrentDateTime())

                        binding.profileImage.setImageResource(R.drawable.icon_profile2)
                        binding.profileImage.clearColorFilter()
                    }
                }else{
                    messageList.add(message)
                    mMessageList = messageList
                    messageList.add("file:///android_asset/loading.html")
                    messageTimesList.add(TimeUtils.getCurrentDateTime())
                    messageTimesList.add(TimeUtils.getCurrentDateTime())
                }
                // 添加用户消息
                //messageList.add("你: $message")
                //messageList.add(message)
                //messageList.add("loading")
                // 模拟机器人回复
                //messageList.add("机器人: 收到你的消息：$message")
                isChat = true
                adapter.updateIsChat(true)
                lifecycleScope.launch(Dispatchers.IO) {
                    if (binding.modelTypeLine.visibility == View.VISIBLE){
                        mModelType = modelType
                        modelType = binding.selectModelTypeAdd.text.toString()
                        if (isEven(messageList.size-1)){
                            adapter.setModelType(modelType,messageList.size-1)
                        }else{
                            adapter.setModelType(modelType,messageList.size-2)
                        }

                    }

                    if (!modelType.contains("gpt") && uidModel != ""){
                        modelType = "$modelType-$uidModel"
                    }
                    //modelType = "$modelType-$uidModel"
                    Log.e("ceshi","0模型类型$modelType,,$chatPrompt,,$serviceProvider,,$message,,$userId,,$isPrompt")
                    isResult = false
                    isSendQuestion = true

                    //标题生成
                    if (chatType.contains("新的聊天") && isFirst){
                        isFirst = false
                        //isGetTitle = true
                        isGetTitle.set(true)
                        if (false){//serviceProvider=="自定义"
                            chatViewModel.sendQuestion1(getChatTitle(messageList[1]),modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResult,false,apiKey,extractSwitch,apiService)
                        }else{
                            chatViewModel.sendQuestion(getChatTitle(messageList[1]),modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResultList,false,apiKey,extractSwitch,apiService,true,messageList,serviceProvider)
                        }

                    }

                    if (isPrompt){
                        if (false){
                            chatViewModel.sendQuestion1(chatPrompt+"&&&"+message,modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResult,isPrompt,apiKey,extractSwitch,apiService)
                        }else{
                            chatViewModel.sendQuestion(chatPrompt+"&&&"+message,modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResultList,isPrompt,apiKey,extractSwitch,apiService,isClearText,mMessageList,serviceProvider)
                        }


                    }else{
                        if (false){
                            chatViewModel.sendQuestion1(message,modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResult,isPrompt,apiKey,extractSwitch,apiService)
                        }else{
                            chatViewModel.sendQuestion(message,modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResultList,isPrompt,apiKey,extractSwitch,apiService,isClearText,mMessageList,serviceProvider)
                        }


                    }

                    mImageUrlServiceResultList.clear()
                    mImageUrlLocalList.clear()
                    imageUrlLocalList.clear()
                    imageUrlServiceResultList.clear()
                    imageCounter = 0



                }

                CoroutineScope(Dispatchers.Main).launch {
                    val rotateAnimation = RotateAnimation(
                        0f, 360f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                    )
                    rotateAnimation.duration = 1000
                    rotateAnimation.repeatCount = Animation.INFINITE
                    rotateAnimation.interpolator = LinearInterpolator()
                    //binding.imageLoading.startAnimation(rotateAnimation)//旋转动画


//                    binding.imageLoad.visibility = View.VISIBLE
//                    binding.loadLine.visibility = View.VISIBLE
                }


                binding.imageLineHorScroll.visibility = View.GONE
                binding.imageLine.removeAllViews()

                // 通知适配器数据已更改
                adapter.notifyDataSetChanged()
                // 清空输入框
                binding.messageEditText.text?.clear()
            }else{
                Log.e("ceshi","0模型类型$modelType")
                Toast.makeText(this, "无法发送空白内容", Toast.LENGTH_SHORT).show()
            }
        }

        binding.sendStopButton.setOnClickListener {
            binding.sendButton.visibility = View.VISIBLE
            binding.sendStopButton.visibility = View.GONE
            adapter.setIsStop(true)
        }


        binding.profileImage.setOnClickListener {
            // 点击时修改颜色
            binding.profileImage.setImageResource(R.drawable.icon_profile2)
            binding.profileImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
            // 使用 lifecycleScope 启动协程，自动绑定生命周期
            lifecycleScope.launch {
                // 延迟 200ms 后恢复颜色（若 Activity 已销毁，协程会自动取消）
                delay(200)
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    binding.profileImage.setImageResource(R.drawable.icon_profile2)
                    binding.profileImage.clearColorFilter()
                }
            }
            // 调用相册选择器
            showPicturePickerDialog()
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(intent, PICK_IMAGE_REQUEST)

            /*if (!isProfile){
                binding.profileImage.setImageResource(R.drawable.icon_profile2)
                binding.profileImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
                isProfile = true
//                // 调用相册选择器
//                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                startActivityForResult(intent, PICK_IMAGE_REQUEST)
//                dispatchTakePictureIntent()
                showPicturePickerDialog()
            }else{
                binding.profileImage.setImageResource(R.drawable.icon_profile2)
                binding.profileImage.clearColorFilter()
                binding.photoPreImage.visibility = View.GONE
                isProfile = false
                isPicture = false
            }*/

        }

        binding.textScroll.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentX = event.x
                    val deltaX = currentX - startX
                    val scrollX = binding.textScroll.scrollX
                    binding.textScroll.scrollTo((scrollX - deltaX).toInt(), 0)
                    startX = currentX
                }
            }
            true
        }

        binding.selectedTextView.setOnClickListener {

            if (binding.selectedTextView.text == "配置模型"){
                //Toast.makeText(this, "请先配置模型apikey!", Toast.LENGTH_SHORT).show()
                // 使用自定义 Toast
                CustomToast.makeText(
                    context = this,
                    message = "请先配置模型apikey!",
                    duration = Toast.LENGTH_SHORT,
                    gravity = Gravity.CENTER
                ).show()
            }else{
                binding.selectedTextView.visibility = View.GONE
                binding.editSearchModel.visibility = View.VISIBLE
                binding.imageDown.visibility = View.GONE
                binding.imageModelTypeSearch.visibility = View.VISIBLE
            }
        }

        binding.editSearchModel.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


                Log.e("ceshi","0搜索文字：${start},$before,$count")

                /*if (start>=2){
                    val job2 = lifecycleScope.launch(Dispatchers.IO) {
                        for (modelType in modelList){
                            if (modelType.contains(s.toString(),ignoreCase = true)){
                                modelSearchList.add(modelType)
                            }
                        }
                    }

                    lifecycleScope.launch(Dispatchers.Main) {
                        job2.join() // 等待数据库操作完成
                        Log.e("ceshi","搜索文字模型：${modelSearchList}")
                        setupPopupWindow(modelSearchList)

                    }
                }*/
                modelSearchList.clear()
                val job2 = lifecycleScope.launch(Dispatchers.IO) {
                    for (modelType in modelList){
                        if (modelType.contains(s.toString(),ignoreCase = true)){
                            modelSearchList.add(modelType)
                        }
                    }
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    job2.join() // 等待数据库操作完成
                    Log.e("ceshi","搜索文字模型：${modelSearchList}")
                    setupPopupWindow(modelSearchList)

                }
//                if (s.toString() == ""){
//                    lifecycleScope.launch(Dispatchers.Main){
//                        Log.e("ceshi","搜索文字：执行")
//                        binding.recycle2.visibility = View.GONE
//                        binding.recycle1.visibility = View.VISIBLE
//                    }
//                }
            }

            override fun afterTextChanged(s: Editable?) {
                Log.e("ceshi","afterTextChanged搜索文字：${s.toString()}")



                if (s?.isEmpty() == true) {
                    lifecycleScope.launch(Dispatchers.Main){
                        Log.e("ceshi","搜索文字：执行")
                        //不做延迟处理，recycle1数据还没有更新完，就是导致下面的visibility不起作用
                        delay(500)
                        Log.e("ceshi","搜索文字：执行1")
                        binding.root.requestLayout() // 强制更新布局
                    }
                    modelSearchList.clear()
                    setupPopupWindow(modelList)
                    showPopup(binding.editSearchModel)
                }
                showPopup(binding.editSearchModel)
                //modelSearchList.clear()
            }
        })

        // 设置焦点变化监听器
        binding.editSearchModel.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                binding.selectedTextView.visibility = View.VISIBLE
                binding.imageDown.visibility = View.VISIBLE
                binding.editSearchModel.visibility = View.GONE
                binding.imageModelTypeSearch.visibility = View.GONE

                // 失去焦点时：显示提示并隐藏键盘
//                val inputText = editText.text.toString()
//                val toastText = if (inputText.isNotEmpty()) "输入内容：$inputText" else "内容为空"
//                Toast.makeText(this, "输入框失去焦点，$toastText", Toast.LENGTH_SHORT).show()
//                hideKeyboard(editText) // 隐藏键盘
            } else {
                // 获得焦点时：显示提示
                //Toast.makeText(this, "输入框获得焦点", Toast.LENGTH_SHORT).show()
                setupPopupWindow(modelList)
                showPopup(binding.editSearchModel)
            }
        }

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onResume() {
        super.onResume()
        Log.e("ceshi","onResume$isNetWorkThink,,$isDeepThink")
        if (isDeepThink){
            binding.deepImage.setImageResource(R.drawable.icon_thinking3)
            binding.deepImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
        }else{
            binding.deepImage.setImageResource(R.drawable.icon_thinking3)
            binding.deepImage.clearColorFilter()
        }
        if (isNetWorkThink){
            binding.networkImage.setImageResource(R.drawable.icon_network3)
            binding.networkImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
        }else{
            binding.networkImage.setImageResource(R.drawable.icon_network3)
            binding.networkImage.clearColorFilter()
        }
        /*if (isEye){
            binding.lookImage.setImageResource(R.drawable.icon_eye3)
            binding.lookImage.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)
            binding.chatPreLine.visibility = View.VISIBLE
            adapter.setOpenPreEye(true)
            //adapter.notifyDataSetChanged()
        }else{
            binding.lookImage.setImageResource(R.drawable.icon_eye3)
            binding.lookImage.clearColorFilter()
            binding.chatPreLine.visibility = View.GONE
            adapter.setOpenPreEye(false)
            //adapter.notifyDataSetChanged()
        }*/


        // 添加滚动监听
        binding.chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                Log.e("ceshi","屏幕高度：${getScreenHeight(this@ChatActivity)/2},,$totalItemCount,,$lastVisibleItemPosition")
                Log.e("ceshi","recycle屏幕高度：${layoutManager.height}")
                // 当聊天内容超出屏幕且不是最后一项可见时显示按钮
                if (totalItemCount > 0 && totalItemCount>=4 && (totalItemCount-lastVisibleItemPosition>1)) {
                    binding.floatingButton.visibility = View.VISIBLE
                } else {
                    binding.floatingButton.visibility = View.GONE
                }

                // 判断滑动方向：dy > 0 表示向下滑动（垂直方向滚动距离）
                if (dy > 0) {
                    Log.d("ScrollListener", "手指向下滑动，dy = $dy")
                    // 在这里处理向下滑动的逻辑（如加载更多、隐藏顶部控件等）
                    // 示例：显示"向下滑动中"
                    // showToast("向下滑动中...")
                } else if (dy < 0) {
                    Log.d("ScrollListener", "手指向上滑动，dy = $dy")
                    adapter.updateIsChat(false)
                    if (binding.keyBoardImage.visibility == View.GONE){
                        binding.sendButton.visibility = View.VISIBLE
                        binding.sendStopButton.visibility = View.GONE
                    }


                }
            }

            // 滚动状态变化时触发（开始滚动、停止滚动等）
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        Log.d("ScrollListener", "滚动停止")
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        Log.d("ScrollListener", "手指正在拖动")
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        Log.d("ScrollListener", "惯性滚动中")
                    }
                }
            }


        })

        // 设置按钮点击事件
        binding.floatingButton.setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(binding.floatingButton)
            scrollToBottom()
        }

        //isGetTitle = true
        binding.lineBack.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        binding.chatSettingLine.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        binding.appStoreConst.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        binding.cueWordConst.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        if (!isDeepThink){
            if (modelType.contains("reason")) {
                binding.deepImage.isEnabled = false
                binding.deepImage.setImageResource(R.drawable.icon_thinking3)
                binding.deepImage.setColorFilter(ContextCompat.getColor(this@ChatActivity, R.color.shadow), PorterDuff.Mode.SRC_IN)
            }else{
                binding.deepImage.isEnabled = true
                binding.deepImage.setImageResource(R.drawable.icon_thinking3)
                binding.deepImage.clearColorFilter()
            }
        }

        // 仅在打开时读取一次数据
        lifecycleScope.launch((Dispatchers.IO)) {
            val readServiceProviderData = dataStoreManager.readServiceProviderData.first()
            val readServiceUrl = dataStoreManager.readServiceUrl.first()?:""
            val readCustomizeModelIdData = dataStoreManager.readCustomizeModelIdData.first()?:"Qwen/Qwen2.5-7B-Instruct"
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
                    //modelType = readCustomizeModelIdData
                }
            }





            val data = dataStoreManager.readData.first()
            val readCustomizeKeyData = dataStoreManager.readCustomizeKeyData.first()

            readServiceProviderData.let {
                if (it=="自定义"){
                    readCustomizeKeyData?.let {
                        Log.e("ceshi","appKey是多少：$it")
                        apiKey = it
                        chatViewModel.get302AiModelList(it,apiService)
                    }
                }else{
                    data?.let {
                        Log.e("ceshi","appKey是多少：$it")
                        apiKey = it
                        chatViewModel.get302AiModelList(it,apiService)
                    }
                }
            }



            if (apiKey == ""){
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.settingApiKeyCon.visibility = View.VISIBLE
                    binding.settingRedImage.visibility = View.VISIBLE
                    binding.selectedTextView.text = "配置模型"
                }
            }

            val readPreSwitch = dataStoreManager.readPreSwitch.first()
            readPreSwitch?.let {
                Log.e("ceshi","readPreSwitch是多少：$it")
                lifecycleScope.launch(Dispatchers.Main) {
                    /*if (readPreSwitch){
                        binding.lookImage.visibility = View.VISIBLE
                    }else{
                        binding.lookImage.visibility = View.GONE
                        if (binding.chatPreLine.visibility == View.VISIBLE){
                            binding.chatPreLine.visibility = View.GONE
                        }
                    }*/
                    if (readPreSwitch){
                        adapter.setOpenPreEye(true)
                        adapter.notifyDataSetChanged()
                    }else{
                        adapter.setOpenPreEye(false)
                        adapter.notifyDataSetChanged()
                    }
                }

            }

            val readClearWordsSwitch = dataStoreManager.readClearWordsSwitch.first()
            readClearWordsSwitch?.let {
                Log.e("ceshi","readClearWordsSwitch是多少：$it")
                lifecycleScope.launch(Dispatchers.Main) {
                    if (readClearWordsSwitch){
                        binding.clearImage.visibility = View.VISIBLE
                    }else{
                        binding.clearImage.visibility = View.GONE
                    }
                }

            }

            val readModelType = dataStoreManager.readModelType.first()?: "gpt-4o"
            val readIsChangeModelSetting = dataStoreManager.readIsChangeModelSetting.first()?:false
            readModelType?.let {
                Log.e("ceshi","readModelType是多少：$readModelType,,$isToPicture,,$priModelType,,$serviceProvider,,$isScreenTurnedOff,,$readIsChangeModelSetting")
                lifecycleScope.launch(Dispatchers.Main) {
                    //binding.selectedTextView.text = readModelType
                    if (!chatType.contains("[") && !isScreenTurnedOff && !chatType.contains("新的聊天")){
                        //binding.selectedTextView.text = readModelType
                        lifecycleScope.launch(Dispatchers.IO) {
                            dataStoreManager.saveIsChangeModelSetting(false)
                        }
                    }
                    if (!isToPicture){
                        if (priModelType != ""){
                            if (serviceProvider != "自定义"){
                                //modelType = priModelType
                            }
                        }else{
                            if (serviceProvider != "自定义" && !isScreenTurnedOff && readIsChangeModelSetting){
                                //modelType = readModelType
                                lifecycleScope.launch(Dispatchers.IO) {
                                    dataStoreManager.saveIsChangeModelSetting(false)
                                }
                            }

                        }
                    }else{
                        isToPicture = false
                    }

                }

            }

            val readSearchSwitch = dataStoreManager.readSearchSwitch.first()
            readSearchSwitch?.let {
                Log.e("ceshi","readSearchSwitch：$it")
                lifecycleScope.launch(Dispatchers.Main) {
                    if (readSearchSwitch){
                        binding.networkImage.visibility = View.VISIBLE
                    }else{
                        binding.networkImage.visibility = View.GONE
                    }
                }

            }

            //自动提取
            val readExtractSwitch = dataStoreManager.readExtractSwitch.first()
            readExtractSwitch?.let {
                Log.e("ceshi","readExtractSwitch是多少：$it")
                lifecycleScope.launch(Dispatchers.Main) {
                    if (readExtractSwitch){
                        extractSwitch = true
                    }else{
                        extractSwitch = false
                    }
                }

            }


        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.e("ceshi","onRestart")

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.e("ceshi","onRestoreInstanceState")
        modelType = savedInstanceState.getString("VARIABLE_MODEL_TYPE").toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.e("ceshi","onSaveInstanceState")
        // 保存变量B
        outState.putString("VARIABLE_MODEL_TYPE", modelType)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        isToPicture = true
        Log.e("ceshi","回调返回值：$requestCode,,$resultCode,,$data")
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data!!
            //val imagePath = getRealPathFromUri(selectedImageUri)
            //val imageUrl = "file://$imagePath"//不能这样处理
            isPicture = true
            //imageUrlLocal = imageUrl
            imageUrlLocal = "$selectedImageUri"

            //上传图片到服务器
            lifecycleScope.launch(Dispatchers.IO) {
                chatViewModel.upLoadImage(this@ChatActivity,
                    SystemUtils.uriToTempFile(this@ChatActivity, selectedImageUri),"imgs",false,apiService)
            }

            // 使用 Glide 加载图片到 ImageView
            /*binding.photoPreImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(imageUrlLocal)
                .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
                .error(android.R.drawable.stat_notify_error) // 加载失败占位图
                .into(binding.photoPreImage)

            binding.photoPreImage.setOnClickListener {
                showImagePreviewDialog(imageUrlLocal)
            }*/

            addNewImageView(imageUrlLocal)


            //messageList.add("$selectedImageUri")
            // 通知适配器数据已更改
//            adapter.notifyDataSetChanged()
//            adapter.notifyItemInserted(adapter.itemCount - 1)
//            binding.chatRecyclerView.scrollToPosition(adapter.itemCount - 1)
//            //pictureNumber++
//            //adapter.setPictureNumber(pictureNumber)
//            adapter.updateIsPicture(true,messageList.size-1)
            Log.e("ceshi","1图片地址$imageUrlLocal")
            // 生成包含图片的 HTML 代码
//            val html = """
//                <!DOCTYPE html>
//                <html>
//                <body>
//                    <img src="$imageUrl" alt="Selected Image">
//                </body>
//                </html>
//            """.trimIndent()
//
//            // 加载 HTML 到 WebView
//            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }else if(requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK){

            currentPhotoPath?.let { path ->
                isPicture = true
                imageUrlLocal = "$path"
                val imageFile = File(path)
                //Log.e("ceshi","3图片地址$imageFile")
                if (imageFile.exists()) {
                    val contentUri = Uri.fromFile(imageFile)
                    //上传图片到服务器
                    lifecycleScope.launch(Dispatchers.IO) {
                        chatViewModel.upLoadImage(this@ChatActivity,
                            SystemUtils.uriToTempFile(this@ChatActivity, contentUri),"imgs",false,apiService)
                    }

                    // 使用 Glide 加载图片到 ImageView
                    /*binding.photoPreImage.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(imageUrlLocal)
                        .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
                        .error(android.R.drawable.stat_notify_error) // 加载失败占位图
                        .into(binding.photoPreImage)

                    binding.photoPreImage.setOnClickListener {
                        showImagePreviewDialog(imageUrlLocal)
                    }*/
                    galleryAddPic()
                    //Log.e("ceshi","2图片地址$imageUrlLocal")
                    addNewImageView(imageUrlLocal)
                } else {
                    Log.e("Camera", "图片文件不存在: $path")
                }




            } ?: run {
                Log.e("Camera", "未找到保存的图片路径")
            }

        }else{
            binding.profileImage.setImageResource(R.drawable.icon_profile2)
            binding.profileImage.clearColorFilter()
            isProfile = false
            isPicture = false
        }

    }

    private fun setupPopupWindow(modelList: MutableList<String>) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_list, null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            isOutsideTouchable = true
            animationStyle = android.R.style.Animation_Dialog
        }

        val recyclerView = popupView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.popupRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager?.scrollToPosition(positionModeType)
        recyclerView.adapter = object : BaseQuickAdapter<String, BaseViewHolder>(
            R.layout.item_popup_select, modelList
        ) {
            override fun convert(holder: BaseViewHolder, item: String) {
                if (holder.adapterPosition == 1){
                    /*val spannableString = SpannableString(item)
                    // 找到“（最强）”的起始和结束位置
                    val startIndex = item.indexOf("(最新版)")
                    val endIndex = startIndex + "(最新版)".length
                    // 设置字体颜色
                    val colorSpan = ForegroundColorSpan(Color.GRAY)
                    spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // 设置字体大小
                    val sizeSpan = RelativeSizeSpan(0.5f)
                    spannableString.setSpan(sizeSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)*/

                    holder.setText(R.id.itemTextView, item)
                }
                holder.setText(R.id.itemTextView, item)
                if (holder.adapterPosition == 0 || holder.adapterPosition == 3) {
                    // 给第一个位置的列表字体加粗
                    //holder.setTextViewTextBold(R.id.itemTextView)
                    // 重新设置字体大小
                    //holder.setTextViewTextSize(R.id.itemTextView, 20f)
                }
            }
        }.apply {
            setOnItemClickListener { _, _, position ->
                popupWindow.dismiss()
                findViewById<TextView>(R.id.selectedTextView).text = modelList[position]
                modelType = modelList[position]
                positionModeType = position
                if (modelType.contains("reason")) {
                    binding.deepImage.isEnabled = false
                    binding.deepImage.setImageResource(R.drawable.icon_thinking3)
                    binding.deepImage.setColorFilter(ContextCompat.getColor(this@ChatActivity, R.color.shadow), PorterDuff.Mode.SRC_IN)
                }else{
                    if (isDeepThink){
                        binding.deepImage.isEnabled = true
                        binding.deepImage.setImageResource(R.drawable.icon_thinking3)
                        binding.deepImage.setColorFilter(ContextCompat.getColor(this@ChatActivity, R.color.blue), PorterDuff.Mode.SRC_IN)
                    }else{
                        binding.deepImage.isEnabled = true
                        binding.deepImage.setImageResource(R.drawable.icon_thinking3)
                        binding.deepImage.clearColorFilter()
                    }
                }
                findViewById<TextView>(R.id.selectedTextView).visibility = View.VISIBLE
                binding.imageDown.visibility = View.VISIBLE
                binding.editSearchModel.visibility = View.GONE
                binding.imageModelTypeSearch.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("ceshi","onDestroy")
        userId = ""
        isFirstInitData = false
        binding.imageLineHorScroll.visibility = View.GONE
        binding.imageLine.removeAllViews()
        mImageUrlServiceResultList.clear()
        mImageUrlLocalList.clear()
        imageUrlLocalList.clear()
        imageUrlServiceResultList.clear()
        imageCounter = 0
        popupWindow?.dismiss()
        tts.release()
    }

    override fun onStop() {
        super.onStop()
        Log.e("ceshi","onStop标题是：$chatType")
        if (chatType == ""){
            chatType = "新的聊天"
        }
        isChat = false
        adapter.updateIsChat(false)
        adapter.clearPicture()
        pictureNumber = 0
        //chatDatabase.chatDao().insertChat(ChatItemRoom(0,"新的聊天", messageList, "2025-04-15 12:00:00"))
        if (!isToSetting){
            isToSetting = false
        }
        lifecycleScope.launch(Dispatchers.IO) {

            /*if (chatDatabase.chatDao().checkTitleExists(chatType)){
                //先删除后添加
                chatDatabase.chatDao().deleteChatByTitle(chatType)
                chatDatabase.chatDao().insertChat(ChatItemRoom(0,chatType, messageList, chatTime))
            }else{
                //不存在直接添加
                chatDatabase.chatDao().insertChat(ChatItemRoom(0,chatType, messageList, chatTime))
            }*/
            Log.e("ceshi","0返回的模型类型$modelType")
            //直接插入，做了title唯一性，如果有了就替换成最新的
            chatDatabase.chatDao().insertChat(ChatItemRoom(chatId,chatType, messageList, chatTime,messageTimesList,modelType,chatPrompt,displayUrl,isDeepThink,isNetWorkThink,userId,isEye))
            Log.e("ceshi", "B插入完成时间：${System.currentTimeMillis()}") // 添加日志

        }


        //messageList.clear()
    }

    private fun showPopup(anchorView: View) {
        popupWindow.showAsDropDown(
            anchorView,
            -(anchorView.width - popupWindow.width) / 2,
            25
        )
    }

    // 扩展函数用于设置 TextView 字体加粗
    fun BaseViewHolder.setTextViewTextBold(viewId: Int) {
        val textView = getView<android.widget.TextView>(viewId)
        textView.paint.isFakeBoldText = true
    }

    // 扩展函数用于设置 TextView 字体大小
    fun BaseViewHolder.setTextViewTextSize(viewId: Int, size: Float) {
        val textView = getView<android.widget.TextView>(viewId)
        textView.textSize = size
    }

    private fun getRealPathFromUri(uri: Uri): String {
        var path = ""
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            path = cursor.getString(columnIndex)
            cursor.close()
        }
        return path
    }

    override fun onOverItemClick(wordPrintOverItem: Boolean) {
        printAnswer = wordPrintOverItem
        if (!isVoice){
            binding.sendButton.visibility = View.VISIBLE
            binding.sendStopButton.visibility = View.GONE
        }
    }

    @RequiresApi(35)
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onBackChatTool(backChatToolItem: BackChatToolItem) {
        when(backChatToolItem.type){
            "delete" -> {
                adapter.updateIsChat(false)
                messageList.remove(backChatToolItem.message)
                Log.e("ceshi","位置是${backChatToolItem.position}")
                messageList.add(backChatToolItem.position,"这是删除过的内容变为空白")
                adapter.notifyItemRemoved(backChatToolItem.position)
            }
            "again" -> {
                if (isEven(backChatToolItem.position)){
                   //机器人
                    Log.e("ceshi","位置是${backChatToolItem.position}")
                    if (backChatToolItem.position>0){
                        val againMessage = messageList[backChatToolItem.position-1]
                        if (backChatToolItem.position == messageList.size-1){
                            messageList.removeLast()
                            messageList.removeLast()
                        }

                        messageList.add(againMessage)
                        mMessageList = messageList
                        messageList.add("file:///android_asset/loading.html")
                        messageTimesList.add(TimeUtils.getCurrentDateTime())
                        messageTimesList.add(TimeUtils.getCurrentDateTime())
                        // 通知适配器数据已更改
                        adapter.notifyDataSetChanged()
                        CoroutineScope(Dispatchers.IO).launch {

                            if (!modelType.contains("gpt") && uidModel != ""){
                                modelType = "$modelType-$uidModel"
                            }
                            //modelType = "$modelType-$uidModel"
                            Log.e("ceshi","模型类型$modelType")
                            if (isPrompt){
                                if (false){
                                    chatViewModel.sendQuestion1(chatPrompt+"&&&"+messageList[backChatToolItem.position-1],modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResult,isPrompt,apiKey,extractSwitch,apiService)
                                }else{
                                    chatViewModel.sendQuestion(chatPrompt+"&&&"+messageList[backChatToolItem.position-1],modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResultList,isPrompt,apiKey,extractSwitch,apiService,isClearText,mMessageList,serviceProvider)
                                }

                            }else{
                                if (false){
                                    chatViewModel.sendQuestion1(messageList[backChatToolItem.position-1],modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResult,isPrompt,apiKey,extractSwitch,apiService)
                                }else{
                                    chatViewModel.sendQuestion(messageList[backChatToolItem.position-1],modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResultList,isPrompt,apiKey,extractSwitch,apiService,isClearText,mMessageList,serviceProvider)
                                }

                            }


                        }
                    }

                }else{
                    Log.e("ceshi","0位置是${backChatToolItem.position}")
                    val againMessage = messageList[backChatToolItem.position]
                    if (backChatToolItem.position == messageList.size-2){
                        messageList.removeLast()
                        messageList.removeLast()
                    }
                    messageList.add(againMessage)
                    mMessageList = messageList
                    messageList.add("file:///android_asset/loading.html")
                    messageTimesList.add(TimeUtils.getCurrentDateTime())
                    messageTimesList.add(TimeUtils.getCurrentDateTime())
                    // 通知适配器数据已更改
                    adapter.notifyDataSetChanged()
                    CoroutineScope(Dispatchers.IO).launch {

                        if (!modelType.contains("gpt") && uidModel != ""){
                            modelType = "$modelType-$uidModel"
                        }
                        //modelType = "$modelType-$uidModel"
                        Log.e("ceshi","模型类型$modelType")
                        if (isPrompt){
                            if (false){
                                chatViewModel.sendQuestion1(chatPrompt+"&&&"+messageList[backChatToolItem.position],modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResult,isPrompt,apiKey,extractSwitch,apiService)
                            }else{
                                chatViewModel.sendQuestion(chatPrompt+"&&&"+messageList[backChatToolItem.position],modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResultList,isPrompt,apiKey,extractSwitch,apiService,isClearText,mMessageList,serviceProvider)
                            }

                        }else{
                            if (false){
                                chatViewModel.sendQuestion1(messageList[backChatToolItem.position],modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResult,isPrompt,apiKey,extractSwitch,apiService)
                            }else{
                                chatViewModel.sendQuestion(messageList[backChatToolItem.position],modelType,isNetWorkThink,isDeepThink,this@ChatActivity,userId,imageUrlServiceResultList,isPrompt,apiKey,extractSwitch,apiService,isClearText,mMessageList,serviceProvider)
                            }

                        }


                    }
                }

            }

            "fixed" -> {
                Log.e("ceshi","0固定位置是${backChatToolItem.position}")
                if (isEven(backChatToolItem.position)){
                    messageList[0] = backChatToolItem.message
                }else{
                    messageList[1] = backChatToolItem.message
                }
                // 通知适配器数据已更改
                adapter.notifyDataSetChanged()
            }

            "broadcast" -> {
                Log.e("ceshi","0固定位置是${backChatToolItem.position}")
                if (!tts.isSpeaking){
                    tts.speakText(backChatToolItem.message)
                }
            }

            "pre" -> {

                if (backChatToolItem.message.contains("```")){
                    codeStr = backChatToolItem.message
                }
                Log.e("ceshi","0预览位置是${backChatToolItem.position},,字符串：$codeStr")
                binding.lineBackChat.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
                codeStr = backChatToolItem.message
                binding.chatRecyclerView.visibility = View.GONE
                binding.const1.visibility = View.GONE
                binding.codePreCon.visibility = View.VISIBLE
                binding.codePreLine.visibility = View.VISIBLE
                Log.e("ceshi","载入html字符串：${extractHtmlFromMarkdownCode(codeStr)}")
                // 加载HTML内容到WebView
                binding.codePreWeb.loadDataWithBaseURL(
                    null,
                    extractHtmlFromMarkdownCode(codeStr),
                    "text/html",
                    "UTF-8",
                    null
                )

            }

            "codePre" -> {
                // 初始化Markwon（包含常用插件）
                codePreClick = true
                val markwon = Markwon.builder(this)
                    .usePlugin(CorePlugin.create()) // 核心解析插件
                    .usePlugin(HtmlPlugin.create()) // 支持HTML标签
                    .usePlugin(StrikethroughPlugin.create()) // 支持删除线
                    .usePlugin(TaskListPlugin.create(this)) // 支持任务列表
                    .usePlugin(TablePlugin.create(this)) // 支持表格
                    .usePlugin(GlideImagesPlugin.create(this)) // 使用Glide加载图片（需添加Glide依赖）
                    .build()


                onClickColor(binding.chatPreLine)
                if (backChatToolItem.message.contains("```")){
                    codeStr = backChatToolItem.message
                }
                Log.e("ceshi","位置是${backChatToolItem.position},,字符串：$codeStr")
                Log.e("ceshi","位置载入html字符串：${extractHtmlFromMarkdownCode(codeStr)}")
                if (extractHtmlFromMarkdownCode(codeStr)==""){
                    binding.noCodePreCon.visibility = View.VISIBLE
                }else{
                    binding.noCodePreCon.visibility = View.GONE
                    // 加载HTML内容到WebView
                    binding.codePreWeb.loadDataWithBaseURL(
                        null,
                        extractHtmlFromMarkdownCode(codeStr),
                        "text/html",
                        "UTF-8",
                        null
                    )
                    markwon.setMarkdown(binding.codePreTv, extractHtml(codeStr))
                }
                binding.chatPreLine.visibility = View.VISIBLE
                binding.chatRecyclerView.visibility = View.GONE
                binding.const1.visibility = View.GONE
                binding.codePreCon.visibility = View.VISIBLE
                binding.codePreLine.visibility = View.VISIBLE

                isCodePre = true
            }

            "cancelClearContext" -> {
                adapter.updateIsChat(false)
                isClearText = false
                userId = mUserId
                adapter.setClearNumber(messageList.size - 1,false)
                adapter.notifyDataSetChanged()
            }

        }
    }

    override fun onDeleteImagePosition(position: Int) {
        if (imageUrlLocalList.size == imageUrlServiceResultList.size){
            imageUrlServiceResultList.remove(mImageUrlServiceResultList[position])
        }
        imageUrlLocalList.remove(mImageUrlLocalList[position])
        imageCounter--
        if (imageUrlLocalList.isEmpty()){
            binding.imageLineHorScroll.visibility = View.GONE
            binding.imageLine.removeAllViews()
            mImageUrlServiceResultList.clear()
            mImageUrlLocalList.clear()
            imageCounter = 0
        }
        Log.e("ceshi","删除了图片位置$position,,${imageUrlLocalList.isEmpty()}")
    }

    override fun onPreImageClick(resUrl: String) {
        showImagePreviewDialog(resUrl)
    }

    fun isEven(number: Int): Boolean {
        return number % 2 == 0
    }

    // 调整高度的具体方法
    private fun adjustRecyclerViewHeight() {
        binding.chatRecyclerView.post {
            val totalHeight = calculateRecyclerViewTotalHeight(binding.chatRecyclerView)
            Log.e("ceshi","测量总高$totalHeight")
            val params = binding.chatRecyclerView.layoutParams as ConstraintLayout.LayoutParams
            params.height = totalHeight*3
            binding.chatRecyclerView.layoutParams = params

            // 可选：打印最终高度验证是否生效
            binding.chatRecyclerView.postDelayed({
                Log.e("ceshi", "最终高度: ${binding.chatRecyclerView.height}")
            }, 100)
        }
    }

    // 计算所有 Item 的总高度
    private fun calculateRecyclerViewTotalHeight(recyclerView: RecyclerView): Int {
        val adapter = recyclerView.adapter ?: return 0
        // 进行类型检查和空值检查
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return 0

        var totalHeight = 0
        // 累加所有子项的高度（通过 Adapter 测量）
        for (i in 0 until adapter.itemCount) {
            val viewHolder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
            adapter.onBindViewHolder(viewHolder, i)

            // 测量子项视图（宽度与 RecyclerView 一致，高度自动）
            viewHolder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            totalHeight += viewHolder.itemView.measuredHeight
        }

        // 加上 RecyclerView 自身的 padding
        totalHeight += recyclerView.paddingTop + recyclerView.paddingBottom
        // 创建一个默认的 State 对象
        val defaultState = RecyclerView.State()
        // 加上 ItemDecoration 的间距（如果有）
        val decorCount = recyclerView.itemDecorationCount
        for (i in 0 until decorCount) {
            val outRect = Rect()
            recyclerView.getItemDecorationAt(i).getItemOffsets(
                outRect,
                // 用第一个子项模拟间距（假设所有子项间距相同）
                recyclerView.getChildAt(0) ?: return totalHeight,
                recyclerView,
                defaultState
            )
            totalHeight += outRect.top + outRect.bottom
        }

        return totalHeight
    }

    /*private fun calculateRecyclerViewTotalHeight1(recyclerView: RecyclerView): Int {
        val adapter = recyclerView.adapter ?: return 0
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return 0

        var totalHeight = 0
        // 1. 累加所有子项的高度（通过 Adapter 测量）
        for (i in 0 until adapter.itemCount) {
            val viewHolder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
            adapter.onBindViewHolder(viewHolder, i)

            // 测量子项视图（宽度与 RecyclerView 一致，高度自动）
            viewHolder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            totalHeight += viewHolder.itemView.measuredHeight
        }

        // 2. 加上 RecyclerView 自身的 padding（必须！）
        totalHeight += recyclerView.paddingTop + recyclerView.paddingBottom

        // 3. 加上 ItemDecoration 的间距（如果有）
        val decorCount = recyclerView.itemDecorationCount
        for (i in 0 until decorCount) {
            val outRect = Rect()
            recyclerView.getItemDecorationAt(i).getItemOffsets(
                outRect,
                // 用第一个子项模拟间距（假设所有子项间距相同）
                recyclerView.getChildAt(0) ?: return totalHeight,
                recyclerView,
                null
            )
            totalHeight += outRect.top + outRect.bottom
        }

        return totalHeight
    }*/

    // 判断是否已经滚动到底部
    private fun isAtBottom(): Boolean {
        val recyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val totalItemCount = layoutManager.itemCount

        // 检查是否最后一个 Item 完全可见
        return if (totalItemCount == 0) {
            true
        } else {
            val lastItemView = layoutManager.findViewByPosition(lastVisibleItemPosition)
            val bottomOffset = lastItemView?.bottom ?: 0 - recyclerView.height
            bottomOffset <= 0 && lastVisibleItemPosition == totalItemCount - 1
        }
    }

    /**
     * 显示图片放大预览对话框
     */
    @SuppressLint("MissingInflatedId")
    private fun showImagePreviewDialog(imageUrl: String) {
        binding.imageLineHorScroll.visibility  = View.GONE
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_preview, null)
        val pvPreview = dialogView.findViewById<PhotoView>(R.id.pv_preview)

        // 使用 Glide 加载大图到 PhotoView（支持缩放）
        Glide.with(this)
            .load(imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
            .error(android.R.drawable.stat_notify_error) // 加载失败占位图
            .into(pvPreview)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("关闭") { dialog, _ -> dialog.dismiss()
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.e("ceshi","图片列表：${imageUrlLocalList.size}")
                    //binding.imageLineHorScroll.visibility  = View.VISIBLE
//                    for (resUrl in imageUrlLocalList.values) {
//                        resUrl.let {
//                            addNewImageView(it!!)
//                        }
//
//                    }
                    //binding.imageLineHorScroll.visibility = View.VISIBLE
                    rebuildImageContainer()
                }

            }
            .show()
    }

    // 调用相机
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // 确保有相机应用可以处理该Intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // 创建临时文件保存照片
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // 处理创建文件失败的情况
                    ex.printStackTrace()
                    null
                }
                // 继续只有在成功创建文件的情况下
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        getFileProviderAuthority(this), // 替换为你的FileProvider authority
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
                }
            }
        }
    }

    // 根据当前环境动态生成 authority
    fun getFileProviderAuthority(context: Context): String {
        return "${context.packageName}.fileprovider"
    }

    // 创建临时图片文件
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 创建唯一文件名
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* 前缀 */
            ".jpg", /* 后缀 */
            storageDir /* 目录 */
        ).apply {
            // 保存文件路径用于后续使用
            currentPhotoPath = absolutePath
        }

    }

    // 将图片添加到系统图库
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }


    private fun showPicturePickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_picture_picker)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        dialog.findViewById<LinearLayout>(R.id.PictureLine).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
            dialog.dismiss()

        }

        dialog.findViewById<LinearLayout>(R.id.CameraLine).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            dispatchTakePictureIntent()
            dialog.dismiss()
        }


        dialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            dialog.dismiss()
            binding.profileImage.setImageResource(R.drawable.icon_profile2)
            binding.profileImage.clearColorFilter()
            //binding.photoPreImage.visibility = View.GONE
            isProfile = false
            isPicture = false
        }

        dialog.show()
    }


    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    private fun setupTouchListener() {
        binding.chatRecyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // 获取当前焦点视图
                val view = currentFocus
                if (view is EditText) {
                    // 计算点击位置是否在EditText外部
                    val outRect = Rect()
                    view.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        // 点击在EditText外部，隐藏键盘并清除焦点
                        view.clearFocus()
                        //view.hideKeyboard()
                        hideKeyboard(view)
                    }
                }
            }
            false
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onModelTypeClick(modelType: String, mServiceProvider:String) {

        Log.e("ceshi","聊天服务商：$mServiceProvider,,$CUSTOMIZE_URL_TWO,,$BASE_URL")
        if (modelType == "testApiKey"){

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

    private fun scrollToBottom() {
        if (messageList.isNotEmpty()) {
            binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    private fun getModelList(){
        val job2 = lifecycleScope.launch(Dispatchers.IO) {
            // 读取 JSON 文件
            val jsonBeat = loadJSONFromAsset("models.json",this@ChatActivity)
            if (jsonBeat != null) {
                try {
                    val json = JSONObject(jsonBeat).getString("data")
                    // 解析 JSON 数据
                    val jsonArray = JSONArray(json)
                    val result = StringBuilder()
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val name = jsonObject.getString("name")
                        val id = jsonObject.getString("id")
                        val resultGroup = StringBuilder()
//                        Log.e("ceshi","这里的值：${name}")
//                        Log.e("ceshi","0这里的值：${id}")
                        modelList.add(id)


                    }
                    isTrueApiKey = true
                    setupPopupWindow(modelList)

                    // 显示结果

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }



        lifecycleScope.launch(Dispatchers.Main) {
            job2.join() // 等待数据库操作完成



        }
    }

    override fun onPause() {
        super.onPause()
        Log.e("ceshi","onPause")

        // 判断是否因屏幕关闭触发 onPause
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        isScreenTurnedOff = !pm.isInteractive


        Log.e("ceshi","onPause状态屏幕$isScreenTurnedOff")
    }


    private fun addNewImageView(imageUrlLocal:String) {
        Log.e("ceshi","添加视图")
        binding.imageLineHorScroll.visibility = View.VISIBLE
        imageUrlLocalList.add(imageUrlLocal)
        mImageUrlLocalList.add(imageUrlLocal)
        val removableLayout = RemovableImageLayout(this,listenerOver=this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 4.dpToPx()
            }
        }

        // 设置图片（示例：加载随机图片）
        removableLayout.setImageResource(imageUrlLocal,imageCounter)

        // 添加到容器
        binding.imageLine.addView(removableLayout)
        imageCounter++

    }

    private fun addNewImageViewShow(imageUrlLocal:String,count:Int) {
        Log.e("ceshi","0添加视图")
        binding.imageLineHorScroll.visibility = View.VISIBLE
        val removableLayout = RemovableImageLayout(this,listenerOver=this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 4.dpToPx()
            }
        }

        // 设置图片（示例：加载随机图片）
        removableLayout.setImageResource(imageUrlLocal,count)

        // 添加到容器
        binding.imageLine.addView(removableLayout)


    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun initCueWord(){
        lifecycleScope.launch(Dispatchers.IO) {
            val readCueWords = dataStoreManager.readCueWords.first()?: "你好，有什么问题都可以问我。"
            readCueWords?.let {
                Log.e("ceshi","readCueWords：$it,,${readCueWords == ""}")

                val readCueWordsSwitch = dataStoreManager.readCueWordsSwitch.first()
                readCueWordsSwitch?.let {
                    if (readCueWordsSwitch){
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (readCueWords == ""){
                                cueWordStr = "你好，有什么问题都可以问我。"
                            }else{
                                cueWordStr = readCueWords
                            }
                            if (messageList.isNotEmpty() && !chatType.contains("[")) {
                                messageList[0] = cueWordStr
                                // 通知适配器数据已更改
                                adapter.notifyDataSetChanged()
                            }
                            if (!chatType.contains("[")){
                                chatPrompt = cueWordStr
                            }

                        }
                    }else{
                        lifecycleScope.launch(Dispatchers.Main) {
                            cueWordStr = "这是删除过的内容变为空白"
                            if (messageList.isNotEmpty() && !chatType.contains("[")) {
                                messageList[0] = cueWordStr
                                // 通知适配器数据已更改
                                adapter.notifyDataSetChanged()
                            }
                            if (!chatType.contains("[")){
                                //chatPrompt = cueWordStr
                            }
                        }

                    }
                }

            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.e("ceshi","拦截返回")
        finishAffinity() // 关闭当前任务栈中的所有Activity
    }

    private fun initData(){

        val job2 = lifecycleScope.launch(Dispatchers.IO) {
            val chatItemHistory = chatDatabase.chatDao().getLastChatItem()

            if (chatItemHistory != null){
                isFirstInitData = true
                Log.e("ceshi","Received chatItemHistory item: ${chatItemHistory.messages}")
                messageList = chatItemHistory.messages
                chatTime = chatItemHistory.time
                messageTimesList = chatItemHistory.messagesTimes
                modelType = chatItemHistory.modelType
                priModelType = chatItemHistory.modelType
                chatPrompt = chatItemHistory.chatPrompt
                displayUrl = chatItemHistory.displayUrl
                isDeepThink = chatItemHistory.isDeepThink
                isNetWorkThink = chatItemHistory.isNetWorkThink
                userId = chatItemHistory.userId
                isEye = chatItemHistory.isEye
                chatType = chatItemHistory.title
                binding.selectedTextView.text = modelType
            }


        }



        lifecycleScope.launch(Dispatchers.Main) {
            job2.join() // 等待数据库操作完成
            Log.e("ceshi","数据刷新")
//            adapter.notifyDataSetChanged()
            // 设置适配器
            adapter = ChatAdapter(messageList,this@ChatActivity,this@ChatActivity,dataStoreManager,messageTimesList,displayUrl)
            binding.chatRecyclerView.adapter = adapter
            //adjustRecyclerViewHeight()
//        // 监听数据变化动态调整高度
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    Log.e("ceshi","数据变化${calculateRecyclerViewTotalHeight(binding.chatRecyclerView)},,${ScreenUtils.getScreenHeight(this@ChatActivity)/2}")
                    //adjustRecyclerViewHeight()
                    //binding.scroll3.fullScroll(ScrollView.FOCUS_DOWN)//自动下滑scrollview视图
//                binding.scroll3.post {
//                    binding.scroll3.scrollToBottom()
//                    //binding.scroll3.fullScroll(ScrollView.FOCUS_DOWN)//自动下滑scrollview视图
//                }
//                binding.chatRecyclerView.post {
//                    binding.chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
//                }
                    val layoutManager = binding.chatRecyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    Log.e("ceshi","屏幕高度：${getScreenHeight(this@ChatActivity)/2},,$totalItemCount,,$lastVisibleItemPosition")
                    Log.e("ceshi","recycle屏幕高度：${layoutManager.height}")
                    binding.chatRecyclerView.postDelayed({
                        binding.chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
                    }, 1000)
                    if (totalItemCount > 0 && totalItemCount>=4 && ScreenUtils.getScreenHeight(this@ChatActivity)/2 < calculateRecyclerViewTotalHeight(binding.chatRecyclerView)){

                        lifecycleScope.launch(Dispatchers.Main) {
                            (binding.chatRecyclerView.layoutManager as LinearLayoutManager).stackFromEnd = true
                        }
                    }

                }
            })
        }


    }


    private fun rebuildImageContainer() {
        // 清空现有视图
        binding.imageLine.removeAllViews()

        var count = 0
        // 重新添加所有图片
        for (imageUrl in imageUrlLocalList) {
            addNewImageViewShow(imageUrl,count)
            count++
        }

        // 显示容器
        binding.imageLineHorScroll.visibility = View.VISIBLE
    }

    private fun initCueWords(){
        lifecycleScope.launch(Dispatchers.IO) {
            val readCueWords = dataStoreManager.readCueWords.first()?: "你好，有什么问题都可以问我。"
            readCueWords?.let {
                Log.e("ceshi","readCueWords：$it,,${readCueWords == ""}")

                val readCueWordsSwitch = dataStoreManager.readCueWordsSwitch.first()
                readCueWordsSwitch?.let {
                    isPrompt = it
                    if (readCueWordsSwitch){
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (readCueWords == ""){
                                cueWordStr = "你好，有什么问题都可以问我。"
                            }else{
                                cueWordStr = readCueWords
                            }
                            if (messageList.isNotEmpty() && !chatType.contains("[")) {
                                messageList[0] = cueWordStr
                                // 通知适配器数据已更改
                                adapter.notifyDataSetChanged()
                            }
                            if (!chatType.contains("[")){
                                chatPrompt = cueWordStr
                            }

                        }
                    }else{
                        lifecycleScope.launch(Dispatchers.Main) {
                            cueWordStr = "这是删除过的内容变为空白"
                            if (messageList.isNotEmpty() && !chatType.contains("[")) {
                                messageList[0] = cueWordStr
                                // 通知适配器数据已更改
                                adapter.notifyDataSetChanged()
                            }
                            if (!chatType.contains("[")){
                                //chatPrompt = cueWordStr
                            }
                        }

                    }
                }

            }
        }
    }

    private fun getChatTitle(message:String):String{
        return "根据用户输入的内容生成一个会话标题，长度适中，简洁明了，要求是会话标题需要涵盖内容的重点。输入内容：<text>$message</text>始终以中文纯文本格式直接返回标题，不要添加任何其他内容。将标题控制在6个汉字以内，不要超过这个限制。"
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
                            Toast.makeText(this@ChatActivity, "登录成功，谢谢", Toast.LENGTH_SHORT).show()
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