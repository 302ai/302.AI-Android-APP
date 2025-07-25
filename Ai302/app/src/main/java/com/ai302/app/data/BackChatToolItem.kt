package com.ai302.app.data

import androidx.room.Entity
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

data class BackChatToolItem(
    val type: String,
    val message: String,
    val position:Int
): Serializable