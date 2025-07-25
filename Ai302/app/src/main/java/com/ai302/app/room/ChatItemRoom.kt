package com.ai302.app.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.io.Serializable

/**
 * author :
 * e-mail :
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
@Entity(tableName = "chat_items",
    // 为 title 字段添加唯一索引（全局唯一）
    indices = [Index(value = ["title"], unique = true)])
@TypeConverters(MessagesConverter::class)
data class ChatItemRoom(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val messages: MutableList<String>,
    val time: String,
    val messagesTimes: MutableList<String>,
    val modelType: String,
    val chatPrompt: String,
    val displayUrl: String,
    val isDeepThink: Boolean,
    val isNetWorkThink: Boolean,
    val userId: String,
    val isEye: Boolean
): Serializable