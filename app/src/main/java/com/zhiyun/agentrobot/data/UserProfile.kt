package com.zhiyun.agentrobot.data // 确保包名与文件路径一致

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 用户信息数据类
 * @param name 用户名
 * @param avatarUrl 用户头像的URL地址（可选，为未来扩展预留）
 * @param userId 用户唯一ID（可选，为未来扩展预留）
 */
@Parcelize
data class UserProfile(
    val name: String,
    val avatarUrl: String? = null,
    val userId: String? = null
) : Parcelable
