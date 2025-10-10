package com.zhiyun.agentrobot.data.network
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端单例对象。
 * 负责创建和配置Retrofit实例。
 */
object RetrofitClient {

    // 心知天气API的基础URL
    private const val BASE_URL = "https://api.seniverse.com/"

    // 创建一个日志拦截器，用于在Logcat中打印网络请求和响应的详细信息，方便调试
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 设置日志级别为BODY，会打印所有信息
    }

    // 配置OkHttpClient，添加日志拦截器并设置超时时间
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS) // 连接超时
        .readTimeout(10, TimeUnit.SECONDS)    // 读取超时
        .build()

    // 使用懒加载方式创建Retrofit实例，只有在第一次使用时才会初始化
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // 使用我们配置好的OkHttpClient
            .addConverterFactory(GsonConverterFactory.create()) // 添加GSON转换器，用于解析JSON
            .build()

        retrofit.create(ApiService::class.java)
    }
}
