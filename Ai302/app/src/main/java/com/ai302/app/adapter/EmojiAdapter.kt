package com.ai302.app.adapter

/**
 * author :
 * e-mail :
 * time   : 2025/6/17
 * desc   :
 * version: 1.0
 */
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ai302.app.R

class EmojiAdapter(private val emojis: List<String>, private val onEmojiSelected: (String) -> Unit) :
    RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiTextView: TextView = itemView.findViewById(R.id.emojiTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_emoji, parent, false)
        return EmojiViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val emoji = emojis[position]
        holder.emojiTextView.text = emoji.toString()

        Log.e("EmojiAdapter", "Binding emoji: $emoji at position $position")

        // 添加这行，强制设置字体（确保兼容性）
        holder.emojiTextView.typeface = Typeface.DEFAULT


        holder.itemView.setOnClickListener {
            onEmojiSelected(emoji)
        }
    }

    override fun getItemCount() = emojis.size
}