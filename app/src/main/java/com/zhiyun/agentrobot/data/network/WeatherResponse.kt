package com.zhiyun.agentrobot.data.network

import com.google.gson.annotations.SerializedName

/**
 * 用于GSON解析天气API返回的JSON数据。
 * 我们只定义我们关心的字段。
 */
data class WeatherResponse(
    @SerializedName("results")
    val results: List<WeatherResult>?
)

data class WeatherResult(
    @SerializedName("daily")
    val daily: List<DailyWeather>?
)

data class DailyWeather(
    @SerializedName("date")
    val date: String, // "2025-10-10"

    @SerializedName("text_day")
    val weatherText: String, // "多云"

    @SerializedName("code_day")
    val weatherCode: String, // 天气代码

    @SerializedName("high")
    val tempHigh: String, // 最高温度

    @SerializedName("low")
    val tempLow: String // 最低温度
)
