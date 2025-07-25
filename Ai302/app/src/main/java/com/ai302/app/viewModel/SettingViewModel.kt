package com.ai302.app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai302.app.MyApplication
import com.ai302.app.datastore.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingViewModel : ViewModel() {
    private val _textState = MutableStateFlow("")
    val textState: StateFlow<String> = _textState.asStateFlow()
    // 使用 applicationContext 获取单例
    private val dataStoreManager = DataStoreManager.getInstance(
        MyApplication.myApplicationContext // 需要自定义 Application 类
    )

    init {
        viewModelScope.launch {
            dataStoreManager.readCueWords
                .map { it ?: "你好，有什么问题都可以问我。" } // 处理null值，提供默认值
                .distinctUntilChanged()
                .collect { _textState.value = it }
        }
    }


}