package com.ai302.app.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ai302.app.ChatItem
import com.ai302.app.R
import com.ai302.app.data.CueWordItem
import com.ai302.app.datastore.DataStoreManager
import com.ai302.app.infa.OnItemClickListener
import com.ai302.app.room.ChatItemRoom
import com.ai302.app.utils.ViewAnimationUtils
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * author :
 * e-mail : "time/{r/p}"
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
class HomeMessageAdapter(private val context:Context,private var chatList: List<ChatItemRoom>, private val listener: OnItemClickListener, private val onDeleteClickListener: (Int,String) -> Unit,) :
    RecyclerView.Adapter<HomeMessageAdapter.ChatViewHolder>() {
    // 记录上一次点击的 item 位置
    private var lastSelectedPosition = -1
    private lateinit var dataStoreManager: DataStoreManager
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_list_item, parent, false)
        dataStoreManager = DataStoreManager(context)
        // 仅在打开时读取一次数据
        CoroutineScope(Dispatchers.IO).launch {
            val data = dataStoreManager.readLastPosition.first()
            data?.let {
                lastSelectedPosition = it
            }
        }
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = chatList[position]
        holder.title.text = chatItem.title
        holder.time.text = chatItem.time

        // 根据位置设置背景颜色
        if (position == lastSelectedPosition) {
            // 当前点击的 item，设置为指定颜色
//            holder.contentLayout.setBackgroundColor(
//                ContextCompat.getColor(context, R.color.colorSelect)
//            )
            //holder.contentLayout.setBackgroundResource(R.drawable.shape_select_site_bg_gray_home_line2)
        } else {
            // 其他 item，设置为白色
//            holder.contentLayout.setBackgroundColor(
//                ContextCompat.getColor(context, R.color.colorUnselect)
//            )
            //holder.contentLayout.setBackgroundResource(R.drawable.shape_unselect_site_bg_gray_home_line)
        }

        var messageText = ""
        /*for (message in chatItem.messages) {
            messageText += "$message\n"
        }*/
        //messageText = (chatItem.messages.size/2).toString()
        // 计算排除指定字符串后的列表 size
        val newSize = chatItem.messages.count { it != "这是删除过的内容变为空白" }
        holder.messagesNumber.text = newSize.toString()
        holder.contentLayout.setOnClickListener {
            // 保存上一次点击的位置
            val previousClickedPosition = lastSelectedPosition
            // 更新当前点击的位置
            lastSelectedPosition = position
            CoroutineScope(Dispatchers.IO).launch {
                dataStoreManager.saveLastPosition(lastSelectedPosition)
            }

            // 通知 RecyclerView 更新之前点击的 item 和当前点击的 item
            if (previousClickedPosition != -1 && previousClickedPosition<chatList.size-1) {
                notifyItemChanged(previousClickedPosition)
            }
            notifyItemChanged(position)

            listener.onItemClick(chatItem)
        }

        holder.btnDelete.setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            onDeleteClickListener(position,"delete")
            (holder.itemView as SwipeMenuLayout).smoothClose()
        }
        holder.btnEdit.setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            onDeleteClickListener(position,"edit")
            (holder.itemView as SwipeMenuLayout).smoothClose()
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    // 更新数据集的方法
    fun updateData(newChatList: List<ChatItemRoom>) {
        chatList = newChatList
        notifyDataSetChanged() // 通知适配器数据集已改变
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleTv)
        val messagesNumber: TextView = itemView.findViewById(R.id.messageNumberTv)
        val time: TextView = itemView.findViewById(R.id.homeTimeTv)
        val btnDelete: Button = itemView.findViewById(R.id.mBtnDelete)
        val contentLayout: View = itemView.findViewById(R.id.content_layout)
        val btnEdit: Button = itemView.findViewById(R.id.mBtnEdit)
    }
}