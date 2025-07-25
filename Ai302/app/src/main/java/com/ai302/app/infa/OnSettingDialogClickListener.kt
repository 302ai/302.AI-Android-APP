package com.ai302.app.infa

import com.ai302.app.room.ChatItemRoom

interface OnSettingDialogClickListener {
    fun onModelTypeClick(modelType: String,mServiceProvider:String)
}