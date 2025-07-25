package com.ai302.app.serializable

import java.io.Serializable

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/4/16
 * desc   :
 * version: 1.0
 */
data class ChatItemRoomSerializable (
    var title: String,
    var messages: List<String>,
    var time: String
): Serializable