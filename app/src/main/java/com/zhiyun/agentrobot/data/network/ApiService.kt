package com.zhiyun.agentrobot.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit的API服务接口。
 * 这里定义了我们App中所有的网络请求。
 */
interface ApiService {

    /**
     * 获取天气信息。我们暂时借用这个免费API来获取包含日期的数据，
     * 以模拟真实的限行查询。
     *
     * 这是一个GET请求，访问的完整URL会是：
     * https://api.seniverse.com/v3/weather/daily.json?key=你的私钥&location=beijing&language=zh-Hans&unit=c&start=0&days=1
     */
    @GET("v3/weather/daily.json")
    suspend fun getBeijingWeather(
        @Query("key") apiKey: String,
        @Query("location") city: String = "beijing",
        @Query("language") lang: String = "zh-Hans",
        @Query("unit") unit: String = "c",
        @Query("start") start: Int = 0,
        @Query("days") days: Int = 1
    ): WeatherResponse // 注意：我们很快就会创建这个WeatherResponse数据类
}
