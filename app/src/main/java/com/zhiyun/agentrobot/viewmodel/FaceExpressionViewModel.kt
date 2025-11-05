// =================================================================================
// 文件路径: app/src/main/java/com/zhiyun/agentrobot/viewmodel/FaceExpressionViewModel.kt
// 【V3.0 · 终极完整替换版】
// =================================================================================
package com.zhiyun.agentrobot.viewmodel

import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.CommandListener
import com.ainirobot.coreservice.client.person.PersonApi
import com.ainirobot.coreservice.client.person.PersonListener
import com.ainirobot.coreservice.client.person.PersonUtils
import com.zhiyun.agentrobot.data.network.ApiClient // ✅ 【确保】您的项目中存在这个Retrofit单例类
import com.zhiyun.agentrobot.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import com.zhiyun.agentrobot.data.network.EmoticonApiClient

class FaceExpressionViewModel : ViewModel() {
    private val TAG = "FaceExpressionVM_V3" // ✅ 版本号升级

    // 状态播报员，向UI层报告作战进展
    private val _statusText = MutableStateFlow("待机中，请点击“表情包合影”")
    val statusText = _statusText.asStateFlow()

    // 战利品展示台1：用于存放捕获的【原始人脸】照片，用于拍照成功后立即展示
    private val _capturedFace = MutableStateFlow<Bitmap?>(null)
    val capturedFace = _capturedFace.asStateFlow()

    // 战利品展示台2：用于存放由“即梦AI”生成的【最终表情包】
    private val _finalEmoticon = MutableStateFlow<Bitmap?>(null)
    val finalEmoticon = _finalEmoticon.asStateFlow()

    // 战利品展示台3：用于存放根据表情包URL生成的【二维码】
    private val _qrCode = MutableStateFlow<Bitmap?>(null)
    val qrCode = _qrCode.asStateFlow()

    // 请求ID生成器
    private var reqId = 0

    /**
     * ✅ 总攻入口：这是我们从UI层（GuideActivity）发起的唯一攻击指令！
     */
    fun startFaceCaptureProcess() {
        if (_statusText.value.contains("正在")) {
            Log.w(TAG, "流程已在进行中，请勿重复点击")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 重置所有战利品
                _capturedFace.value = null
                _finalEmoticon.value = null
                _qrCode.value = null

                // 1. 启动人脸检测，并等待获取最佳人脸的faceId
                _statusText.value = "请您正对机器人，正在检测人脸..."
                val faceId = detectBestFaceId()
                if (faceId == -1) {
                    _statusText.value = "未检测到清晰人脸，请调整姿势后重试"
                    return@launch
                }

                // 2. 使用faceId获取照片路径
                _statusText.value = "检测成功！正在为您拍照..."
                val picturePath = getPicturePathById(faceId)
                if (picturePath == null) {
                    _statusText.value = "拍照失败，无法获取照片路径"
                    return@launch
                }

                // 3. 使用我军的ImageUtils将路径转换为Bitmap
                _statusText.value = "拍照成功！正在处理照片..."
                val faceBitmap = ImageUtils.getBitmapFromPath(picturePath)
                if (faceBitmap == null) {
                    _statusText.value = "照片处理失败，无法生成图片"
                    return@launch
                }

                // ✅ 立即将捕获的原始人脸展示给UI，提供即时反馈
                _capturedFace.value = faceBitmap

                // ✅ 从这里转入联合作战流程！
                _statusText.value = "成功获取头像！正在准备上传..."
                createEmoticonWithJimengAI(faceBitmap)

            } catch (e: Exception) {
                Log.e(TAG, "表情包制作流程发生未知错误: ", e)
                _statusText.value = "发生未知错误: ${e.message}"
            }
        }
    }

    /**
     * ✅ 作战单元1: 启动人脸检测，直到找到最佳人脸或超时，返回 faceId
     */
    private suspend fun detectBestFaceId(): Int = suspendCancellableCoroutine { continuation ->
        val listener = object : PersonListener() {
            override fun personChanged() {
                if (!continuation.isActive) return
                PersonApi.getInstance().unregisterPersonListener(this) // 确保只执行一次
                val bestPerson = PersonUtils.getBestFace(PersonApi.getInstance().getAllPersons())
                if (bestPerson != null) {
                    Log.i(TAG, "检测到最佳人脸, ID: ${bestPerson.id}")
                    continuation.resume(bestPerson.id)
                } else {
                    Log.w(TAG, "视野内人员变化，但未找到符合要求的最佳人脸")
                    continuation.resume(-1)
                }
            }
        }
        continuation.invokeOnCancellation {
            Log.d(TAG, "detectBestFaceId 协程被取消，注销PersonListener")
            PersonApi.getInstance().unregisterPersonListener(listener)
        }
        PersonApi.getInstance().registerPersonListener(listener)
        Log.i(TAG, "PersonListener 已注册，等待人员变化...")
    }

    /**
     * ✅ 作战单元2: 根据faceId，调用官方API获取照片的本地路径
     */
    private suspend fun getPicturePathById(faceId: Int): String? = suspendCancellableCoroutine { continuation ->
        RobotApi.getInstance().getPictureById(reqId++, faceId, 1, object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                try {
                    val json = JSONObject(message)
                    if (Definition.RESPONSE_OK == json.optString("status")) {
                        val pictures = json.optJSONArray("pictures")
                        if (pictures != null && pictures.length() > 0) {
                            val path = pictures.optString(0)
                            if (!TextUtils.isEmpty(path)) {
                                Log.i(TAG, "成功获取照片路径: $path")
                                continuation.resume(path)
                                return
                            }
                        }
                    }
                    Log.e(TAG, "获取照片路径失败, 返回的JSON不符合预期: $message")
                    continuation.resume(null)
                } catch (e: Exception) {
                    Log.e(TAG, "解析照片路径JSON失败", e)
                    continuation.resume(null)
                }
            }
        })
    }

    /**
     * ✅ 作战单元3: 【总控中心】 - 负责协调“上传->提交->轮询”的完整流程
     */
    private suspend fun createEmoticonWithJimengAI(faceBitmap: Bitmap) {
        try {
            // 1. 建立桥头堡：上传图片到我方服务器，获取URL
            val imageUrl = uploadImageAndGetUrl(faceBitmap)
            if (imageUrl == null) {
                _statusText.value = "头像上传失败，请重试"
                return
            }

            // 2. 发射导弹：提交任务到“即梦AI”
            val taskId = submitJimengTask(imageUrl)
            if (taskId == null) {
                _statusText.value = "任务提交失败，请检查网络"
                return
            }

            // 3. 持续追踪：轮询“即梦AI”任务结果
            val finalImageUrl = pollJimengResult(taskId)
            if (finalImageUrl == null) {
                _statusText.value = "创作失败或超时，请稍后重试"
                return
            }

            // 4. 胜利收尾 (下一步实现)
            _statusText.value = "创作完成！正在为您生成分享二维码..."
            Log.i(TAG, "最终表情包URL: $finalImageUrl")
            // TODO: 在此调用Glide/Coil从finalImageUrl加载图片到 _finalEmoticon
            // TODO: 在此调用zxing将finalImageUrl生成二维码到 _qrCode

        } catch (e: Exception) {
            Log.e(TAG, "即梦AI流程出错: ", e)
            _statusText.value = "发生未知错误: ${e.message}"
        } finally {
            // ✅ 注意：此处不再回收原始的Bitmap，因为它正被UI显示。
            // 可以在下一次流程开始时，或ViewModel销毁时统一处理。
            // if (!faceBitmap.isRecycled) { faceBitmap.recycle() }
        }
    }

    /**
     * ✅ 作战单元4: 【实战版上传模块】 - 上传Bitmap到我方Zhiyun Media Server
     */
    private suspend fun uploadImageAndGetUrl(bitmap: Bitmap): String? {
        _statusText.value = "正在上传头像至我方服务器..."

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()
        val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", "user_face.jpg", requestFile)

        return try {
            val response = EmoticonApiClient.instance.uploadImage(body)
            if (response.isSuccessful && response.body()?.success == true) {
                val imageUrl = response.body()?.url
                if (!imageUrl.isNullOrEmpty()) {
                    Log.i(TAG, "图片上传成功！URL: $imageUrl")
                    imageUrl
                } else {
                    Log.e(TAG, "服务器上传成功，但返回的URL为空")
                    null
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "图片上传失败: Code=${response.code()}, Body=$errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "图片上传时发生网络异常", e)
            null
        }
    }

    /**
     * ✅ 作战单元5: 【与“即梦AI”交互的模拟模块】
     * ‼️ 注意：此处仍为模拟实现，因火山引擎的签名鉴权逻辑复杂，需单独处理。
     */
    private suspend fun submitJimengTask(imageUrl: String): String? {
        _statusText.value = "正在向即梦AI提交任务..."
        delay(500) // 模拟0.5秒的网络耗时
        Log.i(TAG, "模拟提交任务成功！Image URL: $imageUrl")
        return "mock-task-id-98765" // 返回模拟的任务ID
    }

    private suspend fun pollJimengResult(taskId: String): String? {
        val maxRetries = 20
        for (i in 1..maxRetries) {
            _statusText.value = "AI创作中... (进度 ${i * 5}%)"
            delay(2000) // 模拟等待2秒
            if (i == 3) {
                Log.i(TAG, "模拟查询成功！Task ID: $taskId, 状态 'done'.")
                return "https://mock.final-emoticon-url.com/results/final_image.jpg"
            } else {
                Log.d(TAG, "模拟查询中... 任务状态 'generating'.")
            }
        }
        return null // 超时
    }
}
