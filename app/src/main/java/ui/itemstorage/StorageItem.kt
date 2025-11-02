package com.zhiyun.agentrobot.ui.itemstorage

// 定义了“记忆卡片”所需的数据结构
// 遵从您的远见，预留了userId字段，为未来的用户系统做好准备
data class StorageItem(
    val id: String,
    val content: String,
    val dateTime: String,
    val userId: String? = null // 预留字段
)
