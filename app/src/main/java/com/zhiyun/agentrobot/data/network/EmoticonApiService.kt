// =================================================================================
// 文件路径: app/src/main/java/com/zhiyun/agentrobot/data/network/EmoticonApiService.kt
// ✨✨✨ V11.0 - 无域名决胜版 - 接口定义最终形态 ✨✨✨
// =================================================================================
package com.zhiyun.agentrobot.data.network

// ‼️ [修改1/3] 删除了不再需要的 OurServerGenerateRequest
import com.zhiyun.agentrobot.data.model.OurServerQueryResponse
import com.zhiyun.agentrobot.data.model.OurServerSubmitResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody // ‼️ [修改2/3] 引入了 RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * ✨ V11.0: 接口定义最终形态。
 * 核心变化：废除旧的 "上传-生成" 两步法，统一为单一的、高效的 form-data 提交接口。
 */
interface EmoticonApiService {

    // ‼️ [修改3/3] 这是我们将要使用的【唯一】的提交任务接口
    /**
     * 【决胜接口】: 通过 multipart/form-data 方式，一次性提交图片文件和prompt，创建AI生成任务。
     *
     * 1. 必须添加 @Multipart 注解，声明这是一个表单上传接口。
     * 2. 使用 @Part 注解来定义每一个表单部分。
     * 3. 接口的返回值是您已有的 OurServerSubmitResponse，这非常完美！
     */
    @Multipart
    @POST("/api/v1/generate-emoticon")
    suspend fun generateEmoticon(
        // "prompt" 是和后端 main.py 中 Form(...) 参数名对应的
        @Part("prompt") prompt: RequestBody,

        // "image_file" 是和后端 main.py 中 File(...) 参数名对应的
        @Part imageFile: MultipartBody.Part
    ): Response<OurServerSubmitResponse> // 使用Response<T>包裹，可以更好地处理网络错误


    /**
     * 【保留接口】: 查询【我方服务器】的任务结果。
     * 这个接口的功能和路径完全不变，我们保留它。
     */
    @GET("/api/v1/get-task-result/{task_id}")
    suspend fun getTaskResult(@Path("task_id") taskId: String): Response<OurServerQueryResponse>


    // =================================================================================
    // ‼️‼️ 以下旧接口已被【光荣废除】，在新战略下不再需要 ‼️‼️
    // =================================================================================
    /*
    @Multipart
    @POST("/api/v1/upload-image/")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<UploadResponse>

    @POST("/api/v1/generate-emoticon")
    suspend fun generateEmoticon(@Body request: OurServerGenerateRequest): OurServerSubmitResponse
    */
}

/**
 * 辅助数据类：UploadResponse 已经不再需要，因为我们不再单独上传图片。
 * 如果您项目的其他地方没有用到它，可以将它删除或注释掉，以保持代码整洁。
 */
/*
data class UploadResponse(
    val success: Boolean,
    val url: String?,
    val message: String?
)
*/

