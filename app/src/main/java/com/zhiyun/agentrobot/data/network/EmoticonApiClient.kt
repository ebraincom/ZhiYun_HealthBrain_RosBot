// =================================================================================
// 文件路径: app/src/main/java/com/zhiyun/agentrobot/data/network/EmoticonApiClient.kt
// ✨✨✨ V11.0 - 无域名决胜版 - 客户端请求实现最终形态 ✨✨✨
// =================================================================================
package com.zhiyun.agentrobot.data.network

import android.util.Log
import com.zhiyun.agentrobot.data.model.OurServerQueryResponse
import com.zhiyun.agentrobot.data.model.OurServerSubmitResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * ✨ V11.0: 客户端网络请求实现最终形态。
 * 核心变化：提供一个全新的 `generateEmoticon` 函数，负责将文件和文本打包成 form-data 请求。
 */
object EmoticonApiClient {

    // ‼️‼️‼️ 【无需修改】您的服务器IP地址和端口配置正确 ‼️‼️‼️
    private const val BASE_URL = "http://117.50.85.132:3000"

    // ✅ 【保留】您的长超时OkHttpClient配置非常正确，对于AI任务至关重要！
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // 建议将超时时间与服务器侧对齐，比如60秒
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // ✅ 【保留】Retrofit实例的创建逻辑不变
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ✅ 【保留】对外暴露的ApiService实例不变
    private val apiService: EmoticonApiService by lazy {
        retrofit.create(EmoticonApiService::class.java)
    }

    // ==============================================================================
    // ‼️‼️‼️ 【核心决胜函数】 - 这是我们所有修改的最终成果 ‼️‼️‼️
    // ==============================================================================

    /**
     * 【决胜函数】: 提交AI生成任务。
     * ViewModel将调用此函数，传入 prompt 和图片文件。
     *
     * @param prompt 提示词字符串。
     * @param imageFile 要上传的图片文件对象。
     * @return 返回服务器的响应，已由Retrofit自动解析为OurServerSubmitResponse。
     */
    suspend fun generateEmoticon(prompt: String, imageFile: File): Response<OurServerSubmitResponse>? {
        Log.d("ApiClient_V11", "准备提交AI任务, prompt: $prompt, filePath: ${imageFile.absolutePath}")

        return try {
            // 1. ✅ 将 prompt 字符串包装成文本类型的 RequestBody
            val promptRequestBody = prompt.toRequestBody("text/plain".toMediaTypeOrNull())

            // 2. ✅ 将图片文件包装成图片类型的 RequestBody，再封装成 MultipartBody.Part
            val imageRequestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull()) // 假设是jpeg，也可以是 "image/png"
            val imagePart = MultipartBody.Part.createFormData(
                // 这个 "image_file" 字符串【必须】和您服务器 main.py 中定义的参数名完全一致！
                // @router.post(...)
                // async def generate_emoticon_endpoint(..., image_file: UploadFile = File(...))
                "image_file",
                imageFile.name,
                imageRequestBody
            )

            // 3. ✅ 调用我们已在 EmoticonApiService.kt 中定义好的【全新】接口
            Log.d("ApiClient_V11", "正在调用apiService.generateEmoticon...")
            val response = apiService.generateEmoticon(promptRequestBody, imagePart)
            Log.d("ApiClient_V11", "收到服务器响应: ${response.code()} - ${response.body()}")
            response

        } catch (e: Exception) {
            Log.e("ApiClient_V11", "提交AI任务时发生严重异常", e)
            null // 发生异常时返回null，上层可以据此判断
        }
    }

    /**
     * 【保留函数】: 查询任务结果。
     * 这个函数的逻辑完全不变，继续为我们服务。
     */
    suspend fun getTaskResult(taskId: String): Response<OurServerQueryResponse>? {
        Log.d("ApiClient_V11", "准备查询任务结果, Task ID: $taskId")
        return try {
            apiService.getTaskResult(taskId)
        } catch (e: Exception) {
            Log.e("ApiClient_V11", "查询任务结果时发生严重异常", e)
            null
        }
    }
}
