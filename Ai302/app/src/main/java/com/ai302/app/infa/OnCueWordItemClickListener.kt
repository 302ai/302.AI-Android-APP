package com.ai302.app.infa

import com.ai302.app.data.CueWordItem
import com.ai302.app.room.ChatItemRoom

interface OnCueWordItemClickListener {
    fun onItemClick(cueWordItem: CueWordItem)
}