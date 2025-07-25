package com.ai302.app.ui

import ResizableDialog
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ai302.app.ChatItem
import com.ai302.app.R
import com.ai302.app.adapter.HomeMessageAdapter
import com.ai302.app.adapter.HomeMessageSearchAdapter
import com.ai302.app.databinding.ActivityChatBinding
import com.ai302.app.databinding.ActivityMainBinding
import com.ai302.app.datastore.DataStoreManager
import com.ai302.app.http.ApiService
import com.ai302.app.http.ChatCompletionRequest
import com.ai302.app.http.Message
import com.ai302.app.http.NetworkFactory
import com.ai302.app.http.RequestMessage
import com.ai302.app.infa.OnItemClickListener
import com.ai302.app.infa.OnItemSearchClickListener
import com.ai302.app.room.ChatDatabase
import com.ai302.app.room.ChatItemRoom
import com.ai302.app.serializable.ChatItemRoomSerializable
import com.ai302.app.utils.CustomToast
import com.ai302.app.utils.ViewAnimationUtils
import com.ai302.app.viewModel.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class HomeActivity : AppCompatActivity(), OnItemClickListener {
    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: HomeMessageAdapter
    private lateinit var adapterSearch: HomeMessageSearchAdapter
    private var chatList = mutableListOf<ChatItemRoom>()
    private var chatListRe = mutableListOf<ChatItemRoom>()
    private var chatListSearch = mutableListOf<ChatItemRoom>()
    private var chatListTitleSearch = mutableListOf<ChatItemRoom>()
    private lateinit var chatDatabase: ChatDatabase
    private lateinit var dataStoreManager: DataStoreManager

    private var startY: Float = 0f
    private val MIN_SLIDE_DISTANCE = 100f // 最小滑动距离（像素）

    private var isDatabaseOperationInProgress = false
    private var mTitle = ""

    private var localChatType = ""
    private var localChatPrompt = "这是删除过的内容变为空白"
    private var localIsPrompt = false

    private var serviceProvider = "302AI"

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        // 在 super.onCreate() 和 setContentView() 之前切换回正常主题
        setTheme(R.style.Theme_Ai302)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        CoroutineScope(Dispatchers.IO).launch {
            //getAPItest()
        }
        // 初始化数据库
        chatDatabase = ChatDatabase.getInstance(this)

        dataStoreManager = DataStoreManager(this)

        initListener()
        refreshChatList()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener(){
        binding.line4.setOnClickListener {
            onClickColor(it)
            // 定义一个Intent，指定目标Activity
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        binding.searchCloseBtn.setOnClickListener {
            // 清空输入框
            binding.editSearch.text?.clear()
            binding.searchCloseBtn.visibility = View.GONE
            binding.recycle2.visibility = View.GONE
            binding.recycle1.visibility = View.VISIBLE
        }

        binding.histBack.setOnClickListener {
            //Log.e("ceshi","还有多少列表${chatList.size}")
            if (chatList.size == 0){
                val intent = Intent(this@HomeActivity, ChatActivity::class.java)
                if (localIsPrompt){
                    //localChatPrompt
                }else{
                    localChatPrompt = "这是删除过的内容变为空白"
                }
                intent.putExtra("chat_prompt", localChatPrompt)
                intent.putExtra("model_type", localChatType)
                intent.putExtra("chat_type", "新的聊天")
                startActivity(intent)
            }
            onClickColor(it)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.lineNewChat1.setOnClickListener {
            onClickColor(it)
            val intent = Intent(this@HomeActivity, ChatActivity::class.java)
            var number = 1
            if (localIsPrompt){
                //localChatPrompt
            }else{
                localChatPrompt = "这是删除过的内容变为空白"
            }
            intent.putExtra("chat_prompt", localChatPrompt)
            intent.putExtra("model_type", localChatType)
            if (chatList != null){
                number = chatList.size
                intent.putExtra("chat_type", "新的聊天$number")
            }else{
                intent.putExtra("chat_type", "新的聊天")
            }

            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            /*var hasTitle = false
            lifecycleScope.launch(Dispatchers.IO) {
                if (chatDatabase.chatDao().checkTitleExists("新的聊天")){
                    hasTitle = true
                    // 传递列表参数
                    val intent = Intent(this@HomeActivity, ChatActivity::class.java)
                    intent.putExtra("chat_item", chatDatabase.chatDao().getChatByTitle("新的聊天"))
                    intent.putExtra("chat_type", "新的聊天")
                    startActivity(intent)
                    Log.e("ceshi","来哦${chatDatabase.chatDao().getChatByTitle("新的聊天")}")
                }else{
                    hasTitle = false
                    val intent = Intent(this@HomeActivity, ChatActivity::class.java)
                    intent.putExtra("chat_type", "新的聊天")
                    startActivity(intent)
                }

            }*/

        }

        val resizableDialog = ResizableDialog(this)
        binding.lineTip.setOnClickListener {
            onClickColor(it)
            resizableDialog.showDialog()
        }
        resizableDialog.setOnDismissListener {
            binding.lineTip.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        }

        binding.const1.setOnClickListener {
//            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//            window.setDimAmount(0.5f) // 0.5f 表示半透明灰色
            onClickColor(it)
            val intent = Intent(this, BottomDialogActivity::class.java)
            startActivity(intent)
        }

        binding.recycle1.layoutManager = LinearLayoutManager(this)
        binding.recycle2.layoutManager = LinearLayoutManager(this)

        // 模拟数据
        /*for (i in 1..10) {
            val messages = mutableListOf<String>()
            for (j in 1..3) {
                messages.add("Message $j in Chat $i")
            }
            chatList.add(ChatItemRoom(0,"Chat $i", messages, "2025-04-15 12:00:00"))
        }*/

        // 插入数据到数据库  测试数据
        /*val messages = mutableListOf("你好我是机器人","你好","哈哈哈?","你笑什么！")
        lifecycleScope.launch(Dispatchers.IO) {
            //chatDatabase.chatDao().insertChat(ChatItemRoom(0,"新的聊天1", messages, "2025-04-15 12:00:00"))
            Log.e("ceshi","是否有新的聊天标题:${chatDatabase.chatDao().checkTitleExists("新的聊天1")}")
            val chatItem = chatDatabase.chatDao().getChatByTitle("新的聊天1")
            Log.e("ceshi","0是否有新的聊天标题:${chatItem?.messages}")

            val chatsWithHello = chatDatabase.chatDao().getChatsWithMessageContaining("你笑什么")
            for (chat in chatsWithHello) {
                Log.e("ceshi","Found chat item with 'hello': ${chat.title}")
            }

        }*/




//        adapter = HomeMessageAdapter(chatList,this@HomeActivity)
//        binding.recycle1.adapter = adapter
//        // 通知适配器数据已更改
//        adapter.notifyDataSetChanged()

        // 添加滑动监听器
        binding.recycle1.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // 向下滑动
                    //onRecyclerViewScrollDown()
                } else if (dy < 0) {
                    // 向上滑动
                    //onRecyclerViewScrollUp()
                }
            }
        })

        binding.recycle1.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 记录手指按下时的 Y 坐标
                        startY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // 移动时不处理，仅在抬起时判断方向
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // 计算滑动距离
                        val endY = event.y
                        val deltaY = endY - startY

                        if (deltaY < -MIN_SLIDE_DISTANCE) {
                            // 上滑（手指从下往上滑动，Y 坐标减小）
                            onRecyclerViewScrollUp()

                        } else if (deltaY > MIN_SLIDE_DISTANCE) {
                            // 下滑（手指从上往下滑动，Y 坐标增大）
                            onRecyclerViewScrollDown()
                        }
                    }
                }
                false // 消费触摸事件，避免事件传递给子视图
            }



        // 设置根布局的触摸监听器
        /*binding.main.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 记录手指按下时的 Y 坐标
                    startY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    // 移动时不处理，仅在抬起时判断方向
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 计算滑动距离
                    val endY = event.y
                    val deltaY = endY - startY

                    if (deltaY < -MIN_SLIDE_DISTANCE) {
                        // 上滑（手指从下往上滑动，Y 坐标减小）
                        onRecyclerViewScrollUp()

                    } else if (deltaY > MIN_SLIDE_DISTANCE) {
                        // 下滑（手指从上往下滑动，Y 坐标增大）
                        onRecyclerViewScrollDown()
                    }
                }
            }
            true // 消费触摸事件，避免事件传递给子视图
        }*/

        /*binding.editSearch.setOnClickListener {
            val message = binding.editSearch.text.toString().trim()
            Log.e("ceshi","点击")
            if (message.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val chatsWithSearch = chatDatabase.chatDao().getChatsWithMessageContaining(message)
                    for (chat in chatsWithSearch) {
                        Log.e("ceshi","Found chat item with 'hello': ${chat.title}")
                    }
                }
            }
        }*/

        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                isDatabaseOperationInProgress = true
                binding.searchCloseBtn.visibility = View.VISIBLE

                val job2 = lifecycleScope.launch(Dispatchers.IO) {
                    chatListSearch = chatDatabase.chatDao().getChatsWithMessageContaining(s.toString()).toMutableList()
//                    for (chat in chatsWithSearch) {
//                        Log.e("ceshi","Found chat item with 'hello': ${chat.title}")
//                    }

                    chatListTitleSearch = chatDatabase.chatDao().getChatsWithTitleContaining(s.toString()).toMutableList()
                    //Log.e("ceshi","这里的数量${chatListTitleSearch}")

                }



                lifecycleScope.launch(Dispatchers.Main) {
                    job2.join() // 等待数据库操作完成
//                    delay(500)
                    chatListSearch.addAll(chatListTitleSearch)
                    //Log.e("ceshi","1这里的数量${chatListSearch}")
                    adapterSearch = HomeMessageSearchAdapter(chatListSearch, this@HomeActivity,  onDeleteClickListener = { position,type ->
                        Log.e("ceshi","位置:${position}")
                        if (type=="delete"){
                            val job1 = lifecycleScope.launch(Dispatchers.IO) {
                                if (chatDatabase.chatDao().checkTitleExists(chatList[position].title)){
                                    //先删除后添加
                                    chatDatabase.chatDao().deleteChatByTitle(chatList[position].title)
                                }
                            }
                            lifecycleScope.launch {
                                job1.join()
                                chatList.removeAt(position)
                                adapter.notifyItemRemoved(position)
                            }
                        }else if (type=="edit"){
                            showEditPickerDialog(position)
                        }

                    })
                    adapterSearch.updateSearchMessage(s.toString())
                    // 可以在这里进行 RecyclerView 的设置等操作
                    binding.recycle2.adapter = adapterSearch
                    // 通知适配器数据已更改
                    adapterSearch.notifyDataSetChanged()
                    binding.recycle1.visibility = View.GONE
                    binding.recycle2.visibility = View.VISIBLE
                    isDatabaseOperationInProgress = false

                }
                Log.e("ceshi","搜索文字：${s.toString() == ""}")
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
                        binding.recycle2.visibility = View.GONE
                        binding.recycle1.visibility = View.VISIBLE
                        Log.e("ceshi","搜索文字：执行1")
                        binding.root.requestLayout() // 强制更新布局
                    }
                }
            }
        })

        binding.logoImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://302.ai/") // 设置要跳转的网址
            startActivity(intent) // 启动活动
        }

        binding.appStore.setOnClickListener {
            if (serviceProvider=="302.AI"){
                onClickColor(it)
                val intent = Intent(this, AppStoreActivity::class.java)
                startActivity(intent)
            }else{
                CustomToast.makeText(
                    context = this,
                    message = "此功能只有302.AI服务商才可以使用，谢谢",
                    duration = Toast.LENGTH_SHORT,
                    gravity = Gravity.CENTER
                ).show()
            }

        }

    }

    private fun onClickColor(view:View){
//        view.setBackgroundColor(
//            ContextCompat.getColor(this, R.color.colorSelect)
//        )
        view.setBackgroundResource(R.drawable.shape_select_site_bg_gray_home_line2)
    }

    override fun onResume() {
        super.onResume()
        //window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        Log.e("ceshi","onResume")
        binding.line4.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        binding.lineTip.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        binding.lineNewChat1.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        binding.appStore.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        binding.const1.setBackgroundResource(R.drawable.shape_select_site_bg_line1)
        binding.histBack.setBackgroundResource(R.drawable.shape_select_site_bg_line1)

        lifecycleScope.launch(Dispatchers.IO){
            delay(500)
            lifecycleScope.launch(Dispatchers.Main) {
                chatDatabase.chatDao().getAllChatsLiveData().observe(this@HomeActivity){
                    Log.e("ceshi", "A查询完成时间：${System.currentTimeMillis()}") // 添加日志
                    chatList = it.reversed().toMutableList()
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.saveChatListNumber(chatList.size)
                    }
                    //val chatListRe = chatList.reversed()
                    adapter.updateData(chatList)
                    //Log.e("ceshi","主界面onRestart${chatList[0].messages}")
                    // 通知适配器数据已更改
                    adapter.notifyDataSetChanged()
                }
            }

            val readCueWordsSwitch = dataStoreManager.readCueWordsSwitch.first()
            val readCueWords = dataStoreManager.readCueWords.first()
            val readModelType = dataStoreManager.readModelType.first()?:"gpt-4o"
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

            val readServiceProviderData = dataStoreManager.readServiceProviderData.first()?:"302.AI"
            readServiceProviderData?.let {
                Log.e("ceshi","服务商是多少：$readServiceProviderData")
                serviceProvider = it

            }


        }


    }

    override fun onRestart() {
        super.onRestart()

        //refreshChatList()
        /*val job = lifecycleScope.launch(Dispatchers.IO) {
            chatList = chatDatabase.chatDao().getAllChats().toMutableList()
            Log.e("ceshi", "A查询完成时间：${System.currentTimeMillis()}") // 添加日志
            dataStoreManager.saveChatListNumber(chatList.size)
        }


        lifecycleScope.launch {
            job.join() // 等待数据库操作完成
            adapter.updateData(chatList)
            Log.e("ceshi","主界面onRestart${chatList[0].messages}")
            // 通知适配器数据已更改
            adapter.notifyDataSetChanged()
        }*/

        /*chatDatabase.chatDao().getAllChatsLiveData().observe(this@HomeActivity){
            Log.e("ceshi", "A查询完成时间：${System.currentTimeMillis()}") // 添加日志
            chatList = it.toMutableList()
            lifecycleScope.launch {
                dataStoreManager.saveChatListNumber(chatList.size)
            }
            adapter.updateData(chatList)
            //Log.e("ceshi","主界面onRestart${chatList[0].messages}")
            // 通知适配器数据已更改
            adapter.notifyDataSetChanged()
        }*/
    }

    override fun onStart() {
        super.onStart()
        val job = lifecycleScope.launch(Dispatchers.IO) {

        }


        lifecycleScope.launch {
            job.join() // 等待数据库操作完成

        }



    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun refreshChatList(){
        val job = lifecycleScope.launch(Dispatchers.IO) {
            chatList = chatDatabase.chatDao().getAllChats().reversed().toMutableList()
            dataStoreManager.saveChatListNumber(chatList.size)
        }


        lifecycleScope.launch {
            job.join() // 等待数据库操作完成
            adapter = HomeMessageAdapter(this@HomeActivity, chatList, this@HomeActivity,  onDeleteClickListener = { position,type ->
                Log.e("ceshi","位置:${position},类型：$type")
                if (type=="delete"){
                    val job1 = lifecycleScope.launch(Dispatchers.IO) {
                        if (chatDatabase.chatDao().checkTitleExists(chatList[position].title)){
                            //先删除后添加
                            chatDatabase.chatDao().deleteChatByTitle(chatList[position].title)
                        }
                    }
                    lifecycleScope.launch {
                        job1.join()
                        chatList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                    }
                }else if (type=="edit"){
                    showEditPickerDialog(position)
                }

            })
            // 可以在这里进行 RecyclerView 的设置等操作
            binding.recycle1.adapter = adapter
            // 通知适配器数据已更改
            adapter.notifyDataSetChanged()

        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    /*suspend fun getAPItest(){
        val apiService = NetworkFactory.createApiService(ApiService::class.java)
        val authorizationToken = "sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA"
        //val authorizationToken1 = "sk-RcnM7qzDdqa3i4mylTGPSl5peJzu8CNMx2pe6cauC0es3JCA"
        val request = ChatCompletionRequest(
            messages = listOf(
                RequestMessage(
                    role = "user",
                    content = "2025年美国总统是谁"
                )
            )
        )

        /*CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.postChatCompletion(
                    authorization = authorizationToken,
                    requestBody = request
                )
                // 处理返回的响应数据
                val assistantMessage = response.choices.firstOrNull()?.message?.content
                Log.e("ceshi","返回数据：${assistantMessage}")
                CoroutineScope(Dispatchers.Main).launch {
                    // 在主线程更新 UI
                    if (assistantMessage != null) {
                        // 可以在这里更新 UI 显示结果
                        Log.e("ceshi","返回数据：${assistantMessage}")
                    }
                }
            } catch (e: HttpException) {
                // 处理 HTTP 错误
            } catch (e: IOException) {
                // 处理网络错误
            }
        }*/
    }*/

//    override fun onItemClick(messages: List<String>) {
//        Log.e("ceshi","收到点击信息${messages}")
//        // 从数据库中读取数据
//        /*lifecycleScope.launch(Dispatchers.IO) {
//            val chats = chatDatabase.chatDao().getAllChats()
//            for (chat in chats) {
//                Log.e("ceshi","数据库查找的聊天${chat.messages}")
//            }
//        }*/
//         lifecycleScope.launch(Dispatchers.IO) {  // 传递列表参数
//             val intent = Intent(this@HomeActivity, ChatActivity::class.java)
//             intent.putExtra("chat_item", chatDatabase.chatDao().getChatByTitle("新的聊天"))
//             intent.putExtra("chat_type", "新的聊天")
//             startActivity(intent)
//             Log.e("ceshi","来哦${chatDatabase.chatDao().getChatByTitle("新的聊天")}")
//
//         }
//    }

    override fun onItemClick(chatItem: ChatItemRoom) {
        Log.e("ceshi","收到点击信息${chatItem}")
        // 从数据库中读取数据
        /*lifecycleScope.launch(Dispatchers.IO) {
            val chats = chatDatabase.chatDao().getAllChats()
            for (chat in chats) {
                Log.e("ceshi","数据库查找的聊天${chat.messages}")
            }
        }*/
        lifecycleScope.launch(Dispatchers.IO) {  // 传递列表参数
            val intent = Intent(this@HomeActivity, ChatActivity::class.java)
            intent.putExtra("chat_item", chatItem)
            intent.putExtra("chat_type", chatItem.title)
            intent.putExtra("model_type", chatItem.modelType)
            if (localIsPrompt){
                //localChatPrompt
            }else{
                localChatPrompt = "这是删除过的内容变为空白"
            }
            intent.putExtra("chat_prompt", localChatPrompt)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            Log.e("ceshi","来哦${chatDatabase.chatDao().getChatByTitle(chatItem.title)}")

        }
    }

    private fun onRecyclerViewScrollUp(){
        binding.line6.visibility = View.GONE
    }

    private fun onRecyclerViewScrollDown(){
        binding.line6.visibility = View.VISIBLE
    }

    private fun showEditPickerDialog(position:Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_picker)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        dialog.findViewById<EditText>(R.id.edit_title).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //参数1代表输入的
                Log.e("TAG", "beforeTextChanged: 输入前（内容变化前）的监听回调$s===$start===$count===$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TAG", "beforeTextChanged: 输入中（内容变化中）的监听回调$s===$start===$before===$count")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("TAG", "beforeTextChanged: 输入后（内容变化后）的监听回调$s")
                mTitle = s.toString()

            }
        })



        dialog.findViewById<Button>(R.id.saveEditButton).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            val job1 = lifecycleScope.launch(Dispatchers.IO) {
                if (chatDatabase.chatDao().checkTitleExists(chatList[position].title)){
                    //
                    chatDatabase.chatDao().updateChat(ChatItemRoom(chatList[position].id,mTitle,chatList[position].messages,
                        chatList[position].time,chatList[position].messagesTimes,chatList[position].modelType,
                        chatList[position].chatPrompt,chatList[position].displayUrl,chatList[position].isDeepThink,
                        chatList[position].isNetWorkThink,chatList[position].userId,chatList[position].isEye))
                }
            }
            lifecycleScope.launch {
                job1.join()
                //chatList.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.cancelEditButton).setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        if (chatList.size == 0){
            val intent = Intent(this@HomeActivity, ChatActivity::class.java)
            if (localIsPrompt){
                //localChatPrompt
            }else{
                localChatPrompt = "这是删除过的内容变为空白"
            }
            intent.putExtra("chat_prompt", localChatPrompt)
            intent.putExtra("model_type", localChatType)
            intent.putExtra("chat_type", "新的聊天")
            startActivity(intent)
        }
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


}