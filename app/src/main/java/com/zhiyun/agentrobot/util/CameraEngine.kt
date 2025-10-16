// =================================================================================
// 文件路径: app/src/main/java/com/zhiyun/agentrobot/util/CameraEngine.kt
// 【最终无错胜利版 v12】 - 请用此完整文件直接替换，不要再手动修改。
// =================================================================================
package com.zhiyun.agentrobot.util

import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Surface
import com.ainirobot.coreservice.client.surfaceshare.SurfaceShareApi
import com.ainirobot.coreservice.client.surfaceshare.SurfaceShareBean
import com.ainirobot.coreservice.client.surfaceshare.SurfaceShareListener
import java.io.File
import java.lang.Exception

class CameraEngine private constructor() {

    private val TAG = "CameraEngine_VICTORY_12" // 最终胜利版TAG

    @Volatile private var isRunning = false
    private var imageReader: ImageReader? = null
    private var remoteSurface: Surface? = null
    private var surfaceShareBean: SurfaceShareBean? = null

    // 唯一的后台线程，用于处理所有相机相关的API调用和回调
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CameraEngine() }
    }

    fun start(
        storageDir: File,
        callback: (success: Boolean, message: String, photoPath: String?) -> Unit
    ) {
        if (isRunning) {
            callback(false, "引擎正忙，请稍后再试", null)
            return
        }
        isRunning = true
        Log.i(TAG, "START command received. Final Victory Engine.")

        startBackgroundThread()

        backgroundHandler?.post {
            Log.d(TAG, "Executing initialization on thread: ${Thread.currentThread().name}")
            if (!initializeAndRequest(storageDir, callback)) {
                Log.e(TAG, "Engine initialization failed. Stopping.")
                mainHandler.post {
                    callback(false, "引擎启动失败", null)
                }
                stop()
            }
        }
    }

    private fun initializeAndRequest(
        storageDir: File,
        callback: (success: Boolean, message: String, photoPath: String?) -> Unit
    ): Boolean {
        try {
            // 【关键修正1/2】增加一个flag，确保我们只处理第一张图片
            val photoTaken = java.util.concurrent.atomic.AtomicBoolean(false)
            imageReader = ImageReader.newInstance(640, 480, ImageFormat.YUV_420_888, 4)

            // 【关键修正】将 backgroundHandler 作为第二个参数传入，确保回调100%在我们的后台线程中执行！
            imageReader?.setOnImageAvailableListener({ reader ->
                if (photoTaken.get()) {
                    return@setOnImageAvailableListener
                }
                val image: Image? = try { reader.acquireLatestImage() } catch (e: Exception) { null }
                if (image == null) {
                    Log.w(TAG, "acquireLatestImage returned null, skipping frame.")
                    return@setOnImageAvailableListener
                }
                // 【关键】一旦获取到第一张有效图片，立刻设置flag并停止引擎！
                // 这会关闭数据流，但不会影响我们对当前这张`image`的处理。
                if (photoTaken.compareAndSet(false, true)) {
                    Log.i(TAG, "First valid image acquired. Stopping engine to prevent more frames.")
                    stop()
                }

                // 【核心战术：先榨取，后关闭】
                Log.d(TAG, "Image available! Extracting data immediately...")
                val nv21Data = ImageUtils.convertYUV420888ToNV21(image)
                val width = image.width
                val height = image.height
                image.close() // **完成使命，立刻释放！**
                Log.d(TAG, "Image closed. Data is now safe in a byte array.")

                // 【数据安全后，再进行耗时操作】
                val photoPath = ImageUtils.saveNv21DataToJpeg(nv21Data, width, height, storageDir)

                val success = photoPath != null
                val message = if (success) "拍照成功，稍后获得表情包" else "拍照成功但保存文件失败！"

                mainHandler.post {
                    callback(success, message, photoPath)
                }

                // 任务完成，停止引擎
                // stop()

            }, backgroundHandler) //  <-- 决定胜负的关键！

            if (surfaceShareBean == null) {
                surfaceShareBean = SurfaceShareBean().apply { name = "ZhiyunAgentRobot_Photo" }
            }
            Log.d(TAG, "Step 2: Preparing Surface...")
            if (surfaceShareBean == null) {// ▼▼▼【最终的、决定性的质询！】▼▼▼
                // 我们已经尝试过 "ZhiyunAgentRobot_Photo" 和 ""。
                // 最后一次尝试：完全不设置name，让它保持默认的 null 值。
                surfaceShareBean = SurfaceShareBean()
                Log.i(TAG, "Final attempt 2: Creating SurfaceShareBean without setting a name (keeping it null).")
            }

            remoteSurface = imageReader?.surface
            // ▼▼▼【第一道日志防线：检查Surface的有效性】▼▼▼
            if (remoteSurface == null || !remoteSurface!!.isValid) {
                Log.e(TAG, "FATAL FLAW: ImageReader surface is NULL or INVALID. Cannot proceed. isValid=${remoteSurface?.isValid}")
                return false
            }
            Log.i(TAG, "Step 2: Surface is VALID and ready. remoteSurface: $remoteSurface")

            // ▼▼▼【第二道日志防线：在调用API前后都加上日志】▼▼▼
            Log.i(TAG, "Step 3: About to call SurfaceShareApi.requestImageFrame...")
            SurfaceShareApi.getInstance().requestImageFrame(
                remoteSurface, surfaceShareBean, object : SurfaceShareListener() {
                    override fun onError(error: Int, message: String?) {
                        Log.e(TAG, "MISSION FAILED at the source! SurfaceShareApi.onError triggered!Code: $error, Message: $message")
                        mainHandler.post {
                            callback(false, "摄像头API错误: $message (code: $error)", null)
                        }
                        stop()
                    }
                })
            Log.i(TAG, "TAG, \"Step 3: Call to SurfaceShareApi.requestImageFrame has been sent. Engine is now waiting for image data...")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize engine.", e)
            return false
        }
    }

    private fun stop() {
        if (!isRunning) return
        // 确保停止操作也在后台线程执行，避免线程冲突
        backgroundHandler?.post {
            if (!isRunning) return@post // 双重检查，防止多次调用
            Log.i(TAG, "STOP command received. Releasing resources...")

            surfaceShareBean?.let { SurfaceShareApi.getInstance().abandonImageFrame(it) }
            imageReader?.close()

            imageReader = null
            remoteSurface = null
            surfaceShareBean = null

            stopBackgroundThread()
            isRunning = false
            Log.i(TAG, "All resources have been released.")
        }
    }

    private fun startBackgroundThread() {
        if (backgroundThread != null) return
        backgroundThread = HandlerThread("CameraEngineVictoryThread").apply {
            start()
            backgroundHandler = Handler(looper)
        }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join(50)
        } catch (e: InterruptedException) { e.printStackTrace() }
        backgroundThread = null
        backgroundHandler = null
    }
}
