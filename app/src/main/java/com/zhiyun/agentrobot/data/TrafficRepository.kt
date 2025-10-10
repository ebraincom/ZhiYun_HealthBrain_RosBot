package com.zhiyun.agentrobot.data

import com.zhiyun.agentrobot.data.network.RetrofitClient
import java.util.Calendar

/**
 * * 【新增功能】专门为主页UI提供天气数据。
 *      * 为了方便UI使用，我们不再返回一个拼接好的字符串，
 *      * 而是返回一个包含天气信息的专用数据类。
 * 多功能数据仓库。
 * 现在它既能查询天气，也能查询限行。
 */
class TrafficRepository {

    // 【请再次确认这里是您自己的私钥】
    private val XINZHI_API_KEY = "SKJ4ceN06zyfjCPKt" // 再次提醒：请务必替换成您自己的私钥

    /**
     * 功能一：获取天气信息
     */
    suspend fun getWeatherInfo(city: String): String {
        return try {
            val weatherResponse = RetrofitClient.instance.getBeijingWeather(XINZHI_API_KEY, city)
            val todayWeather = weatherResponse.results?.firstOrNull()?.daily?.firstOrNull()

            if (todayWeather != null) {
                "为您查询到今天${city}的天气：${todayWeather.weatherText}，最高温${todayWeather.tempHigh}度，最低温${todayWeather.tempLow}度。"
            } else {
                "无法获取到有效的天气信息。"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "抱歉，查询天气时网络出错了。"
        }
    }

    /**
     * 功能二：获取限行信息 (我们重新把它加回来了！)
     */
    suspend fun getRestrictionInfo(city: String): String {
        // 为了简单，我们暂时不依赖网络API的日期，而是直接用系统日历
        // 这也展示了两种不同的数据来源处理方式
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val restrictionNumbers = when (dayOfWeek) {
            Calendar.MONDAY -> "4 和 9"
            Calendar.TUESDAY -> "5 和 0"
            Calendar.WEDNESDAY -> "1 和 6"
            Calendar.THURSDAY -> "2 和 7"
            Calendar.FRIDAY -> "3 和 8" // 注意：这里还是我们自己定义的规则
            else -> "不限行"
        }

        return if (restrictionNumbers == "不限行") {
            "今天是周末，${city}不限行。"
        } else {
            "根据我了解到的规则，今天${city}的限行尾号是：$restrictionNumbers。"
        }
    }
    suspend fun getWeatherInfoForHomepage(city: String): HomepageWeatherInfo? {
        return try {
            val weatherResponse = RetrofitClient.instance.getBeijingWeather(XINZHI_API_KEY, city)
            val todayWeather = weatherResponse.results?.firstOrNull()?.daily?.firstOrNull()

            if (todayWeather != null) {
                // 请求成功，构建并返回数据对象
                HomepageWeatherInfo(
                    weatherText = todayWeather.weatherText,
                    temperature = todayWeather.tempHigh // 主页我们只显示最高温度作为示例
                )
            } else {
                // API调用成功，但数据解析失败
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 网络请求等异常
            null
        }
    }
}
/**
 * 【新增数据类】专门用于主页天气显示的数据结构。
 * 这样UI层就不需要关心复杂的API响应体了。
 */
data class HomepageWeatherInfo(
    val weatherText: String, // "多云"
    val temperature: String  // "28"
)
