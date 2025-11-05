// =================================================================================// 文件路径: app/src/main/java/com/zhiyun/agentrobot/data/network/ApiClient.kt
// 【V1.0 · 终极完整版】
// =================================================================================
package com.zhiyun.agentrobot.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // 或者您项目中使用的其他JSON解析库，如 MoshiConverterFactory

/**
 * Retrofit 客户端单例对象（我军唯一的“网络请求兵工厂”）。
 * 它采用饿汉式单例模式，负责创建和管理 ApiService 接口的唯一实例。
 */
object ApiClient {

    // ‼️【【【【 您的Zhiyun Media Server服务器地址！ 】】】】‼️
    private const val BASE_URL = "http://117.50.85.132:3000"

    // 使用 lazy 属性，确保Retrofit实例只在第一次被调用时创建一次，保证线程安全和性能。
    val instance: ApiService by lazy {
        // 在这里可以配置OkHttpClient，例如设置连接超时、读取超时、添加日志拦截器等
        val okHttpClient = OkHttpClient.Builder()
            // .connectTimeout(30, TimeUnit.SECONDS) // 示例：设置连接超时
            // .readTimeout(30, TimeUnit.SECONDS)    // 示例：设置读取超时
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // 使用我们配置好的OkHttpClient
            .addConverterFactory(GsonConverterFactory.create()) // 将服务器返回的JSON字符串转换为Kotlin数据对象
            .build()

        // 创建并返回 ApiService 接口的实现，后续所有网络请求都通过这个实例发起
        retrofit.create(ApiService::class.java)
    }
}


