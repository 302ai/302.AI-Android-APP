package com.ai302.app.adapter

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/3/21
 * desc   :
 * version: 1.0
 */
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.PrecomputedText.Params
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.ai302.app.R
import com.ai302.app.data.BackChatToolItem
import com.ai302.app.datastore.DataStoreManager
import com.ai302.app.infa.OnCueWordLeftItemClickListener
import com.ai302.app.infa.OnWordPrintOverClickListener
import com.ai302.app.plugin.CodeCopyPlugin
import com.ai302.app.utils.CustomToast
import com.ai302.app.utils.CustomUrlSpan
import com.ai302.app.utils.ScreenUtils
import com.ai302.app.utils.StringObjectUtils.convertLatexFormat

import com.ai302.app.utils.StringObjectUtils.extractAllUrls
import com.ai302.app.utils.StringObjectUtils.extractUrl
import com.ai302.app.utils.StringObjectUtils.processFormulas
import com.ai302.app.utils.SystemUtils
import com.ai302.app.utils.TimeUtils
import com.ai302.app.utils.ViewAnimationUtils
import com.ai302.app.view.ThreeCircularLoadingAnim
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.spans.LinkSpan
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Link
import org.commonmark.parser.Parser
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.latex.JLatexMathTheme
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import kotlin.math.E
import kotlin.math.abs

class ChatAdapter(private val messageList: List<String>,private val listenerOver: OnWordPrintOverClickListener,private val context: Context,private val dataStoreManager: DataStoreManager,private val messageTimesList: List<String>,private val displayUrl:String) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    // 用于标记每条消息是否已经加载过
    private val loadedMessages = mutableSetOf<String>()
    private var counter = 0
    // 状态存储集合（使用LinkedHashMap保持顺序）
    private val messageStates = LinkedHashMap<String, MessageState>()
    private var isStop = false
    private var isChatNew = false
    private var pictureUrl = ""
    private var pictureNumber = 0
    private var isPicture = false

    private var picturePosition = mutableSetOf<Int>()
    private var nowPosition = 0
    private var buttonStop = false
    private var chatType = 0
    private var deepThinkingTime = 0

    private val handler = Handler(Looper.getMainLooper())
    private val handlerDeep = Handler(Looper.getMainLooper())
    private var currentLength = 0 // 当前已显示的字符数
    private var currentLengthDeep = 0 // 当前已显示的字符数
    private var fullSpannable: Spannable? = null // 完整解析后的 Markdown 内容（带样式）
    private var fullSpannableDeep: Spannable? = null // 完整解析后的 Markdown 内容（带样式）

    private var scaleAnimator: AnimatorSet? = null
    private var scaleAnimation: ValueAnimator? = null

    private var chatItemOnClick = false

    private var clearNumber = 0

    private var clearNumbers = mutableSetOf<Int>()
    private var preSetWordNumbers = mutableSetOf<Int>()

    private var modelType = ""
    private var modelTypePosition = 0

    private var hashMapModelType = HashMap<Int, String>()

    private var isClearText = true

    private var isOpenPreEye = false


    // 定义消息类型常量
    private companion object {
        const val TYPE_USER = 0
        const val TYPE_ROBOT = 1
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deepTextView: TextView = itemView.findViewById(R.id.text1)
        //val webView:WebView = itemView.findViewById<WebView>(R.id.webView1)
        val timeText:TextView = itemView.findViewById<TextView>(R.id.text2)
        val deepLine:LinearLayout = itemView.findViewById(R.id.deepLine)
        val deepThinkTime: TextView = itemView.findViewById(R.id.deepThinkTime)
        val chatToolLine:LinearLayout = itemView.findViewById(R.id.chatToolLine)
        val copyLine:LinearLayout = itemView.findViewById(R.id.copyLine)
        val eyePreLine:LinearLayout = itemView.findViewById(R.id.eyePreLine)
        val againLine:LinearLayout = itemView.findViewById(R.id.againLine)
        val fixedLine:LinearLayout = itemView.findViewById(R.id.fixedLine)
        val deleteLine:LinearLayout = itemView.findViewById(R.id.deleteLine)
        val broadcastLine:LinearLayout = itemView.findViewById(R.id.broadcastLine)
        val chatItemLayout:ConstraintLayout = itemView.findViewById(R.id.chat_item_layout)

        val selectModelTypeTv:TextView = itemView.findViewById<TextView>(R.id.selectModelTypeTv)

        val preCons:ConstraintLayout = itemView.findViewById(R.id.preCons)

        //var webSettings: WebSettings = webView.settings
        var currentMessage: String? = null

        val messageText:TextView = itemView.findViewById<TextView>(R.id.messageTv)
        val loadingThreeLine:LinearLayout = itemView.findViewById(R.id.loadingThreeLine)
        val loadThreeImage: ThreeCircularLoadingAnim = itemView.findViewById<ThreeCircularLoadingAnim>(R.id.loadThreeImage)

        val clearViewCon:ConstraintLayout = itemView.findViewById(R.id.clearViewCon)

        val profileImage:TextView = itemView.findViewById(R.id.image1)

        val profileImage1:ImageView = itemView.findViewById(R.id.image2)

        val msgLine:LinearLayout = itemView.findViewById(R.id.msgLine)

        // 清理资源方法
        fun clear() {
            currentMessage?.let { message ->
                messageStates[message]?.job?.cancel()
            }
            //webView.loadUrl("about:blank")
            // 销毁时停止动画，避免内存泄漏
            handler.removeCallbacksAndMessages (null)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (isEven(position)) {
            TYPE_ROBOT
        } else {
            TYPE_USER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutRes = when (viewType) {
            TYPE_USER -> R.layout.me_list_item
            TYPE_ROBOT -> R.layout.robot_list_item // 可替换为右侧对齐的布局
            else -> throw IllegalArgumentException("Invalid view type")
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
        return ChatViewHolder(view)
    }

    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messageList[position]
        //val message = ScreenUtils.markdownToHtml(message1)

        // 初始化消息状态（如果是新消息）
        if (!messageStates.containsKey(message)) {
            messageStates[message] = MessageState(message)
        }

        holder.chatToolLine.visibility = View.VISIBLE
        // 获取TextView的布局参数
        /*var layoutParams = holder.timeText.layoutParams as ConstraintLayout.LayoutParams
        if (message.length<5 && isEven(position)){
            layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
        }else if (message.length>5 && isEven(position)){
            layoutParams.rightToRight = holder.messageText.id
        }
        // 应用布局参数更改
        holder.timeText.layoutParams = layoutParams*/

        if (!isEven(position)){
            /*CoroutineScope(Dispatchers.IO).launch {
                val data = dataStoreManager.readImageUrl.first()
                data?.let {
                    CoroutineScope(Dispatchers.Main).launch {
                        // 方法1：使用内置的CircleCrop变换
                        Glide.with(context)
                            .load(it)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.stat_notify_error)
                            .into(holder.profileImage)
                    }

                }
            }*/
            CoroutineScope(Dispatchers.IO).launch {
                val readAppEmojisData = dataStoreManager.readAppEmojisData.first()
                readAppEmojisData?.let {
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.profileImage.text = readAppEmojisData

                    }

                }
            }
        }else{

            if (displayUrl != ""){
                CoroutineScope(Dispatchers.Main).launch {
                    holder.profileImage.setBackgroundResource(R.drawable.shape_select_site_bg_write)
                    holder.profileImage1.visibility = View.VISIBLE
                    // 方法1：使用内置的CircleCrop变换
                    Glide.with(context)
                        .load(displayUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(holder.profileImage1)
                }
            }


        }


        // 初始化状态（如果不存在）
//        if (messageStates.size == null) {
//            messageStates.put(position.toString(), MessageState(message).apply {
//                // 只有最新消息需要动画
//                displayedText = if (isNewMessage(position)) "" else message
//            })
//        }
//
        val state = messageStates[message]!!

        if (message=="这是删除过的内容变为空白"){
            holder.chatItemLayout.visibility = View.GONE
            holder.chatItemLayout.layoutParams = holder.chatItemLayout.layoutParams.apply {
                height = 0 // 关键：设置高度为0
            }
        }else{
            holder.chatItemLayout.visibility = View.VISIBLE
            holder.chatItemLayout.layoutParams = holder.chatItemLayout.layoutParams.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        /*if (message.contains("```")){
            holder.preCons.visibility = View.VISIBLE
        }else{
            holder.preCons.visibility = View.GONE
        }*/

        /*if (position==0){
            holder.timeText.text = "预设提示词"
        }else{
            holder.timeText.text = TimeUtils.getCurrentDateTime()
        }*/

        holder.timeText.text = messageTimesList[position]

        //Log.e("ceshi","提示$position,,$modelTypePosition,,$modelType")
        holder.selectModelTypeTv.visibility = View.GONE
        hashMapModelType.forEach { (key, value) ->
            Log.e("ceshi","hashMapModelType$key,,$value")
            if (position != 0 && position == key){
                holder.selectModelTypeTv.text = value
                holder.selectModelTypeTv.visibility = View.VISIBLE
            }
        }


        holder.preCons.setOnClickListener {
            listenerOver.onBackChatTool(BackChatToolItem("pre",message,position))
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(holder.preCons)
            //Toast.makeText(context, "预览", Toast.LENGTH_SHORT).show()
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = "预览",
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }

        holder.copyLine.setOnClickListener {
            SystemUtils.copyTextToClipboard(context,message)
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(holder.copyLine)
            //Toast.makeText(context, "复制", Toast.LENGTH_SHORT).show()
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = "复制",
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }

        holder.eyePreLine.setOnClickListener {
            SystemUtils.copyTextToClipboard(context,message)
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(holder.eyePreLine)
            //Toast.makeText(context, "复制", Toast.LENGTH_SHORT).show()
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = "预览",
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
            listenerOver.onBackChatTool(BackChatToolItem("codePre",message,position))
        }

        if (isOpenPreEye && isEven(position)){
            holder.eyePreLine.visibility = View.VISIBLE
        }else{
            holder.eyePreLine.visibility = View.GONE
        }

        holder.deleteLine.setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(holder.deleteLine)

            if (messageTimesList[position]=="预设提示词"){
                //Toast.makeText(context, "删除", Toast.LENGTH_SHORT).show()
                // 使用自定义 Toast
                CustomToast.makeText(
                    context = context,
                    message = "预设提示词不可删除！",
                    duration = Toast.LENGTH_SHORT,
                    gravity = Gravity.CENTER
                ).show()
            }else{
                listenerOver.onBackChatTool(BackChatToolItem("delete",message,position))

                //Toast.makeText(context, "删除", Toast.LENGTH_SHORT).show()
                // 使用自定义 Toast
                CustomToast.makeText(
                    context = context,
                    message = "删除",
                    duration = Toast.LENGTH_SHORT,
                    gravity = Gravity.CENTER
                ).show()
            }


        }

        holder.againLine.setOnClickListener {

            listenerOver.onBackChatTool(BackChatToolItem("again",message,position))
            buttonStop = false
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(holder.againLine)
            //Toast.makeText(context, "重试", Toast.LENGTH_SHORT).show()
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = "重试",
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }

        holder.fixedLine.setOnClickListener {

            listenerOver.onBackChatTool(BackChatToolItem("fixed",message,position))
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(holder.fixedLine)
            //Toast.makeText(context, "固定", Toast.LENGTH_SHORT).show()
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = "固定",
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }

        holder.broadcastLine.setOnClickListener {

            listenerOver.onBackChatTool(BackChatToolItem("broadcast",message,position))
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(holder.broadcastLine)
            //Toast.makeText(context, "播放", Toast.LENGTH_SHORT).show()
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = "播放",
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }

        //holder.loadThreeImage.setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_IN)

        var deepLineOnClick = false
        //Log.e("ceshi","深度思考显示：${isEven(position)},,,$isChatNew")
        if (isEven(position)&&message.contains("&&&&&&")){
            holder.deepLine.visibility = View.VISIBLE
            holder.deepLine.setOnClickListener {
                if (!deepLineOnClick){
                    holder.deepTextView.visibility = View.VISIBLE
                    deepLineOnClick = true
                }else{
                    holder.deepTextView.visibility = View.GONE
                    deepLineOnClick = false
                }

            }
        }else{
            holder.deepLine.visibility = View.GONE
        }

        if (!isClearText){
            holder.clearViewCon.visibility = View.GONE
        }else{
            for (mClearNumber in clearNumbers){
                if (position == mClearNumber){
                    holder.clearViewCon.visibility = View.VISIBLE
                }else{
                    holder.clearViewCon.visibility = View.GONE
                }
            }
        }

        holder.clearViewCon.setOnClickListener {
            listenerOver.onBackChatTool(BackChatToolItem("cancelClearContext",message,position))
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(holder.clearViewCon)
            //Toast.makeText(context, "播放", Toast.LENGTH_SHORT).show()
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = "上下文清除已经取消",
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }




        //holder.webView.isClickable = true
        //Log.e("ceshi","0点击$chatItemOnClick")

//        holder.chatItemLayout.setOnClickListener {
//            Log.e("ceshi","1点击$chatItemOnClick")
//            if (!chatItemOnClick){
//                holder.chatToolLine.visibility = View.VISIBLE
//                chatItemOnClick = true
//            }else{
//                holder.chatToolLine.visibility = View.GONE
//                chatItemOnClick = false
//            }
//        }

//        holder.webView.setOnClickListener {
//            Log.e("ceshi","0点击$chatItemOnClick")
//            if (!chatItemOnClick){
//                holder.chatToolLine.visibility = View.VISIBLE
//                chatItemOnClick = true
//            }else{
//                holder.chatToolLine.visibility = View.GONE
//                chatItemOnClick = false
//            }
//        }

        // 若需要 ItemView 的点击事件，需确保不拦截子 View 的事件
        //holder.itemView.isClickable = false  // 让子 View 优先处理点击

        /*holder.chatItemLayout.setOnClickListener {
            Log.e("ceshi", "0WebView抬起事件${holder.chatToolLine.visibility == View.VISIBLE}")
        }*/

//        holder.messageText.setOnClickListener {
//            Log.e("ceshi", "WebView抬起事件${holder.chatToolLine.visibility == View.VISIBLE}")
////            if (!chatItemOnClick){
////                holder.chatToolLine.visibility = View.VISIBLE
////                chatItemOnClick = true
////            }else{
////                holder.chatToolLine.visibility = View.GONE
////                chatItemOnClick = false
////            }
//            if (holder.chatToolLine.visibility == View.VISIBLE){
//                holder.chatToolLine.visibility = View.GONE
//            }else{
//                holder.chatToolLine.visibility = View.VISIBLE
//            }
//        }


        /*holder.messageText.setOnTouchListener(View.OnTouchListener { v, event ->
            Log.e("ceshi", "WebView按下事件0")
            // 处理触摸事件，例如记录点击位置等
            false // 返回false表示事件没有被完全消费，可以继续传递到WebView内部处理（如点击链接）
            // 只处理按下（ACTION_DOWN）或抬起（ACTION_UP）事件（根据需求选择）
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 按下时记录日志（仅触发1次）
                    Log.e("ceshi", "WebView按下事件")
//                    if (!chatItemOnClick){
//                        holder.chatToolLine.visibility = View.VISIBLE
//                        chatItemOnClick = true
//                    }else{
//                        holder.chatToolLine.visibility = View.GONE
//                        chatItemOnClick = false
//                    }
                    false  // 让事件继续传递给WebView内部处理
                }

                MotionEvent.ACTION_UP -> {
                    // 抬起时记录日志（仅触发1次）
                    Log.e("ceshi", "WebView抬起事件")
                    // 关键：启动延迟过渡动画（自动处理布局变化）
//                    TransitionManager.beginDelayedTransition(
//                        holder.chatToolLine,
//                        TransitionInflater.from(context).inflateTransition(R.transition.auto_transition)
//                    )

                    if (holder.chatToolLine.visibility == View.VISIBLE){
                        // 隐藏时执行动画
                        ViewAnimationUtils.hideWithAnimation(holder.chatToolLine)
                        holder.chatToolLine.visibility = View.GONE
                    }else{
                        ViewAnimationUtils.showWithAnimation(holder.chatToolLine)
                        holder.chatToolLine.visibility = View.VISIBLE
                    }
                    false  // 让事件继续传递给WebView内部处理
                }

                else -> {
                    // 其他事件（如ACTION_MOVE）不处理
                    false
                }
            }
        })*/


        /*holder.webView.setOnTouchListener(View.OnTouchListener { v, event ->
            // 处理触摸事件，例如记录点击位置等
            false // 返回false表示事件没有被完全消费，可以继续传递到WebView内部处理（如点击链接）
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
                    if (!chatItemOnClick){
                        holder.chatToolLine.visibility = View.VISIBLE
                        chatItemOnClick = true
                    }else{
                        holder.chatToolLine.visibility = View.GONE
                        chatItemOnClick = false
                    }
                    false  // 让事件继续传递给WebView内部处理
                }

                else -> {
                    // 其他事件（如ACTION_MOVE）不处理
                    false
                }
            }
        })

        holder.webSettings.javaScriptEnabled = true
        holder.webSettings.domStorageEnabled = true
        //holder.webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        // 设置缓存模式为优先使用缓存，缓存不存在时从网络获取
        holder.webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        //holder.webView.setBackgroundColor(R.color.BgChat)
        //holder.webView.setBackgroundResource(R.drawable.shape_chat_site_bg_gray_line)

        holder.webView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                holder.webView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                holder.webView.setBackgroundColor(Color.TRANSPARENT)
                holder.webView.setBackgroundResource(R.drawable.shape_chat_site_bg_gray_line)
            }
        })

        // 监听加载进度（控制动画）
//        holder.webView.webChromeClient = object : WebChromeClient() {
//            override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                super.onProgressChanged(view, newProgress)
//                // 进度 0-100，进度 < 100 时显示加载动画
//                holder.loadLine.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
//            }
//        }

        holder.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                view.evaluateJavascript("initRender()", null)
                //holder.loadLine.visibility = View.GONE // 加载完成后隐藏动画
                // 获取内容宽度
                val contentWidthStr = getTextWidth(message,sp2px(context,16f))
                val isLess = ScreenUtils.isTextWidthLessThanScreen(context, message, sp2px(context,16f))
                Log.e("ceshi","聊天页面宽度是：${contentWidthStr.toInt()},,$isLess")
                // 更新 WebView 高度（主线程）
                if (isLess){
                    holder.webView.post {
                        val layoutParams = holder.webView.layoutParams.apply {
                            width = contentWidthStr.toInt()  + 100
                        }
                        holder.webView.layoutParams = layoutParams
                    }
                }else{
                    holder.webView.post {
                        val layoutParams = holder.webView.layoutParams.apply {
                            width = ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                        holder.webView.layoutParams = layoutParams
                    }
                }

                /*view?.evaluateJavascript("document.getElementById('content').scrollWidth") { widthStr ->
                    Log.e("ceshi","0聊天页面宽度是：${"widthStr.toInt()"},,${widthStr.isNullOrEmpty()}")
                    if (!widthStr.isNullOrEmpty()){
                        val contentWidthStr = widthStr.removeSurrounding("\"").toInt()

                        // 更新 WebView 高度（主线程）
                        holder.webView.post {
                            val layoutParams = holder.webView.layoutParams.apply {
                                width = contentWidthStr
                                Log.e("ceshi","聊天页面宽度是：${contentWidthStr.toInt()}")
                            }
                            holder.webView.layoutParams = layoutParams
                        }
                    }

                }*/


                // 页面加载完成后启动动画（仅对新消息）
                Log.e("ceshi","position$position")
                Log.e("ceshi","messageList.size${messageList.size}")
                Log.e("ceshi","两个开关：${state.isAnimating},${isStop},${state.displayedText.isEmpty()},${isChatNew}")
                if (isEven(position) && isNewMessage(position) && !state.isAnimating && !isStop &&isChatNew) {
                    startTypewriterEffect(holder, state)
                }
                // 页面加载完成后，标记该消息已加载
                Log.e("ceshi","添加到loadedMessages的信息：$message")
                loadedMessages.add(message)
                isStop = false
            }

        }*/


        fitterMarkWon(holder,state,context,position)

        // 处理消息内容，添加 MathJax 配置

        var htmlContent = ""

        if (!isEven(messageList.indexOf(message))) {
            htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        direction: rtl;
                    }
                </style>
                <script type="text/javascript" async
                        src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML">
                </script>
            </head>
            <body>
                ${message}
            </body>
            </html>
        """.trimIndent()
//            holder.imageLoad.visibility = View.VISIBLE
//            holder.imageLoading.visibility = View.VISIBLE
        }else{
//            holder.imageLoad.visibility = View.GONE
//            holder.imageLoading.visibility = View.GONE
            htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <script type="text/javascript" async
                        src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML">
                </script>
            </head>
            <body>
                ${message}
            </body>
            </html>
        """.trimIndent()
        }
        //Log.e("ceshi","是否已经加载${loadedMessages.contains(message)},${message.contains("\\")}")
        //Log.e("ceshi","是否包括深度思考${message.contains("&&&&&&")},${isNewMessage(position)}")
        if (message.contains("file:///android_asset/loading.html")){
            holder.loadingThreeLine.visibility = View.VISIBLE
            holder.messageText.visibility = View.GONE
            //startIconScaleAnimation(holder)
            holder.loadThreeImage.startAnim()
        }else{
            holder.loadThreeImage.stopAnim()
            holder.loadingThreeLine.visibility = View.GONE
            holder.messageText.visibility = View.VISIBLE
            scaleAnimation?.cancel() // 销毁时停止动画
        }
        // 检查该消息是否已经加载过
        /*if (message.contains("file:///android_asset/loading.html")){
            holder.webView.loadUrl("file:///android_asset/loading.html")
        }else{
            if (!loadedMessages.contains(message) || !message.contains("\\")) {
                //holder.webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
                state.displayedText = message
                state.isAnimating = false
                // 旧消息：直接渲染完整内容
                if (state.displayedText.contains("&&&&&&") && (loadedMessages.contains(message) || !isNewMessage(position) || !isChatNew)){
                    val parts = state.displayedText.split("&&&&&&")
                    val firstPart = parts[0]
                    val secondPart = parts[1]
                    holder.deepTextView.text = firstPart
                    holder.deepThinkTime.text = "思考中 (${firstPart.length*5/1000}s)"
                    holder.webView.loadDataWithBaseURL(null, getFullHtml(secondPart), "text/html", "utf-8", null)
                }else{
                    holder.webView.loadDataWithBaseURL(null, getFullHtml(state.displayedText), "text/html", "utf-8", null)
                }

            }else{
                // 新消息：加载空白页面准备动画
                holder.webView.loadDataWithBaseURL(null, getBaseHtml(""), "text/html", "utf-8", null)
            }
        }*/

        //holder.messageTextView.text = message

        // 判断是否是新消息
//        if (position == messageList.lastIndex && state.displayedText.isEmpty()) {
//            // 新消息：加载空白页面准备动画
//            holder.webView.loadDataWithBaseURL(null, getBaseHtml(""), "text/html", "utf-8", null)
//        } else {
//            // 旧消息：直接渲染完整内容
//            holder.webView.loadDataWithBaseURL(null, getFullHtml(state.displayedText), "text/html", "utf-8", null)
//        }



        /*if (!isEven(messageList.indexOf(message))) {
            holder.webView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        } else {
            holder.webView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        }*/
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun isEven(number: Int): Boolean {
        return number % 2 == 0
    }

    //逐字显示效果
    // 状态管理类
    inner class MessageState(val content: String) {
        var isAnimating = false  // 是否正在动画中
        var displayedText = ""   // 已显示文本
        var job: Job? = null     // 关联的协程任务
    }

    // 启动打字机效果
    /*private fun startTypewriterEffect(holder: ChatViewHolder, state: MessageState) {
        state.job?.cancel() // 取消已有任务

        state.job = holder.itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            state.isAnimating = true
            val fullText = state.content
            val speed = 50L // 打字速度
            isStop = false
//            for (i in 0..fullText.length) {
//                if (!holder.webView.isAttachedToWindow) break // 防止View被回收后继续执行
//
//                val partialText = fullText.substring(0, i)
//                state.displayedText = partialText
//
//                // 更新WebView内容
//                holder.webView.evaluateJavascript(
//                    "updateContent('${escapeText(partialText)}')",
//                    null
//                )
//
//                delay(speed)
//            }
            if (fullText.contains("&&&&&&")){
                val startTime = System.currentTimeMillis()
                // 根据 "&&&" 分割字符串
                val parts = fullText.split("&&&&&&", limit = 2)

                var htmlContent = fullText

                // 打印第一部分
                for ((index,part) in parts.withIndex()) {
                    holder.deepTextView.text = parts[0]
                    for (i in 0..part.length) {
                        //if (!holder.webView.isAttachedToWindow) break // 防止View被回收后继续执行

                        if (buttonStop){
                            break
                        }

                        val partialText = part.substring(0, i)
                        state.displayedText = partialText

                        if (index == 0) {
                            // 第一部分，设置字体大小和颜色
                            Log.e("ceshi","颜色调整")
                            val escapedText = escapeJavaScriptString(ScreenUtils.markdownToHtml(escapeText(partialText)))
                            htmlContent = "<span style='font-size: 12px; color: gray;'>${escapeText1(escapedText)}</span>"
                        } else {
                            val endTime = System.currentTimeMillis()
                            val executionTime = endTime - startTime
                            holder.deepThinkTime.text = "已深度思考 (${executionTime/1000}s)"
                            // 其他部分，正常显示
                            val escapedText = escapeJavaScriptString(ScreenUtils.markdownToHtml(escapeText(partialText)))
                            htmlContent = escapeText1(escapedText)
                        }

                        // 打印调试信息
                        //val jsCode = "updateContent('$htmlContent')"
                        val jsCode = "updateContent(\"$htmlContent\")"
                        println("Sending to JavaScript: $jsCode")

                        // 更新WebView内容
                        holder.webView.evaluateJavascript(
                            "updateContent(\"$htmlContent\")",
                            null
                        )

                        delay(speed)
                    }

                    // 如果不是最后一部分，隐藏内容
                    if (parts.indexOf(part) < parts.size - 1) {
                        // 可以通过调用 JavaScript 函数来隐藏内容
                        holder.webView.evaluateJavascript("hideContent()", null)
                        // 等待一段时间再显示下一部分
                        delay(500)
                    }
                }
            }else{
                for (i in 0..fullText.length) {
                    //if (!holder.webView.isAttachedToWindow) break // 防止View被回收后继续执行(但是如果用了这个方法会被打断，例如“朋友你好”，只打印“朋”)

                    val partialText = fullText.substring(0, i)
                    state.displayedText = partialText

                    // 打印调试信息
                    //val jsCode = "updateContent('$htmlContent')"
                    val jsCode = "updateContent(\"${escapeText(partialText)}\")"
                    println("Sending to JavaScript: $jsCode")

                    if (buttonStop){
                        break
                    }
                    // 更新WebView内容
//                    holder.webView.evaluateJavascript(
//                        "updateContent(\"${escapeText(partialText)}\")",
//                        null
//                    )


                    val escapedText = escapeJavaScriptString(ScreenUtils.markdownToHtml(escapeText(partialText)))
                    holder.webView.evaluateJavascript("updateContent(\"$escapedText\")", null)
                    //holder.webView.evaluateJavascript("updateContent(\"${ScreenUtils.markdownToHtml(escapeText(partialText))}\")", null)
                    delay(speed)

                }
            }





            isStop = true
            listenerOver.onOverItemClick(true)

            state.isAnimating = false
            //notifyItemChanged(messageList.indexOf(state.content)) // 刷新确保最终状态
        }
    }*/

    // HTML模板生成
    private fun getBaseHtml(content: String): String {
        /*return """
            <!DOCTYPE html>
            <html>
            <head>
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
                <div id="content">$content</div>
            </body>
            </html>
        """.trimIndent()*/
        val directionStyle = if (isEven(messageList.indexOf(content))) "" else "text-align: right;"
        var Style = ""
        Log.e("ceshi","这里的是：$content")
        if (content.contains("content://media")){
            var firstPart = ""
            var secondPart = ""
            val originalString = content
            val separator = "content://media"
            val parts = originalString.split(separator, limit = 2)
            if (parts.size == 2) {
                firstPart = parts[0]
                secondPart = separator + parts[1]
                println("第一部分: $firstPart")
                println("第二部分: $secondPart")
            } else {
                println("分割失败，未找到分隔符或分割结果不符合预期。")
            }

            Style = "<img src=\"$secondPart\" width=100,height=200 alt=\"Sample image\"><p>$firstPart</p>"
        }else{
            Style = "<div id=\"content\">${ScreenUtils.markdownToHtml(content)}</div>"
        }

        Log.e("ceshi","0图片地址$Style,,$pictureNumber")

        var htmlContent = ""
        htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        $directionStyle
                    }
                </style>
                
                 <script>
                    function updateContent(text) {
                    console.log('Received 哈哈哈content:', text);
                        document.getElementById('content').innerHTML = text;
                        MathJax.Hub.Queue(['Typeset', MathJax.Hub]);
                    }
                    
                </script>
                
                   <!-- 代码高亮库 -->
                <link rel="stylesheet" 
                        href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.6.0/styles/default.min.css">
                <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.6.0/highlight.min.js"></script>
                
                <script type="text/javascript" async
                        src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML">
                </script>
            </head>
            <body>
                $Style
                
                  <script>
                    // 初始化代码高亮和 MathJax
                     function initRender() {
                        hljs.highlightAll(); // 高亮代码块
                        MathJax.Hub.Queue(['Typeset', MathJax.Hub]); // 渲染公式
                     }
                    
                </script>
                
            </body>
            </html>
        """.trimIndent()
        return htmlContent

    }

    private fun getFullHtml(content: String): String {
        return getBaseHtml(content)
    }

    // 转义特殊字符
    private fun escapeText(text: String): String {
        return text.replace("'", "\\'")
            .replace("\r", "\\r")
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }

    private fun escapeText1(text: String): String {
        return text.replace("'", "\\'")
            .replace("\r", "\\r")
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\\n", "<br>")
    }

    fun escapeJavaScriptString(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }

    override fun onViewRecycled(holder: ChatViewHolder) {
        super.onViewRecycled(holder)
        holder.clear() // 清理资源
    }

    // 添加新消息的方法
//    fun addNewMessage(message: String) {
//        messageList.add(message)
//        notifyItemInserted(messageList.size - 1)
//    }

    private fun isNewMessage(position: Int): Boolean {
        return position == messageList.size - 1
    }

    // 自定义更新数据的方法
    fun updateIsChat(isChat: Boolean) {
        isChatNew = isChat
    }

    fun updateIsPicture(isPictures:Boolean,mPicturePosition:Int){
        isPicture = isPictures
        picturePosition.add(mPicturePosition)
    }

    fun clearPicture(){
        picturePosition.clear()
    }

    fun setPictureNumber(mPictureNumber: Int){
        pictureNumber = mPictureNumber
    }

    fun setIsStop(newIsStop:Boolean){
        buttonStop = newIsStop
    }

    fun setModelType(modelType1:String,position: Int){
        modelType = modelType1
        modelTypePosition = position
        hashMapModelType[position] = modelType1
    }

    fun getTextWidth(text: String, textSize: Float, typeface: Typeface = Typeface.DEFAULT): Float {
        // 创建 Paint 对象
        val paint = Paint().apply {
            this.textSize = textSize  // 设置字号（单位：px）
            this.typeface = typeface  // 设置字体（可选，默认是系统字体）
            isAntiAlias = true        // 开启抗锯齿（优化显示效果）
        }
        // 测量字符串宽度（单位：px）
        return paint.measureText(text)
    }

    /**
     * 将 sp 转换为 px（Float 类型，适用于 Paint 等场景）
     */
    fun sp2px(context: Context, spValue: Float): Float {
        return spValue * context.resources.displayMetrics.scaledDensity
    }

    /**
     * 将 sp 转换为 px（Int 类型，适用于布局参数等场景）
     */
    fun sp2pxInt(context: Context, spValue: Float): Int {
        return (sp2px(context, spValue) + 0.5f).toInt()
    }

    /**
     * 将 dp 转换为 px（扩展方法，可选）
     */
    fun dp2px(context: Context, dpValue: Float): Float {
        return dpValue * context.resources.displayMetrics.density
    }



    private fun fitterMarkWon(holder: ChatViewHolder, state: MessageState,context: Context,position: Int){

        // 创建 JLatexMathTheme 来配置公式样式



        // 初始化Markwon（包含常用插件）
        val markwon = Markwon.builder(context)
            .usePlugin(CorePlugin.create()) // 核心解析插件
            .usePlugin(MarkwonInlineParserPlugin.create()) // 启用内联解析
            .usePlugin(
                JLatexMathPlugin.create(28f){
                    it.inlinesEnabled(true) // 启用行内公式
                    it.blocksEnabled(true)   // 启用块级公式
                }
            )
            //.usePlugin(CodeCopyPlugin(holder.messageText)) // 添加我们的插件
            .usePlugin(HtmlPlugin.create()) // 支持HTML标签
            .usePlugin(StrikethroughPlugin.create()) // 支持删除线
            .usePlugin(TaskListPlugin.create(context)) // 支持任务列表
            .usePlugin(TablePlugin.create(context)) // 支持表格
            .usePlugin(GlideImagesPlugin.create(context)) // 使用Glide加载图片（需添加Glide依赖）
            .usePlugin(LinkifyPlugin.create()) // 自动识别 URL 并转换为链接
            // 添加自定义插件来替换默认的URLSpan
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.linkResolver { view, url ->
                        // 自定义链接点击行为
                        if (view is TextView) {
                            // 示例：可以在这里添加链接点击统计等功能
                        }
                        // 使用默认行为打开链接
                        //android.text.util.Linkify.addLinks(view, android.text.util.Linkify.WEB_URLS)
                    }
                }

                override fun afterSetText(textView: TextView) {
                    super.afterSetText(textView)
                    // 处理文本设置后的操作，确保链接被正确应用
                    Log.e("ceshi","来到这里：${textView.text}")
                    val text = textView.text
                    if (text is Spannable) {
                        val urlSpans = text.getSpans(0, text.length, URLSpan::class.java)
                        Log.e("ceshi","0来到这里：${urlSpans.size}")

                        if (urlSpans.isEmpty()) {
                            // 尝试手动添加链接
                            Log.e("MarkwonDebug", "自定义插件: 未找到URLSpan，尝试手动添加链接")
                            android.text.util.Linkify.addLinks(textView, android.text.util.Linkify.WEB_URLS)

                            // 重新检查
                            val newUrlSpans = text.getSpans(0, text.length, URLSpan::class.java)
                            Log.e("MarkwonDebug", "自定义插件: 手动添加后URLSpan数量: ${newUrlSpans.size}")

                            for (newSpan in newUrlSpans) {
                                Log.e("MarkwonDebug", "自定义插件: 手动添加的URL: ${newSpan.url}")
                            }
                        } else {
                            for (span in urlSpans) {
                                Log.e("MarkwonDebug", "自定义插件: 找到URL: ${span.url}")
                                val start = text.getSpanStart(span)
                                val end = text.getSpanEnd(span)
                                val flags = text.getSpanFlags(span)
                                text.removeSpan(span)
                                text.setSpan(CustomUrlSpan(span.url), start, end, flags)
                            }
                        }


                    }
                }

            })
            .build()

        // 启用TextView的链接点击功能
        holder.messageText.movementMethod = android.text.method.LinkMovementMethod.getInstance()

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

        val markdown1 = """
    # 数学公式示例
    
    行内公式示例：$|a + b| \leq |a| + |b|$
    
    
    
    
    块级公式：
    $$
    \int_{0}^{\infty} e^{-x^2} dx = \frac{\sqrt{\pi}}{2}
    $$
""".trimIndent()

        val fullText = state.content.trimIndent()

        //Log.e("ceshi","来这类")

        val mFullText = convertLatexFormat(fullText)
        Log.e("ceshi","渲染字符串$fullText")
        Log.e("ceshi","渲染字符串修改后$mFullText")

        if (mFullText.contains("&&&&&&")){
            holder.deepLine.visibility = View.VISIBLE
            var firstPart = ""
            var secondPart = ""
            val originalString = mFullText
            val separator = "&&&&&&"
            val parts = originalString.split(separator, limit = 2)
            if (parts.size == 2) {
                firstPart = parts[0]
                secondPart = parts[1]
                println("第一部分: $firstPart")
                println("第二部分: $secondPart")
                holder.deepThinkTime.text = "已深度思考 (${firstPart.length*50/1000}s)"
                // 将Markdown渲染到TextView
                markwon.setMarkdown(holder.deepTextView, firstPart)
                markwon.setMarkdown(holder.messageText, secondPart)


            } else {
                println("分割失败，未找到分隔符或分割结果不符合预期。")
            }

        }else{
            holder.deepLine.visibility = View.GONE
            // 将Markdown渲染到TextView
            markwon.setMarkdown(holder.messageText, mFullText)
        }


        Log.e("ceshi","流式打印开关${isNewMessage(position)},,${!state.isAnimating},,${!isStop},,$isChatNew")
        if (isEven(position) && isNewMessage(position) && !state.isAnimating && !isStop &&isChatNew) {
            holder.chatToolLine.visibility = View.GONE

            if (mFullText.contains("&&&&&&")){
                holder.deepTextView.visibility = View.VISIBLE
                var firstPart = ""
                var secondPart = ""
                val originalString = mFullText
                val separator = "&&&&&&"
                val parts = originalString.split(separator, limit = 2)
                if (parts.size == 2) {
                    firstPart = parts[0]
                    secondPart = parts[1]
                    println("第一部分: $firstPart")
                    println("第二部分: $secondPart")


//                    fullSpannableDeep = firstPart.toSpannable() // 获取解析后的 Spannable
//                    fullSpannable = secondPart.toSpannable() // 获取解析后的 Spannable
                    fullSpannableDeep = (holder.deepTextView.text as? Spannable)!!  // 获取解析后的 Spannable
                    fullSpannable = (holder.messageText.text as? Spannable)!!  // 获取解析后的 Spannable
                    // 2. 清空 TextView，准备流式显示
                    holder.messageText.text = ""
                    currentLength = 0
                    currentLengthDeep = 0
                    // 3. 启动流式显示动画（50ms / 字符）
                    startTypingAnimation(holder=holder, isDeep = true)

                } else {
                    println("分割失败，未找到分隔符或分割结果不符合预期。")
                }

            }else{
                holder.deepTextView.visibility = View.GONE
                fullSpannable = (holder.messageText.text as? Spannable)!! // 获取解析后的 Spannable
                // 2. 清空 TextView，准备流式显示
                holder.messageText.text = ""
                currentLength = 0
                // 3. 启动流式显示动画（50ms / 字符）
                startTypingAnimation(holder=holder, isDeep = false)
            }


        }



    }

    private fun startTypingAnimation (delayPerChar: Long = 50,holder: ChatViewHolder,isDeep:Boolean) {

        if (isDeep){
            val totalLength = fullSpannableDeep?.length ?: 0
            if (totalLength == 0) return
            // 使用 Handler 定时更新显示内容
            handler.postDelayed (object : Runnable {
                override fun run () {
                    if (currentLengthDeep <= totalLength && !buttonStop) {
                        holder.deepThinkTime.text = "思考中 (${currentLengthDeep*50/1000}s)"
                        // 截取完整 Spannable 的前 currentLength 个字符（保留样式）
                        val currentText = fullSpannableDeep?.subSequence (0, currentLengthDeep)
                        holder.deepTextView.text = currentText
                        // 滚动到最新显示的位置（长文本时有用）
                        holder.deepTextView.scrollTo (0, holder.deepTextView.lineHeight * holder.deepTextView.lineCount)
                        currentLengthDeep++
                        handler.postDelayed (this, delayPerChar) // 继续下一个字符
                    }else{
                        holder.deepThinkTime.text = "已深度思考 (${totalLength*50/1000}s)"
                        val totalLength = fullSpannable?.length ?: 0
                        if (totalLength == 0) return
                        if (currentLength <= totalLength && !buttonStop) {
                            // 截取完整 Spannable 的前 currentLength 个字符（保留样式）
                            val currentText = fullSpannable?.subSequence (0, currentLength)
                            holder.messageText.text = currentText
                            // 滚动到最新显示的位置（长文本时有用）
                            holder.messageText.scrollTo (0, holder.messageText.lineHeight * holder.messageText.lineCount)
                            currentLength++
                            handler.postDelayed (this, delayPerChar) // 继续下一个字符
                        }else{
                            listenerOver.onOverItemClick(true)
                            if (!holder.messageText.text.contains("android_asset")){
                                ViewAnimationUtils.showWithAnimation(holder.chatToolLine)
                                holder.chatToolLine.visibility = View.VISIBLE
                            }

                        }
                    }
                }
            }, delayPerChar) // 初始延迟（与单个字符间隔相同）
        }else{
            val totalLength = fullSpannable?.length ?: 0
            if (totalLength == 0) return
            // 使用 Handler 定时更新显示内容
            handler.postDelayed (object : Runnable {
                override fun run () {
                    //Log.e("ceshi","总长度${totalLength},打印长度${currentLength},,$buttonStop")
                    if (currentLength <= totalLength && !buttonStop) {
                        // 截取完整 Spannable 的前 currentLength 个字符（保留样式）
                        val currentText = fullSpannable?.subSequence (0, currentLength)
                        holder.messageText.text = currentText
                        // 滚动到最新显示的位置（长文本时有用）
                        holder.messageText.scrollTo (0, holder.messageText.lineHeight * holder.messageText.lineCount)
                        currentLength++
                        handler.postDelayed (this, delayPerChar) // 继续下一个字符
                    }else{
                        Log.e("ceshi","打印结束")
                        listenerOver.onOverItemClick(true)
                        if (!holder.messageText.text.contains("android_asset")){
                            ViewAnimationUtils.showWithAnimation(holder.chatToolLine)
                            holder.chatToolLine.visibility = View.VISIBLE
                        }
                    }
                }
            }, delayPerChar) // 初始延迟（与单个字符间隔相同）
        }

    }


    private fun startIconScaleAnimation(holder:ChatViewHolder) {
//        // 定义缩放动画（1f→1.5f→1f）
//        val scaleX = ObjectAnimator.ofFloat(holder.loadThreeImage, "scaleX", 1f, 1.5f, 1f)
//        val scaleY = ObjectAnimator.ofFloat(holder.loadThreeImage, "scaleY", 1f, 1.5f, 1f)
//
//        // 组合动画并配置参数
//        scaleAnimator = AnimatorSet().apply {
//            playTogether(scaleX, scaleY) // 同时缩放X/Y轴
//            duration = 120000 // 每个周期1.2秒
//            //repeatCount = AnimatorSet.INFINITE // 无限循环
//            interpolator = AccelerateDecelerateInterpolator() // 加速减速插值器
//            start()
//        }
        //val viewToAnimate = findViewById<View>(R.id.view_to_animate) // 替换为你的视图ID
        scaleAnimation = ValueAnimator.ofFloat(1f, 0.8f, 1f) // 1f是原始大小，1.5f是放大到1.5倍，再变回1f
        scaleAnimation?.duration = 1000 // 动画持续时间，单位毫秒
        scaleAnimation?.repeatCount = ValueAnimator.INFINITE // 无限重复
        scaleAnimation?.interpolator = LinearInterpolator() // 线性插值器
        scaleAnimation?.addUpdateListener { animator ->
            val scale = animator.animatedValue as Float
            holder.loadThreeImage.scaleX = scale
            holder.loadThreeImage.scaleY = scale
        }
        scaleAnimation?.start()
    }


    /**
     * 扩展TextView，添加防重复点击监听
     * @param interval 重复点击的时间间隔阈值（毫秒），默认500ms
     * @param action 点击回调
     */
    fun TextView.setOnAntiRepeatClickListener(interval: Long = 500, action: (View) -> Unit) {
        var lastClickTime = 0L  // 记录上一次点击时间
        setOnClickListener { view ->
            val currentTime = System.currentTimeMillis()
            if (abs(currentTime - lastClickTime) > interval) {
                action(view)
                lastClickTime = currentTime  // 更新最后一次点击时间
            }
        }
    }

    fun setClearNumber(clearNumber:Int,isClearText:Boolean){
        clearNumbers.add(clearNumber)
        this.isClearText = isClearText
    }

    fun setOpenPreEye(openPreEye:Boolean){
        this.isOpenPreEye = openPreEye
    }

}