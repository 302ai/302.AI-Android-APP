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
import com.ai302.app.infa.OnCueWordLeftItemClickListener
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
class CueWordLeftListAdapter(private val context: Context,private val cueWordLeftList: List<String>, private val listener: OnCueWordLeftItemClickListener, private val onDeleteClickListener: (Int) -> Unit,) :
    RecyclerView.Adapter<CueWordLeftListAdapter.ChatViewHolder>() {

    // 记录上一次点击的 item 位置
    private var lastSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cue_word_left_list_item, parent, false)
        return ChatViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val cueWordLeftItem = cueWordLeftList[position]
        holder.leftTv.text = cueWordLeftItem


        // 根据位置设置背景颜色
        if (position == lastSelectedPosition) {
            // 当前点击的 item，设置为指定颜色
            holder.cueWordLeftLayout.setBackgroundColor(
                ContextCompat.getColor(context, R.color.color6)
            )
        } else {
            // 其他 item，设置为白色
            holder.cueWordLeftLayout.setBackgroundColor(
                ContextCompat.getColor(context, android.R.color.white)
            )
        }



        holder.cueWordLeftLayout.setOnClickListener {
//            if (position != lastSelectedPosition){
//                //holder.cueWordLeftLayout.setBackgroundResource(R.drawable.shape_select_site_bg_blue_line)
//                holder.cueWordLeftLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.color6))
//
//            }

            // 保存上一次点击的位置
            val previousClickedPosition = lastSelectedPosition
            // 更新当前点击的位置
            lastSelectedPosition = position

            // 通知 RecyclerView 更新之前点击的 item 和当前点击的 item
            if (previousClickedPosition != -1) {
                notifyItemChanged(previousClickedPosition)
            }
            notifyItemChanged(position)


            listener.onLeftItemClick(cueWordLeftItem)

        }



    }

    override fun getItemCount(): Int {
        return cueWordLeftList.size
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftTv: TextView = itemView.findViewById(R.id.leftTv)
        val cueWordLeftLayout: View = itemView.findViewById(R.id.cue_word_left_layout)

    }
}