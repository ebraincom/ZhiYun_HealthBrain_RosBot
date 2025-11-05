// =================================================================================
// 文件路径: app/src/main/java/com/zhiyun/agentrobot/data/network/EmoticonApiService.kt
// 【V1.0 · 全新创建 - 专属兵工厂】
// =================================================================================
package com.zhiyun.agentrobot.data.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * ✅ 为【图片上传】接口定义的数据模型。
 * 这是我们本次新增功能专属的数据模型。
 */
data class EmoticonUploadResponse(
    val success: Boolean,
    val url: String?
)

/**
 * ✅ 【表情包专用兵工厂】
 * 只负责与“表情包合影”功能相关的网络请求。
 */
interface EmoticonApiService {

    /**
     * ✅ 上传图片到我们的Zhiyun Media Server。
     */
    @Multipart
    @POST("/api/v1/upload-image/") // ‼️ 使用您最终在服务器上确定的图片上传接口路径！
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<EmoticonUploadResponse> // ✅ 指向我们在此文件中定义的专属Response
}
