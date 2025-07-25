package com.ai302.app.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai302.app.R
import com.ai302.app.databinding.ActivityChatBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * author :
 * e-mail :
 * time   : 2025/5/12
 * desc   :
 * version: 1.0
 */
object PopupWindowUtils {
    private lateinit var popupWindow: PopupWindow
    private var modelSearchList = mutableListOf<String>()
    private var localModelSearchList = mutableListOf<String>()
    private var mCurY = 0
    private var isFirst = true

    fun setPopupWindow(popupWindow: PopupWindow,modelList: MutableList<String>){
        this.popupWindow = popupWindow
        this.localModelSearchList = modelList
    }



    fun setupPopupWindow(modelList: MutableList<String>,context: Context,binding:ActivityChatBinding) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_a_list, null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            isFocusable = true                // 必设：允许内部获取焦点
            isOutsideTouchable = false        // 关键修改：禁止点击外部关闭（包括软键盘区域）
            isTouchable = true                // 允许触摸内部视图
            inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED  // 强制保留输入框
            animationStyle = android.R.style.Animation_Dialog
        }

        val recyclerView = popupView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.popupRecyclerView)
        val editSearchType = popupView.findViewById<AppCompatEditText>(R.id.edit_search_type)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = object : BaseQuickAdapter<String, BaseViewHolder>(
            R.layout.item_popup_select, modelList
        ) {
            override fun convert(holder: BaseViewHolder, item: String) {
                if (holder.adapterPosition == 1){
                    holder.setText(R.id.itemTextView, item)
                }
                holder.setText(R.id.itemTextView, item)
                if (holder.adapterPosition == 0 || holder.adapterPosition == 3) {
                    // 重新设置字体大小
                    //holder.setTextViewTextSize(R.id.itemTextView, 20f)
                }

                editSearchType.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


                        Log.e("ceshi","0搜索文字：${start},$before,$count")

                        if (start>=2){
                            val job2 = CoroutineScope(Dispatchers.IO).launch {
                                modelSearchList.clear()
                                for (modelType in localModelSearchList){
                                    if (modelType.contains(s.toString(),ignoreCase = true)){
                                        modelSearchList.add(modelType)
                                    }
                                }
                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                job2.join() // 等待数据库操作完成
                                //Log.e("ceshi","搜索文字模型：${modelSearchList}")
                                setupPopupWindow(modelSearchList,context,binding)
                            }
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
                            CoroutineScope(Dispatchers.Main).launch{
                                Log.e("ceshi","搜索文字：执行")
                                //不做延迟处理，recycle1数据还没有更新完，就是导致下面的visibility不起作用
                                delay(500)
                                Log.e("ceshi","搜索文字：执行1")
                                binding.root.requestLayout() // 强制更新布局
                            }
                            modelSearchList.clear()
                        }
                        showPopup(binding.const1,binding, mCurY,true)
                        //showPopup(binding.const1,binding,)
                        //modelSearchList.clear()
                    }
                })

            }
        }.apply {
            setOnItemClickListener { _, _, position ->
                popupWindow.dismiss()
//                findViewById<TextView>(R.id.selectedTextView).text = modelList[position]
//                modelType = modelList[position]
//                findViewById<TextView>(R.id.selectedTextView).visibility = View.VISIBLE
                binding.modelTypeLine.visibility = View.VISIBLE
                binding.selectModelTypeAdd.text = modelList[position]

            }
        }



    }

    fun showPopup(anchorView: View, binding:ActivityChatBinding,curY:Int,isKeySoft:Boolean) {

        mCurY = curY

        if (isKeySoft){
            popupWindow.showAsDropDown(
                anchorView,
                -(anchorView.width - popupWindow.width) / 2,
                -curY*3
            )
        }else{
            popupWindow.showAsDropDown(
                anchorView,
                -(anchorView.width - popupWindow.width) / 2,
                -curY
            )
        }


    }

}