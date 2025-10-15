package com.zhiyun.agentrobot.util // 请确保包名与您的项目结构一致

import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Surface
import com.ainirobot.coreservice.client.surfaceshare.SurfaceShareApi
import com.ainirobot.coreservice.client.surfaceshare.SurfaceShareBean
import com.ainirobot.coreservice.client.surfaceshare.SurfaceShareError
import com.ainirobot.coreservice.client.surfaceshare.SurfaceShareListener
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 官方 SurfaceShareDataEngine.java 的终极1:1 Kotlin复刻版 (带时序修正)
 */
object CameraEngine {

    private const val TAG = "CameraEngine_Ultimate"
    private const val VISION_IMAGE_WIDTH = 640
    private const val VISION_IMAGE_HEIGHT = 480
    private const val MAX_CACHE_IMAGES = 4

    private val isRunning = AtomicBoolean(false)

    // 线程1: 专门用于接收 ImageReader 的回调
    private var imageReaderThread: HandlerThread? = null
    private var imageReaderHandler: Handler? = null

    // 线程2: 专门用于处理图像数据
    private var imageProcessThread: HandlerThread? = null
    private var imageProcessHandler: ImageProcessHandler? = null

    private var imageReader: ImageReader? = null
    // ▼▼▼【最终修正第一处：提前声明 remoteSurface】▼▼▼
    private var remoteSurface: Surface? = null
    private var surfaceShareBean: SurfaceShareBean? = null
    private var currentError = 0

    private var captureCallback: ((ByteArray) -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null

    private class ImageProcessHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0) {
                val yuvData = msg.obj as ByteArray
                Log.d(TAG, "ImageProcessHandler: Received YUV data, invoking callback.")
                captureCallback?.invoke(yuvData)
                Log.d(TAG, "ImageProcessHandler: Releasing resources after callback.")
                stopCapture()
            }
        }
    }

    fun startSingleFrameCapture(onSuccess: (yuvData: ByteArray) -> Unit, onFailure: (reason: String) -> Unit) {
        if (!isRunning.compareAndSet(false, true)) {
            Log.w(TAG, "DEFEAT! Capture is already running.")
            onFailure("上次拍照还未完成")
            return
        }

        Log.i(TAG, "ACTION: Starting capture...")
        this.captureCallback = onSuccess
        this.errorCallback = onFailure

        try {
            startThreads()
            initImageReaderAndSurface() // 关键步骤：初始化并立刻获取Surface
            requestFrame()
        } catch (e: Exception) {
            Log.e(TAG, "DEFEAT! Exception during capture start.", e)
            errorCallback?.invoke("启动拍照时发生异常: ${e.message}")
            stopCapture()
        }
    }

    // 严格按照官方顺序释放
    private fun stopCapture() {
        if (!isRunning.getAndSet(false)) { return }
        Log.w(TAG, "ACTION: Stopping capture and releasing all resources...")

        if (currentError != SurfaceShareError.ERROR_SURFACE_SHARE_USED) {
            surfaceShareBean?.let { SurfaceShareApi.getInstance().abandonImageFrame(it) }
        }
        closeImageReader() // 先关闭Reader和Surface
        stopThreads()      // 再停止线程

        surfaceShareBean = null
        currentError = 0
        captureCallback = null
        errorCallback = null
        Log.w(TAG, "All resources released.")
    }

    private fun startThreads() {
        imageReaderThread = HandlerThread("ImageReaderThread").apply { start() }
        imageReaderHandler = Handler(imageReaderThread!!.looper)
        imageProcessThread = HandlerThread("ImageProcessThread").apply { start() }
        imageProcessHandler = ImageProcessHandler(imageProcessThread!!.looper)
    }

    // ▼▼▼【最终修正第二处：合并初始化和Surface获取，确保时序正确】▼▼▼
    private fun initImageReaderAndSurface() {
        if (imageReader != null) throw IllegalStateException("ImageReader has been created.")

        imageReader = ImageReader.newInstance(
            VISION_IMAGE_WIDTH, VISION_IMAGE_HEIGHT, ImageFormat.YUV_420_888, MAX_CACHE_IMAGES
        ).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                Log.i(TAG, "SUCCESS! Image available! Processing...")

                val nv21Data = convertYuv420888ToNv21(image)
                image.close()
                Message.obtain(imageProcessHandler, 0, nv21Data).sendToTarget()

            }, imageReaderHandler)
        }
        // 关键：在请求前就获取并持有Surface的引用
        remoteSurface = imageReader?.surface ?: throw IllegalStateException("Surface created is null!")
        Log.d(TAG, "Surface initialized and obtained successfully: $remoteSurface")
    }

    private fun requestFrame() {
        if (surfaceShareBean == null) {
            surfaceShareBean = SurfaceShareBean().apply { name = "ZhiyunApp" }
        }
        // remoteSurface 此时一定已被初始化
        SurfaceShareApi.getInstance().requestImageFrame(remoteSurface!!, surfaceShareBean, object : SurfaceShareListener() {
            override fun onError(error: Int, message: String) {
                super.onError(error, message)
                currentError = error
                Log.e(TAG, "DEFEAT! SurfaceShareApi onError: $error, $message")
                errorCallback?.invoke("摄像头共享失败: $message")
                stopCapture()
            }
        })
        Log.i(TAG, "SurfaceShareApi.requestImageFrame called.")
    }

    private fun stopThreads() {
        imageReaderThread?.quitSafely()
        imageProcessThread?.quitSafely()
        try { imageReaderThread?.join(50) } catch (e: InterruptedException) {}
        try { imageProcessThread?.join(50) } catch (e: InterruptedException) {}
        imageReaderThread = null
        imageProcessThread = null
        imageReaderHandler = null
        imageProcessHandler = null
    }

    private fun closeImageReader() {
        imageReader?.close()
        imageReader = null
        // 根据官方文档，不应手动释放由 ImageReader 创建的 Surface
        // remoteSurface?.release()
        remoteSurface = null
    }

    private fun convertYuv420888ToNv21(image: Image): ByteArray {
        // (此部分代码无变化)
        val width = image.width
        val height = image.height
        val ySize = width * height
        val nv21 = ByteArray(ySize + width * height / 2)
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        yBuffer.get(nv21, 0, ySize)
        val vPlane = image.planes[2]
        val uvPixelStride = vPlane.pixelStride
        val vPlaneBuffer = vPlane.buffer
        for (i in 0 until height / 2) {
            for (j in 0 until width / 2) {
                val v = vPlaneBuffer[i * vPlane.rowStride + j * uvPixelStride]
                nv21[ySize + i * width + j * 2] = v
                nv21[ySize + i * width + j * 2 + 1] = uBuffer[i * image.planes[1].rowStride + j * uvPixelStride]
            }
        }
        return nv21
    }
}
