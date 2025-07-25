package com.ai302.app.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.widget.Toast

/**
 * author :
 * e-mail :
 * time   : 2025/5/16
 * desc   :
 * version: 1.0
 */
object VoiceToTextUtils {
    //录音参数
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    fun initVoiceToText(mediaRecorder: MediaRecorder,mediaPlayer: MediaPlayer){
        this.mediaRecorder = mediaRecorder
    }

    // 开始录音
    fun startRecording(audioFilePath:String,context: Context) {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)       // 音频源：麦克风
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)  // 输出格式（支持MP3编码）
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)     // 音频编码器（AAC兼容MP3）
            setOutputFile(audioFilePath)                        // 输出路径
            setAudioSamplingRate(44100)                         // 采样率（标准CD音质）
            setAudioEncodingBitRate(192000)                     // 比特率（192kbps高品质）

            try {
                prepare()
                start()
                //isRecording = true
//                btnRecord.text = "停止录音"
//                tvStatus.text = "状态：录音中..."
                //binding.recordAudio.isEnabled = false
            } catch (e: Exception) {
                Toast.makeText(context, "录音初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 停止录音
    fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                // 防止异常崩溃
            }
            mediaRecorder = null
            //isRecording = false
//            btnRecord.text = "开始录音"
//            tvStatus.text = "状态：录音完成，文件路径：$audioFilePath"
            //btnPlay.isEnabled = true  // 录音完成后启用播放按钮
        }
    }

    fun clearVoiceToText(){

    }







}