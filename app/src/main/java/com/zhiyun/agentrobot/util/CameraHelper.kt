package com.zhiyun.agentrobot.util

import android.graphics.Bitmap
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.ainirobot.coreservice.client.surfaceshare.SurfaceShareApi
import com.ainirobot.coreservice.client.surfaceshare.SurfaceShareBean
import com.ainirobot.coreservice.client.surfaceshare.SurfaceShareListener

/**
 * 【战略重构】一个单例的摄像头管理者，完全模仿官方SurfaceShareDataEngine.java的设计.
 * 它负责长期管理与VisionSDK的连接. 文件名保持为CameraHelper.kt，但内部实现为单例对象.
 */
object CameraHelper { // <--- 核心改变：从 class 变成了 object (单例)

    private const val TAG = "CameraHelper"
    private const val VISION_IMAGE_WIDTH = 640
    private const val VISION_IMAGE_HEIGHT = 480
    private const val MAX_CACHE_IMAGES = 4

    private var imageReader: ImageReader? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    @Volatile
    private var isCapturing = false
    private var singleFrameCallback: ((Result<Bitmap>) -> Unit)? = null

    private val surfaceShareListener = object : SurfaceShareListener() {
        override fun onError(error: Int, message: String) {
            super.onError(error, message)
            Log.e(TAG, "Request image frame failed! Error: $error, Message: $message")
            stopCaptureWithError(Result.failure(Exception("SDK错误: $message (Code: $error)")))
        }
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        Log.d(TAG, "onImageAvailable triggered!")
        val image = reader.acquireLatestImage()
        if (image != null) {
            Log.i(TAG, "SUCCESS: Image frame is available!")
            val bitmap = ImageUtils.yuvImageToBitmap(image)
            image.close()

            singleFrameCallback?.invoke(Result.success(bitmap))
            // 获取单帧后，我们不再主动销毁资源，让管理者保持待命状态
            isCapturing = false
            singleFrameCallback = null
        }
    }

    /**
     * 捕获单帧画面.
     */
    fun captureSingleFrame(callback: (Result<Bitmap>) -> Unit) {
        if (isCapturing) {
            Log.w(TAG, "Capture is already in progress.")
            callback.invoke(Result.failure(Exception("上一次拍照还未完成")))
            return
        }

        Log.i(TAG, "Starting to capture a single frame...")
        isCapturing = true
        this.singleFrameCallback = callback

        if (backgroundThread == null || !backgroundThread!!.isAlive) {
            startBackgroundThread()
        }

        backgroundHandler?.post {
            try {
                if (imageReader == null) {
                    Log.i(TAG, "Creating new ImageReader instance.")
                    imageReader = ImageReader.newInstance(
                        VISION_IMAGE_WIDTH,
                        VISION_IMAGE_HEIGHT,
                        android.graphics.ImageFormat.YUV_420_888,
                        MAX_CACHE_IMAGES
                    ).apply {
                        setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
                    }
                }

                val surface = imageReader!!.surface
                if (surface != null && surface.isValid) {
                    val surfaceShareBean = SurfaceShareBean().apply {
                        name = "ZhiyunCapture_${System.currentTimeMillis()}"
                    }
                    Log.i(TAG, "Requesting image frame...")
                    SurfaceShareApi.getInstance().requestImageFrame(surface, surfaceShareBean, surfaceShareListener)
                } else {
                    throw IllegalStateException("Surface is null or invalid.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "FATAL: Exception during capture setup. ${e.message}")
                stopCaptureWithError(Result.failure(e))
            }
        }
    }

    private fun startBackgroundThread() {
        Log.i(TAG, "Starting background thread...")
        backgroundThread = HandlerThread("CameraHelperThread").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopCaptureWithError(errorResult: Result<Bitmap>) {
        singleFrameCallback?.invoke(errorResult)
        isCapturing = false
        singleFrameCallback = null
    }

    fun release() {
        Log.w(TAG, "Releasing all CameraHelper resources.")
        backgroundHandler?.post {
            imageReader?.close()
            imageReader = null
            backgroundThread?.quitSafely()
            backgroundThread = null
            backgroundHandler = null
        }
    }
}
