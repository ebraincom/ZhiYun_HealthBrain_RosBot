// 文件路径: app/src/main/java/com/zhiyun/agentrobot/data/model/OurServerModels.kt
package com.zhiyun.agentrobot.data.model

/**
 * ✅【V2.5】: 用于请求【我方服务器】的数据模型
 */
data class OurServerGenerateRequest(
    val image_url: String,
    // ‼️【最终版提示词 - 待您确认后填入】
    val prompt: String = "一位时尚潮流的焦点人物，走在繁华的都市街头，背景是复古风格的涂鸦墙和温暖的街灯，动态抓拍瞬间，充满故事感和生活气息，质感细腻"
)

/**
 * ✅【V2.5】: 【我方服务器】返回的提交任务响应
 */
data class OurServerSubmitResponse(
    val success: Boolean,
    val task_id: String?,
    val error: String?
)

/**
 * ✅【V2.5】: 【我方服务器】返回的查询任务响应
 */
data class OurServerQueryResponse(
    val success: Boolean,
    val status: String?,
    val image_urls: List<String>?,
    val error: String?
)


