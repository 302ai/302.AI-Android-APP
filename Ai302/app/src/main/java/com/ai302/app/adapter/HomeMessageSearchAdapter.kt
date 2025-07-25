package com.ai302.app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ai302.app.ChatItem
import com.ai302.app.R
import com.ai302.app.infa.OnItemClickListener
import com.ai302.app.infa.OnItemSearchClickListener
import com.ai302.app.room.ChatItemRoom
import com.ai302.app.utils.ViewAnimationUtils
import com.mcxtzhang.swipemenulib.SwipeMenuLayout

/**
 * author :
 * e-mail : "time/{r/p}"
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
class HomeMessageSearchAdapter(private val chatList: List<ChatItemRoom>, private val listener: OnItemClickListener, private val onDeleteClickListener: (Int,String) -> Unit) :
    RecyclerView.Adapter<HomeMessageSearchAdapter.ChatViewHolder>() {
        var searchTextNew = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_list_search_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = chatList[position]
        holder.title.text = chatItem.title
        holder.time.text = chatItem.time

        var messageText = ""
        /*for (message in chatItem.messages) {
            messageText += "$message\n"
        }*/
        //messageText = (chatItem.messages.size/2).toString()
        val newSize = chatItem.messages.count { it != "这是删除过的内容变为空白" }
        holder.messagesNumber.text = newSize.toString()
       // holder.messagesNumber.text = chatItem.messages.size.toString()
        //Log.e("ceshi","链表：${chatItem.messages}搜索内容：${searchTextNew}")

        if (searchForHello(chatItem.messages,searchTextNew).isEmpty()){
            holder.messageSearch.text = chatItem.messages[0]
        }else{
            holder.messageSearch.text = searchForHello(chatItem.messages,searchTextNew)[0]
        }
        //holder.messageSearch.text = "hi"
        holder.contentLayout.setOnClickListener {
            listener.onItemClick(chatItem)
        }

//        holder.btnDelete.setOnClickListener {
//            onDeleteClickListener(position)
//            (holder.itemView as SwipeMenuLayout).smoothClose()
//        }

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

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleSearchTv)
        val messageSearch: TextView = itemView.findViewById(R.id.textSearch)
        val messagesNumber: TextView = itemView.findViewById(R.id.messageSearchNumberTv)
        val time: TextView = itemView.findViewById(R.id.homeTimeSearchTv)
        //val btnDelete: Button = itemView.findViewById(R.id.mBtnDelete)
        val contentLayout: View = itemView.findViewById(R.id.content_search_layout)
        val btnEdit: Button = itemView.findViewById(R.id.mBtnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.mBtnDelete)
    }

    fun searchForHello(list: MutableList<String>,searchText:String): MutableList<String> {
        return list.filter { it.contains(searchText) }.toMutableList()
    }

//    fun searchForHello1(title: String,searchText:String): String {
//        return title.filter { it.contains(searchText) }
//    }

    fun updateSearchMessage(searchText: String) {
        searchTextNew = searchText
    }

}