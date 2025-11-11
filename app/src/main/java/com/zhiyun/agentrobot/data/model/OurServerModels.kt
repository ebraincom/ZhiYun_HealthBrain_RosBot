// 文件路径: app/src/main/java/com/zhiyun/agentrobot/data/model/OurServerModels.kt
package com.zhiyun.agentrobot.data.model

// =================================================================================
// ✨✨✨ V12.0 - 决战版 - 数据模型修正 ✨✨✨
// 本次修改的核心是让数据模型与Python服务器返回的JSON结构【完全匹配】
// 解决了因模型不匹配导致的GSON解析失败、返回null的问题。
// =================================================================================


// =================================================================================
// 提交任务的响应模型 (Submit Response)
// =================================================================================

/**
 * ‼️‼️‼️ 【V12.0 已修正】: 用于解析【创建任务】时，我方服务器返回的响应。
 * 对应服务器JSON: {'success': True, 'message': '任务创建成功', 'data': {'task_id': '...'}}
 */
data class OurServerSubmitResponse(
    val success: Boolean,
    val message: String?, // 对应服务器返回的 'message'
    val data: SubmitData?  // 对应服务器返回的 'data' 对象
)

/**
 * ‼️‼️‼️ 【V12.0 新增】: SubmitResponse的子模型，用于解析 'data' 字段。
 */
data class SubmitData(
    val task_id: String? // 对应 'data' 对象里的 'task_id'
)


// =================================================================================
// 查询任务的响应模型 (Query Response)
// =================================================================================

/**
 * ‼️‼️‼️ 【V12.0 已修正】: 用于解析【查询任务】时，我方服务器返回的响应。
 * 这是解决【轮询失败】问题的核心！
 * 这个模型的结构现在与您的Python服务器返回的JSON结构完全一致！
 *
 * 对应服务器JSON:
 * {
 *   "code": 10000,
 *   "message": "Success",
 *   "data": {
 *     "status": "in_queue",
 *     "image_urls": null,
 *     ...
 *   },
 *   ...
 * }
 */
data class OurServerQueryResponse(
    val code: Int,                // 对应顶层的 'code'
    val message: String?,         // 对应顶层的 'message'
    val data: QueryTaskData?      // 对应顶层的 'data' 对象，我们用下面的子模型来解析它
)

/**
 * ‼️‼️‼️ 【V12.0 新增】: OurServerQueryResponse的子模型，用于解析 'data' 字段。
 * 这个模型专门负责解析【火山引擎】返回的、被我方服务器包装在 'data' 字段里的核心信息。
 */
data class QueryTaskData(
    val status: String?,          // "in_queue", "processing", "success", "failed"
    val image_urls: List<String>? // 任务成功时，这里将包含最终的图片URL列表
)


// =================================================================================
// ✨✨✨【历史遗留模型 - 暂不使用】✨✨✨
// 下面的模型是旧版本的设计，当前新的 "form-data" 提交流程已不再使用它。
// 保留在此仅为历史参考。
// =================================================================================

/**
 * ❌【V12.0 已废弃】: 旧版本用于请求【我方服务器】的数据模型。
 * 当前我们使用 MultipartBody 直接构建请求，不再需要这个模型。
 */
data class OurServerGenerateRequest(
    val image_url: String,
    val prompt: String
)
