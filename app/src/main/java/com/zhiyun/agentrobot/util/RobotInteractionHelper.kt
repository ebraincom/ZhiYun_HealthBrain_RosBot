// 文件路径: com/zhiyun/agentrobot/util/RobotInteractionHelper.kt
package com.zhiyun.agentrobot.util

import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.CommandListener
import com.ainirobot.coreservice.client.person.PersonApi
import com.ainirobot.coreservice.client.person.PersonListener
import com.ainirobot.coreservice.client.person.PersonUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * 机器人交互的独立单例帮助类 (V2.0 - 完全体)
 * 职责：封装所有与机器人硬件API及相关网络API的直接交互。
 */
object RobotInteractionHelper {

    private const val TAG = "RobotInteractionHelper"
    private var reqId = 0 // reqId 内聚于此，自我管理

    // --- “传令兵终身制”相关代码，保持不变 ---
    private var personListener: PersonListener? = null
    private var onFaceDetectedCallback: ((Int) -> Unit)? = null

    fun init() {
        if (personListener != null) return
        Log.i(TAG, "Initializing RobotInteractionHelper and registering the one and only PersonListener...")
        personListener = object : PersonListener() {
            override fun personChanged() {
                val bestPerson = PersonUtils.getBestFace(PersonApi.getInstance().getAllPersons())
                onFaceDetectedCallback?.invoke(bestPerson?.id ?: -1)
            }
        }.also { PersonApi.getInstance().registerPersonListener(it) }
    }

    fun release() {
        personListener?.let { PersonApi.getInstance().unregisterPersonListener(it) }
        personListener = null
    }

    suspend fun detectBestFaceId(): Int = suspendCancellableCoroutine { continuation ->
        onFaceDetectedCallback = { faceId ->
            if (continuation.isActive) {
                onFaceDetectedCallback = null
                continuation.resume(faceId)
            }
        }
        continuation.invokeOnCancellation { onFaceDetectedCallback = null }
        val currentBestPerson = PersonUtils.getBestFace(PersonApi.getInstance().getAllPersons())
        currentBestPerson?.id?.let { onFaceDetectedCallback?.invoke(it) }
    }

    suspend fun getPicturePathById(faceId: Int): String? = suspendCancellableCoroutine { continuation ->
        RobotApi.getInstance().getPictureById(reqId++, faceId, 1, object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                if (!continuation.isActive) return
                try {
                    val json = JSONObject(message)
                    if (Definition.RESPONSE_OK == json.optString("status")) {
                        val path = json.optJSONArray("pictures")?.optString(0)
                        if (!TextUtils.isEmpty(path)) {
                            continuation.resume(path)
                            return
                        }
                    }
                    continuation.resume(null)
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            }
        })
    }

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 【全新的、集权后的AI生成流程】 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    /**
     * 【全新】执行完整的AI图片生成流程，返回最终的Bitmap。
     * 这是一个高级封装，ViewModel只需调用这一个函数。
     */
    suspend fun createAiEmoticon(faceBitmap: Bitmap, prompt: String): Bitmap? {
        // 1. 上传图片
        val imageUrl = uploadImageAndGetUrl(faceBitmap)
        if (imageUrl == null) {
            Log.e(TAG, "【AI流程】上传图片失败")
            return null
        }

        // 2. 提交AI任务
        val taskId = submitJimengTask(imageUrl, prompt)
        if (taskId == null) {
            Log.e(TAG, "【AI流程】提交AI任务失败")
            return null
        }

        // 3. 轮询任务结果
        return pollJimengResult(taskId)
    }

    // 将您之前在ViewModel中的私有函数，全部迁移并改造到Helper中
    private suspend fun uploadImageAndGetUrl(faceBitmap: Bitmap): String? {
        Log.d(TAG, "uploadImageAndGetUrl: 开始上传图片...")
        // *** 此处是您调用自己服务器API上传图片的代码 ***
        // *** 您需要将这部分逻辑从旧的ViewModel中复制过来 ***
        // 这是一个示例：
        // return MyApiClient.upload(faceBitmap)
        return "https://example.com/uploaded_image.jpg" // 临时占位符
    }

    private suspend fun submitJimengTask(imageUrl: String, prompt: String): String? {
        Log.d(TAG, "submitJimengTask: 提交任务到Jimeng AI...")
        // *** 此处是您调用计梦AI提交任务的代码 ***
        // *** 您需要将这部分逻辑从旧的ViewModel中复制过来 ***
        // 这是一个示例：
        // return MyJimengClient.submit(reqId++, imageUrl, prompt)
        return "task_12345" // 临时占位符
    }

    private suspend fun pollJimengResult(taskId: String): Bitmap? {
        Log.d(TAG, "pollJimengResult: 开始轮询任务结果, taskId: $taskId")
        val maxAttempts = 10
        var attempt = 0
        while (attempt < maxAttempts) {
            Log.d(TAG, "轮询... 第 ${attempt + 1} 次")
            // *** 此处是您调用计梦AI查询任务状态的代码 ***
            // *** 您需要将这部分逻辑从旧的ViewModel中复制过来 ***
            // 这是一个示例：
            // val response = MyJimengClient.query(reqId++, taskId)
            // if (response.isSuccess) {
            //     return ImageUtils.getBitmapFromUrl(response.imageUrl)
            // }
            delay(3000) // 延迟3秒
            attempt++
        }
        Log.e(TAG, "轮询超时，未能获取到最终图片")
        return null // 超时返回null
    }
}
