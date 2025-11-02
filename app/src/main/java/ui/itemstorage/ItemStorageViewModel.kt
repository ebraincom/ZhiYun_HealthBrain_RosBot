// ✅【【【这是完整的 ItemStorageViewModel.kt 文件！！！】】】
package com.zhiyun.agentrobot.ui.itemstorage

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 负责管理物品存放页面的UI状态和业务逻辑
 */
class ItemStorageViewModel : ViewModel() {

    private val TAG = "ItemStorage_ViewModel"

    // 使用StateFlow来持有可被Compose观察的状态列表
    private val _storageItems = MutableStateFlow<List<String>>(emptyList())
    val storageItems = _storageItems.asStateFlow()

    init {
        Log.i(TAG, ">>> [ViewModel] ViewModel instance has been created. Ready to manage data.")
    }

    /**
     * 添加一个新的存放记录
     */
    fun addStorageItem(item: String) {
        val updatedList = _storageItems.value.toMutableList()
        updatedList.add(0, item) // 新项目加到列表的最前面
        _storageItems.value = updatedList
        Log.i(TAG, ">>> [ViewModel] New item added: $item")
    }

    /**
     * 清空所有存放记录
     */
    fun clearAllItems() {
        _storageItems.value = emptyList()
        Log.i(TAG, ">>> [ViewModel] All items have been cleared.")
    }
}
