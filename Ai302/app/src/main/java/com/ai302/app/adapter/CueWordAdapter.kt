package com.ai302.app.adapter

import android.annotation.SuppressLint
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
import com.ai302.app.infa.OnCueWordItemClickListener
import com.ai302.app.infa.OnItemClickListener
import com.ai302.app.room.ChatItemRoom
import com.mcxtzhang.swipemenulib.SwipeMenuLayout

/**
 * author :
 * e-mail : "time/{r/p}"
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
class CueWordAdapter(private val context: Context, private var cueWordList: List<CueWordItem>, private val listener: OnCueWordItemClickListener, private val onDeleteClickListener: (Int) -> Unit,) :
    RecyclerView.Adapter<CueWordAdapter.ChatViewHolder>() {

    private val colorIdList = mutableSetOf(R.color.color1,R.color.color2,R.color.color3,R.color.color4,R.color.color5,R.color.color6).toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cue_word_list_item, parent, false)
        return ChatViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val cueWordItem = cueWordList[position]
        holder.cueEmojiTv.text = cueWordItem.emoji
        holder.cueNameTv.text = cueWordItem.name
        holder.cueDescriptionTv.text = cueWordItem.description

//        colorIdList.forEach {
//
//        }
//        if (position<colorIdList.size-1){
//            holder.cueNameLine.setBackgroundColor(colorIdList.toList()[position])
//        }else{
//            holder.cueNameLine.setBackgroundColor(colorIdList.toList()[position%2])
//        }

        // 计算当前要使用的颜色索引
        val colorIndex = position % colorIdList.size
        // 获取颜色资源 ID
        val colorResId = colorIdList[colorIndex]
        // 设置 item 的背景颜色
        holder.cueNameLine.setBackgroundResource(R.drawable.shape_select_site_bg_gray_line)
        holder.cueNameLine.setBackgroundColor(ContextCompat.getColor(context, colorResId))//之前设置颜色失败的原因是没有使用ContextCompat.getColor方法，要将对应id转化为颜色




        var messageText = ""
        /*for (message in chatItem.messages) {
            messageText += "$message\n"
        }*/
        //messageText = (chatItem.messages.size/2).toString()
//        holder.messagesNumber.text = chatItem.messages.size.toString()
        holder.cueWordLayout.setOnClickListener {
            listener.onItemClick(cueWordItem)
        }

//        holder.btnDelete.setOnClickListener {
//            onDeleteClickListener(position)
//            (holder.itemView as SwipeMenuLayout).smoothClose()
//        }

    }

    override fun getItemCount(): Int {
        return cueWordList.size
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cueEmojiTv: TextView = itemView.findViewById(R.id.cueEmojiTv)
        val cueNameTv: TextView = itemView.findViewById(R.id.cueNameTv)
        val cueDescriptionTv: TextView = itemView.findViewById(R.id.cueDescriptionTv)
        val cueWordLayout: View = itemView.findViewById(R.id.cue_word_layout)
        val cueNameLine : View = itemView.findViewById(R.id.cueNameLine)
    }

    // 更新数据集的方法
    fun updateData(newCueWordList: List<CueWordItem>) {
        cueWordList = newCueWordList
        notifyDataSetChanged() // 通知适配器数据集已改变
    }

}