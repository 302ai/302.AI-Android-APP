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

data class CueWordItem(
    val name: String,
    val emoji: String,
    val prompt: String,
    val description: String,
    val group:String
): Serializable