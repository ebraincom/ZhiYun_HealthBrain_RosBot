// =================================================================================
// 文件路径: app/src/main/java/com/zhiyun/agentrobot/viewmodel/FaceExpressionViewModel.kt
// ✨✨✨ V19.0 · 重构版本- 完整、正确、取得完胜！ ✨✨✨
// =================================================================================
package com.zhiyun.agentrobot.viewmodel
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.zhiyun.agentrobot.data.network.EmoticonApiClient
import com.zhiyun.agentrobot.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import com.zhiyun.agentrobot.util.RobotInteractionHelper // 导入我们全新的、独立的Helper

// ‼️‼️‼️【V16.0 最终编译通过版】: TAG升级，纪念这次来之不易的最终胜利！‼️‼️‼️
class FaceExpressionViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "FaceExpressionVM_V19_REFACTORED"    // 升级版本号，纪念这次精准重构！

    private val _statusText = MutableStateFlow("待机中，请点击“表情包合影”")
    val statusText = _statusText.asStateFlow()

    private val _capturedFace = MutableStateFlow<Bitmap?>(null)
    val capturedFace = _capturedFace.asStateFlow()

    private val _finalEmoticon = MutableStateFlow<Bitmap?>(null)
    val finalEmoticon: StateFlow<Bitmap?> = _finalEmoticon.asStateFlow()

    private val _qrCode = MutableStateFlow<Bitmap?>(null)
    val qrCode: StateFlow<Bitmap?> = _qrCode.asStateFlow()


    // ‼️‼️‼️【V14.0 核心改造 B】: 构建“新陈代谢”机制！‼️‼️‼️
    fun resetState() {
        viewModelScope.launch(Dispatchers.Main) {
            Log.i(TAG, "‼️‼️‼️【状态重置】‼️‼️‼️ 执行 resetState，准备迎接下一次任务！")
            _statusText.value = "待机中，请点击“表情包合影”"
            _capturedFace.value = null
            _finalEmoticon.value = null
            _qrCode.value = null
        }
    }

    fun startFaceCaptureProcess() {
        if (_statusText.value.contains("正在")) {
            Log.w(TAG, "流程已在进行中，请勿重复点击")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _capturedFace.value = null
                    _finalEmoticon.value = null
                    _qrCode.value = null
                    _statusText.value = "请您正对机器人，正在检测人脸..."
                }
                // --------------------- 改造区域 · 开始 ---------------------
                // 原来: 调用ViewModel内部的私有函数
                // 现在: 调用全局唯一的、独立的 RobotInteractionHelper 单例！
                Log.d(TAG, "【重构调用】向 RobotInteractionHelper 发出 'detectBestFaceId' 指令...")
                val faceId = RobotInteractionHelper.detectBestFaceId()
                if (faceId == -1) {
                    withContext(Dispatchers.Main) {
                        _statusText.value = "未检测到清晰人脸，请调整姿势后重试"
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) { _statusText.value = "检测成功！正在为您拍照..." }

                // 【改造点】通过 robotHelper 调用
                Log.d(TAG, "【重构调用】向 RobotInteractionHelper 发出 'getPicturePathById' 指令, faceId: $faceId")
                val picturePath = RobotInteractionHelper.getPicturePathById(faceId)
                if (picturePath == null) {
                    withContext(Dispatchers.Main) {
                        _statusText.value = "拍照失败，无法获取照片路径"
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) { _statusText.value = "拍照成功！正在处理照片..." }

                val faceBitmap = ImageUtils.getBitmapFromPath(picturePath)

                // ‼️‼️‼️【V14.0 核心改造 A】: 植入“现场清理”模块！‼️‼️‼️
                if (faceBitmap != null) {
                    try {
                        val sdkPhotoFile = File(picturePath)
                        if (sdkPhotoFile.exists() && sdkPhotoFile.delete()) {
                            Log.i(TAG, "✅ 【现场清理】成功删除SDK照片文件: $picturePath")
                        } else {
                            Log.w(TAG, "⚠️ 【现场清理】SDK照片文件删除失败或不存在: $picturePath")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "【现场清理】删除SDK照片文件时发生异常", e)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _statusText.value = "照片处理失败，无法生成图片"
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    _capturedFace.value = faceBitmap
                    _statusText.value = "成功获取头像！正在准备上传..."
                }

                startAiGenerationProcess(
                    faceBitmap,
                    "一位时尚潮流的焦点人物，走在繁华的都市街头，背景是复古风格的涂鸦墙和温暖的街灯，动态抓拍瞬间，充满故事感和生活气息，质感细腻"
                )

            } catch (e: Exception) {
                Log.e(TAG, "表情包制作流程发生未知错误: ", e)
                withContext(Dispatchers.Main) { _statusText.value = "发生未知错误: ${e.message}" }
            }
        }
    }

    // =========================================================================================
    // ✅✅✅ 以下是您代码中真正执行网络请求的核心逻辑，我们保持其完整性 ✅✅✅
    // =========================================================================================
    fun startAiGenerationProcess(bitmap: Bitmap, prompt: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) { _statusText.value = "正在处理图片并提交AI任务..." }

            var imageFile: File? = null
            try {
                imageFile = withContext(Dispatchers.IO) {
                    val file = File.createTempFile(
                        "temp_ai_photo_",
                        ".jpg",
                        getApplication<Application>().cacheDir
                    )
                    FileOutputStream(file).use { stream ->
                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            90,
                            stream
                        )
                    }
                    Log.d(TAG, "【上传核心】Bitmap已成功保存为临时文件: ${file.absolutePath}")
                    file
                }

                if (imageFile != null) {
                    // 为了破坏服务器基于Prompt的缓存，我们在prompt末尾加入一个独一无二的时间戳“盐”。
                    // 这样，即使两次拍照的prompt完全相同，服务器也会因为这个“盐”而将它们视为两个独立的请求。
                    val uniquePrompt = "$prompt (TaskStamp: ${System.currentTimeMillis()})"
                    Log.i(TAG, "【缓存破坏】为本次请求生成了唯一的Prompt: $uniquePrompt")
                    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 【终极改造 · 增加随机盐】 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
                    val response = withContext(Dispatchers.IO) {
                        EmoticonApiClient.generateEmoticon(
                            uniquePrompt, // ‼️ 【核心】使用改造后的、带“盐”的uniquePrompt进行网络请求！
                            imageFile
                        )
                    }

                    if (response != null && response.isSuccessful && response.body()?.success == true) {
                        val taskId = response.body()?.data?.task_id
                        if (taskId.isNullOrEmpty().not()) {
                            Log.d(TAG, "🎉🎉🎉 胜利！任务创建成功！ Task ID: $taskId 🎉🎉🎉")
                            startPollingForTaskResult(taskId!!)
                        } else {
                            Log.e(TAG, "服务器提交成功，但返回的task_id为空")
                            withContext(Dispatchers.Main) {
                                _statusText.value = "服务器错误[无task_id]，请稍后重试"
                            }
                        }
                    } else {
                        val errorBody = response?.errorBody()?.string()
                        Log.e(TAG, "AI任务提交失败: Code=${response?.code()}, Body=$errorBody")
                        withContext(Dispatchers.Main) {
                            _statusText.value = "网络请求失败(${response?.code()})，请检查网络"
                        }
                    }
                } else {
                    Log.e(TAG, "创建临时图片文件失败，无法提交任务")
                    withContext(Dispatchers.Main) { _statusText.value = "创建临时文件失败" }
                }
            } catch (e: Exception) {
                Log.e(TAG, "AI生成流程发生异常", e)
                withContext(Dispatchers.Main) { _statusText.value = "发生未知错误，请重试" }
            } finally {
                withContext(Dispatchers.IO) {
                    if (imageFile?.exists() == true) {
                        imageFile.delete()
                        Log.d(TAG, "【上传核心】用于上传的临时图片文件已在流程最后被删除。")
                    }
                }
            }
        }
    }

    private suspend fun startPollingForTaskResult(taskId: String) {
        val maxAttempts = 20
        val delayMillis = 3000L

        for (attempt in 1..maxAttempts) {
            val resultResponse = withContext(Dispatchers.IO) {
                Log.d(TAG, "【轮询】第 $attempt 次查询任务结果, Task ID: $taskId")
                EmoticonApiClient.getTaskResult(taskId)
            }

            if (resultResponse != null && resultResponse.isSuccessful) {
                val resultBody = resultResponse.body()
                if (resultBody == null) {
                    Log.e(TAG, "【轮询失败】服务器返回了成功代码(200)，但响应体为null！")
                    withContext(Dispatchers.Main) { _statusText.value = "AI处理异常[响应体为空]" }
                    return
                }

                val responseData = resultBody.data
                if (responseData != null) {
                    val status = responseData.status
                    val finalImageUrls = responseData.image_urls

                    if (status == "success" || status == "done") {
                        if (finalImageUrls.isNullOrEmpty().not()) {
                            val firstImageUrl = finalImageUrls!![0]
                            Log.d(TAG, "🎉🎉🎉【最终胜利】🎉🎉🎉 成功获取最终图片URL: $firstImageUrl")
                            withContext(Dispatchers.Main) {
                                _statusText.value = "AI绘图成功！请扫码保存您的专属写真表情包"
                            }

                            // ‼️‼️‼️【V16.0 最终错误修复】: 使用 viewModelScope 启动并行任务！ ‼️‼️‼️
                            // 在suspend函数中，要启动一个与当前任务“并行”且生命周期与ViewModel绑定的新协程，
                            // 必须显式地使用 viewModelScope.launch。
                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    Log.d(TAG, "开始使用Glide加载最终图片...")
                                    val finalBitmap: Bitmap =
                                        Glide.with(getApplication<Application>().applicationContext)
                                            .asBitmap()
                                            .load(firstImageUrl)
                                            .timeout(30000)
                                            .submit()
                                            .get()
                                    withContext(Dispatchers.Main) {
                                        Log.d(TAG, "最终图片加载成功，更新_finalEmoticon状态！")
                                        _finalEmoticon.value = finalBitmap
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "使用Glide加载最终图片失败！", e)
                                    withContext(Dispatchers.Main) {
                                        _statusText.value = "图片加载失败，请检查网络"
                                    }
                                }
                            }
                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    Log.d(TAG, "开始使用ZXing生成二维码...")
                                    val qrCodeSize = 512
                                    val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
                                    val bitMatrix = MultiFormatWriter().encode(
                                        firstImageUrl,
                                        BarcodeFormat.QR_CODE,
                                        qrCodeSize,
                                        qrCodeSize,
                                        hints
                                    )
                                    val width = bitMatrix.width
                                    val height = bitMatrix.height
                                    val pixels = IntArray(width * height)
                                    for (y in 0 until height) {
                                        val offset = y * width
                                        for (x in 0 until width) {
                                            pixels[offset + x] =
                                                if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                                        }
                                    }
                                    val qrCodeBitmap =
                                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                    qrCodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                                    withContext(Dispatchers.Main) {
                                        Log.d(TAG, "二维码生成成功，更新_qrCode状态！")
                                        _qrCode.value = qrCodeBitmap
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "使用ZXing生成二维码失败！", e)
                                }
                            }
                            return // 流程结束
                        } else {
                            Log.e(TAG, "【轮询异常】服务器返回状态'success'，但图片URL列表为空！")
                            withContext(Dispatchers.Main) {
                                _statusText.value = "AI处理异常[无图片返回]"
                            }
                            return
                        }
                    } else if (status == "processing" || status == "in_queue") {
                        withContext(Dispatchers.Main) {
                            _statusText.value =
                                "AI正在创作中(${status})...(${attempt}/${maxAttempts})"
                        }
                        Log.d(TAG, "【轮询】服务器仍在处理中(状态:$status)，继续等待...")
                    } else {
                        Log.e(TAG, "【轮询失败】服务器返回任务失败状态: $status")
                        withContext(Dispatchers.Main) {
                            _statusText.value = "AI处理失败[状态:$status]"
                        }
                        return
                    }
                } else {
                    Log.e(TAG, "【轮询失败】服务器返回了成功代码，但 'data' 字段为空！")
                    withContext(Dispatchers.Main) { _statusText.value = "AI处理异常[data为空]" }
                    return
                }
            } else {
                Log.e(TAG, "【轮询失败】网络请求失败, Code: ${resultResponse?.code()}")
                withContext(Dispatchers.Main) { _statusText.value = "查询结果失败[网络错误]" }
                return
            }

            delay(delayMillis)
        }

        Log.w(TAG, "【轮询超时】超过最大尝试次数，未能获取任务结果。")
        withContext(Dispatchers.Main) { _statusText.value = "AI任务超时，请稍后重试" }
    }
}


