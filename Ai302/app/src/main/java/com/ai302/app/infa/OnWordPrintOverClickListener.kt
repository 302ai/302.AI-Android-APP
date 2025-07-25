package com.ai302.app.infa

import com.ai302.app.data.BackChatToolItem
import com.ai302.app.data.CueWordItem
import com.ai302.app.room.ChatItemRoom

interface OnWordPrintOverClickListener {
    fun onOverItemClick(wordPrintOverItem: Boolean)

    fun onBackChatTool(backChatToolItem: BackChatToolItem)

    fun onDeleteImagePosition(position:Int)

    fun onPreImageClick(resUrl:String)
}