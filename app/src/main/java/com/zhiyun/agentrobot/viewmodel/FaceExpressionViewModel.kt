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
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import android.app.Application
import androidx.lifecycle.AndroidViewModel


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
                // createEmoticonWithJimengAI(faceBitmap)
                startAiGenerationProcess(faceBitmap, "一位时尚潮流的焦点人物，走在繁华的都市街头，背景是复古风格的涂鸦墙和温暖的街灯，动态抓拍瞬间，充满故事感和生活气息，质感细腻")

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
        _statusText.value = "正在处理图片并提交AI任务..."

        // 在IO线程中执行文件操作和网络请求
        return withContext(Dispatchers.IO) {
            var imageFile: File? = null
            try {
                // 步骤1：将Bitmap保存为临时文件 (这是ApiClient所需要的)
                val tempDir = System.getProperty("java.io.tmpdir")
                val fileName = "temp_ai_photo_${java.util.UUID.randomUUID()}.jpg"
                imageFile = File(tempDir, fileName)
                FileOutputStream(imageFile).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                }

                // 步骤2：准备Prompt
                val prompt = "一位时尚潮流的焦点人物，走在繁华的都市街头，背景是复古风格的涂鸦墙和温暖的街灯，动态抓拍瞬间，充满故事感和生活气息，质感细腻"

                // 步骤3：【最终修正点】调用正确的ApiClient函数
                val response = EmoticonApiClient.generateEmoticon(prompt, imageFile)

                // 步骤4：处理返回结果
                if (response != null && response.isSuccessful && response.body()?.success == true) {
                    val taskId = response.body()?.task_id
                    if (!taskId.isNullOrEmpty()) {
                        Log.i(TAG, "🎉🎉🎉 AI任务提交成功！Task ID: $taskId")
                        // 如果您需要立即返回一个可用的URL，这里可能需要轮询或返回一个不同的值。
                        // 根据您服务器的逻辑，我们先假设返回 task_id 作为成功的标志。
                        taskId
                    } else {
                        Log.e(TAG, "服务器提交成功，但返回的task_id为空")
                        null
                    }
                } else {
                    val errorBody = response?.errorBody()?.string()
                    Log.e(TAG, "AI任务提交失败: Code=${response?.code()}, Body=$errorBody")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "图片上传或AI任务提交时发生网络异常", e)
                null
            } finally {
                // 步骤5：确保临时文件被删除
                imageFile?.delete()
            }
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
    // ==============================================================================
    // ‼️‼️‼️ 【嫁接上的新核心】 - 这是我们早已确认无误的AI任务提交函数 ‼️‼️‼️
    // ==============================================================================
    // ==============================================================================
    // ✅✅✅ 【V30.0 - 拨乱反正最终胜利版】 - 彻底抛弃错误上下文！ ✅✅✅
    // ==============================================================================
    private fun startAiGenerationProcess(bitmap: Bitmap, prompt: String) {
        // 启动一个顶层协程来管理整个异步流程
        viewModelScope.launch {
            // 通过 withContext(Dispatchers.Main) 来确保UI更新在主线程
            withContext(Dispatchers.Main) {
                _statusText.value = "正在处理图片并提交AI任务..."
            }

            var imageFile: File? = null
            try {
                // 步骤 1: 将Bitmap保存为临时文件 (切换到IO线程执行)
                imageFile = withContext(Dispatchers.IO) {
                    // ‼️‼️‼️ 【最终修正点】 ‼️‼️‼️
                    // 使用 File.createTempFile，它不需要任何Context！
                    // 这将在系统的临时目录中创建一个文件，例如 /data/user/0/com.zhiyun.agentrobot/cache/temp_ai_photo12345.jpg
                    val file = File.createTempFile("temp_ai_photo_", ".jpg")

                    java.io.FileOutputStream(file).use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    }
                    Log.d(TAG, "【新核心】Bitmap已成功保存为临时文件: ${file.absolutePath}")
                    file
                }

                // 步骤 2: 调用ApiClient提交任务 (仍在IO线程执行)
                // ✅ 确保 imageFile 不为null
                if (imageFile != null) {
                    val response = withContext(Dispatchers.IO) {
                        EmoticonApiClient.generateEmoticon(prompt, imageFile)
                    }

                    // 步骤 3: 处理提交结果，如果成功，则【等待】轮询完成！
                    if (response != null && response.isSuccessful && response.body()?.success == true) {
                        val taskId = response.body()?.task_id
                        if (!taskId.isNullOrEmpty()) {
                            Log.d(TAG, "🎉🎉🎉 胜利！任务创建成功！ Task ID: $taskId 🎉🎉🎉")

                            // 调用并等待我们的轮询函数执行完毕！
                            startPollingForTaskResult(taskId)

                        } else {
                            Log.e(TAG, "服务器提交成功，但返回的task_id为空")
                            withContext(Dispatchers.Main) { _statusText.value = "服务器错误[无task_id]，请稍后重试" }
                        }
                    } else {
                        val errorBody = response?.errorBody()?.string()
                        Log.e(TAG, "AI任务提交失败: Code=${response?.code()}, Body=$errorBody")
                        withContext(Dispatchers.Main) { _statusText.value = "网络请求失败(${response?.code()})，请检查网络" }
                    }
                } else {
                    Log.e(TAG, "创建临时图片文件失败，无法提交任务")
                    withContext(Dispatchers.Main) { _statusText.value = "创建临时文件失败" }
                }

            } catch (e: Exception) {
                Log.e(TAG, "AI生成流程发生异常", e)
                withContext(Dispatchers.Main) { _statusText.value = "发生未知错误，请重试" }
            } finally {
                // 步骤 4: 【万无一失】确保临时文件在所有操作结束后被删除 (切换到IO线程)
                withContext(Dispatchers.IO) {
                    if (imageFile?.exists() == true) {
                        imageFile.delete()
                        Log.d(TAG, "【新核心】临时图片文件已在流程最后被删除。")
                    }
                }
            }
        }
    }

    // ==============================================================================
    // ✅✅✅ 【V27.0 - 最终胜利版】 - 使用正确的 image_urls 字段！ ✅✅✅
    // ==============================================================================
    private suspend fun startPollingForTaskResult(taskId: String) {
        val maxAttempts = 20 // 最多尝试20次
        val delayMillis = 3000L // 每次间隔3秒

        for (attempt in 1..maxAttempts) {
            // 在IO线程中执行网络请求
            val resultResponse = withContext(Dispatchers.IO) {
                Log.d(TAG, "【轮询】第 $attempt 次查询任务结果, Task ID: $taskId")
                EmoticonApiClient.getTaskResult(taskId)
            }

            if (resultResponse != null && resultResponse.isSuccessful) {
                val resultBody = resultResponse.body() // resultBody 的类型现在是 OurServerQueryResponse?

                if (resultBody == null) {
                    Log.e(TAG, "【轮询失败】服务器返回了成功代码，但响应体为空！")
                    withContext(Dispatchers.Main) { _statusText.value = "AI处理异常[响应体为空]" }
                    return // 结束轮询
                }

                // ‼️‼️‼️【最终修正点】‼️‼️‼️
                // 我们现在严格按照您项目中已有的 OurServerQueryResponse 来处理
                if (resultBody.success) {
                    // ✅✅✅ 【最终胜利的钥匙】: 使用 image_urls (复数) 字段，并检查它是否不为空且包含元素！
                    val finalImageUrls = resultBody.image_urls
                    if (!finalImageUrls.isNullOrEmpty()) {
                        val firstImageUrl = finalImageUrls[0] // 取列表中的第一个URL
                        Log.d(TAG, "🎉🎉🎉【最终胜利】🎉🎉🎉 成功获取最终图片URL: $firstImageUrl")

                        withContext(Dispatchers.Main) {
                            _statusText.value = "AI绘图成功！请扫码或查看结果"
                            // TODO: 在这里处理最终的图片
                        }
                        return // 成功获取，结束轮询！
                    } else if (resultBody.status == "processing") {
                        // 服务器明确告知还在处理中，这是正常情况
                        withContext(Dispatchers.Main) {
                            _statusText.value = "AI正在创作中...(${attempt}/${maxAttempts})"
                        }
                        Log.d(TAG, "【轮询】服务器仍在处理中，继续等待...")
                    } else {
                        // 虽然 success = true，但没有 image_urls，也没有 processing 状态，作为异常处理
                        Log.e(TAG, "【轮询异常】服务器返回成功，但结果状态未知: ${resultBody.status}")
                        withContext(Dispatchers.Main) { _statusText.value = "AI处理异常[状态未知]" }
                        return
                    }

                } else {
                    // 服务器明确告知失败 (success = false)
                    Log.e(TAG, "【轮询失败】服务器返回失败: ${resultBody.error}")
                    withContext(Dispatchers.Main) { _statusText.value = "AI处理失败: ${resultBody.error}" }
                    return // 结束轮询
                }

            } else {
                // 网络请求失败
                Log.e(TAG, "【轮询失败】网络请求失败, Code: ${resultResponse?.code()}")
                withContext(Dispatchers.Main) { _statusText.value = "查询结果失败[网络错误]" }
                return // 结束轮询
            }

            // 等待一段时间再进行下一次查询
            kotlinx.coroutines.delay(delayMillis)
        }

        // 如果循环结束了还没拿到结果，就是超时了
        Log.w(TAG, "【轮询超时】超过最大尝试次数，未能获取任务结果。")
        withContext(Dispatchers.Main) { _statusText.value = "AI任务超时，请稍后重试" }
    }



    /**
     * 【嫁接上的新核心辅助函数】: 将Bitmap对象保存到系统临时目录中。
     */
    private suspend fun saveBitmapToTempFile(bitmap: Bitmap): File? = withContext(Dispatchers.IO) {
        try {
            // 因为ViewModel没有Android Context，我们使用Java的系统临时目录
            val tempDir = System.getProperty("java.io.tmpdir")

            // ✅✅✅ 【修正点 1/2】: 使用正确的 java.util.UUID！✅✅✅
            val fileName = "temp_ai_photo_${java.util.UUID.randomUUID()}.jpg"
            val file = File(tempDir, fileName)

            // ✅✅✅ 【修正点 2/2】: 使用完整的包名来调用FileOutputStream，避免import错误！✅✅✅
            java.io.FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }
            Log.d(TAG, "【新核心】Bitmap已成功保存为临时文件: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "【新核心】保存Bitmap到文件时发生异常", e)
            null
        }
    }

}