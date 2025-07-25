package com.ai302.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai302.app.R
import com.ai302.app.adapter.CueWordAdapter
import com.ai302.app.adapter.CueWordLeftListAdapter
import com.ai302.app.adapter.HomeMessageAdapter
import com.ai302.app.adapter.HomeMessageSearchAdapter
import com.ai302.app.data.AppStoreItem
import com.ai302.app.data.CueWordItem
import com.ai302.app.databinding.ActivityBottomDialogBinding
import com.ai302.app.databinding.ActivityMainBinding
import com.ai302.app.infa.OnCueWordItemClickListener
import com.ai302.app.infa.OnCueWordLeftItemClickListener
import com.ai302.app.infa.OnItemClickListener
import com.ai302.app.room.ChatItemRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException

class BottomDialogActivity : AppCompatActivity(), OnCueWordItemClickListener,
    OnCueWordLeftItemClickListener {
    private lateinit var binding:ActivityBottomDialogBinding
    private lateinit var adapter: CueWordAdapter
    private lateinit var adapterLeft: CueWordLeftListAdapter
    private var cueWordList = mutableListOf<CueWordItem>()
    private var cueWordLeftList = mutableListOf<CueWordItem>()
    private var cueWordSearchList = mutableListOf<CueWordItem>()
    private var cueLeftList = mutableListOf("全部","","","","")
    private var cueLeftSet = mutableSetOf<String>()
    @SuppressLint("MissingInflatedId", "RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 在 super.onCreate() 和 setContentView() 之前切换回正常主题
        setTheme(R.style.Theme_Ai302)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBottomDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_bottom_dialog)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.recycleList.layoutManager = LinearLayoutManager(this)
        binding.cueWordLeftListRecycle.layoutManager = LinearLayoutManager(this)
        cueLeftSet.add("全部")
        //cueLeftSet.add("我的")





        val job2 = lifecycleScope.launch(Dispatchers.IO) {
            // 读取 JSON 文件
            val json = loadJSONFromAsset("agents.json")
            if (json != null) {
                try {
                    // 解析 JSON 数据
                    val jsonArray = JSONArray(json)
                    val result = StringBuilder()
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val name = jsonObject.getString("name")
                        val emoji = jsonObject.getString("emoji")
                        val prompt = jsonObject.getString("prompt")
                        val description = jsonObject.getString("description")
                        val group = jsonObject.getString("group")
                        val resultGroup = StringBuilder()
                        group.forEach {
                            resultGroup.append(it)
                        }
                        Log.e("ceshi","这里的值：${resultGroup.toString().contains("工具")}")
                        Log.e("ceshi","0这里的值：${resultGroup.toString()}")

                        try {
                            val jsonArray = JSONArray(resultGroup.toString())
                            for (i in 0 until jsonArray.length()) {
                                val element = jsonArray.getString(i)
                                cueLeftSet.add(element.toString())
                                Log.e("ceshi","1这里的值：${element.toString()}")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }



                        cueWordList.add(CueWordItem(name,emoji,prompt,description,resultGroup.toString()))
//                    result.append("Name: $name\n")
//                    result.append("Emoji: $emoji\n")
//                    result.append("Prompt: $prompt\n")
//                    result.append("Description: $description\n\n")

                    }

                    // 显示结果

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }



        lifecycleScope.launch(Dispatchers.Main) {
            job2.join() // 等待数据库操作完成
            adapter = CueWordAdapter(this@BottomDialogActivity,cueWordList, this@BottomDialogActivity,  onDeleteClickListener = { position ->
                Log.e("ceshi","位置:${position}")

            })

            // 可以在这里进行 RecyclerView 的设置等操作
            binding.recycleList.adapter = adapter
            // 通知适配器数据已更改
            adapter.notifyDataSetChanged()

            //侧边栏list
            adapterLeft = CueWordLeftListAdapter(this@BottomDialogActivity,cueLeftSet.toList(), this@BottomDialogActivity,  onDeleteClickListener = { position ->
                Log.e("ceshi","位置:${position}")

            })
            // 可以在这里进行 RecyclerView 的设置等操作
            binding.cueWordLeftListRecycle.adapter = adapterLeft
            // 通知适配器数据已更改
            adapterLeft.notifyDataSetChanged()


        }
        //Log.e("ceshi","搜索文字：${s.toString() == ""}")





        // 为打开侧边栏按钮设置点击监听器
        binding.tipList.setOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.LEFT)
        }

        // 为关闭侧边栏按钮设置点击监听器
//        closeDrawerButton.setOnClickListener {
//            binding.drawerLayout.closeDrawer(Gravity.LEFT)
//        }
        // 监听抽屉状态
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: android.view.View, slideOffset: Float) {
                // 抽屉滑动时的回调
            }

            override fun onDrawerOpened(drawerView: android.view.View) {
                // 抽屉打开时，为空白区域添加点击事件
                binding.drawerLayout.setOnClickListener {
                    if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        binding.drawerLayout.closeDrawer(Gravity.LEFT)
                    }
                }
            }

            override fun onDrawerClosed(drawerView: android.view.View) {
                // 抽屉关闭时，移除点击事件
                binding.drawerLayout.setOnClickListener(null)
            }

            override fun onDrawerStateChanged(newState: Int) {
                // 抽屉状态改变时的回调
            }
        })

        // 调整窗口尺寸（可选）
//        window.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )

        // 关闭按钮
        findViewById<ImageView>(R.id.closeBtn).setOnClickListener {
            finish()
        }

        // 处理返回键
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.drawerLayout.closeDrawer(Gravity.LEFT)
                finish()
            }
        })

        //搜索
        binding.editCueWord.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


                val job1 = lifecycleScope.launch(Dispatchers.IO) {
                    for (cueWord in cueWordList){
                        if (cueWord.description.contains(s.toString())){
                            cueWordSearchList.add(cueWord)
                        }
                    }
                }



                lifecycleScope.launch(Dispatchers.Main) {
                    job1.join() // 等待数据库操作完成
                    adapter.updateData(cueWordSearchList)
                    adapter.notifyDataSetChanged()
                }
                Log.e("ceshi","搜索文字：${s.toString()}")
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
                        adapter.updateData(cueWordList)
                        adapter.notifyDataSetChanged()
                        cueWordSearchList.clear()
                    }
                }
            }
        })



    }

    // 点击外部关闭（API 21+ 自动生效，低版本需手动处理）
//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        if (ev?.action == MotionEvent.ACTION_DOWN) {
//            val decorView = window.decorView
//            val rect = Rect()
//            decorView.getWindowVisibleDisplayFrame(rect)
//            val x = ev.rawX.toInt()
//            val y = ev.rawY.toInt()
//            if (!rect.contains(x, y)) {
//                finish()
//                return true
//            }
//        }
//        return super.dispatchTouchEvent(ev)
//    }


    private fun loadJSONFromAsset(fileName: String): String? {
        return try {
            val assetManager: AssetManager = assets
            val inputStream = assetManager.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun onStop() {
        super.onStop()

        Log.e("ceshi","onStop提示词")
    }

    override fun onDestroy() {
        super.onDestroy()
        cueWordList.clear()
        cueWordLeftList.clear()
        Log.e("ceshi","onDestroy提示词")
    }


    override fun onItemClick(cueWordItem: CueWordItem) {
        Log.e("ceshi","收到点击信息提示词${cueWordItem}")
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chat_cueWord", cueWordItem)
        intent.putExtra("chat_type", cueWordItem.name)
        startActivity(intent)
    }

    override fun onLeftItemClick(cueWordLeftItem: String) {
        Log.e("ceshi","侧边栏收到点击信息提示词${cueWordLeftItem}")
        binding.text2.text = cueWordLeftItem
        val job = lifecycleScope.launch(Dispatchers.IO) {
            if (cueWordLeftItem == "全部" || cueWordLeftItem == "我的"){
                //cueWordLeftList = cueWordList
            }else{
                cueWordLeftList.clear()
                for (cueWord in cueWordList){
                    if (cueWord.group.contains(cueWordLeftItem)){
                        cueWordLeftList.add(cueWord)
                    }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            job.join()
            // 通知适配器数据已更改
//            adapter = CueWordAdapter(cueWordList, this@BottomDialogActivity,  onDeleteClickListener = { position ->
//                Log.e("ceshi","位置:${position}")
//
//            })
//
//            // 可以在这里进行 RecyclerView 的设置等操作
//            binding.recycleList.adapter = adapter
            if (cueWordLeftItem == "全部" || cueWordLeftItem == "我的"){
                adapter.updateData(cueWordList)
            }else{
                adapter.updateData(cueWordLeftList)
            }

            adapter.notifyDataSetChanged()
            binding.drawerLayout.closeDrawer(Gravity.LEFT)
        }

    }

}