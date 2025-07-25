package com.ai302.app.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * author :
 * e-mail :
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
class MessagesConverter {
    @TypeConverter
    fun fromMessagesList(messages: List<String>): String {
        return Gson().toJson(messages)
    }

    @TypeConverter
    fun toMessagesList(messagesString: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(messagesString, type)
    }
}