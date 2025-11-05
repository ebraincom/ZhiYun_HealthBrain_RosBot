// =================================================================================
// 文件路径: app/src/main/java/com/zhiyun/agentrobot/data/network/EmoticonApiClient.kt
// 【V1.0 · 全新创建 - 专属客户端】// =================================================================================
package com.zhiyun.agentrobot.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ✅ 【表情包专用客户端】
 * 只负责创建和管理 EmoticonApiService 接口的实例。
 */
object EmoticonApiClient {

    // ‼️ 您的Zhiyun Media Server服务器地址！
    private const val BASE_URL = "http://117.50.85.132:3000"

    val instance: EmoticonApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
        retrofit.create(EmoticonApiService::class.java)
    }
}


