package com.zhiyun.agentrobot.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor


/**
 * 专用于媒体服务的Retrofit单例对象。
 * 它独立于天气等其他任何公共API服务，确保了职责的专一和架构的清晰。
 */
object MediaApiClient {

    // 我们的私有服务器外网IP地址和端口。
    private const val BASE_URL = "http://117.50.85.132:3000/"
    // private const val AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTc2MTE1OTEyOH0.jyIu1lE8MVJprL-e72h9tmxKae4qlBvWDSORMdkjbSc"

    // 日志拦截器，仅用于调试我们的媒体API。
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    // ▼▼▼【第二处改造：创建一个专门添加请求头的认证拦截器！】▼▼▼

    // OkHttp客户端，其配置专为我们的媒体服务。
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Retrofit实例，它只知道我们的媒体服务器。
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * 对外暴露 MediaApiService 的唯一实现。
     */
    val mediaApiService: MediaApiService by lazy {
        retrofit.create(MediaApiService::class.java)
    }
}


