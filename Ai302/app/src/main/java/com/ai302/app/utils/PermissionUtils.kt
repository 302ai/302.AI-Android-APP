package com.ai302.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.ai302.app.ui.ChatActivity

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/5/15
 * desc   :
 * version: 1.0
 */
object PermissionUtils {

    val TAG = "PermissionUtils"
    fun checkRecordPermission(activity: Activity) {
        val permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Record permission is granted")
        } else {
            Log.d(TAG, "Requesting record permission")
            activity.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }
}