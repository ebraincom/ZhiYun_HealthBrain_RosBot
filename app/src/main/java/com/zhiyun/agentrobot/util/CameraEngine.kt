// 文件路径: app/src/main/java/com/zhiyun/agentrobot/util/CameraEngine.kt
package com.zhiyun.agentrobot.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.core.app.ActivityCompat
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.Comparator

/**
 * 【v17.0·雄狮觉醒版】 - 帝国的战略升级
 * 强制开启后置摄像头（推测为13M高清主摄），夺取旗舰画质！
 */
object CameraEngine {

    private const val TAG = "CameraEngine_v17.0_Lion"
    // ... 其他成员变量保持不变 ...
    private lateinit var cameraManager: CameraManager
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private val cameraOpenCloseLock = Semaphore(1)
    private val isCapturing = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var resultCallback: ((success: Boolean, message: String, bitmap: Bitmap?) -> Unit)? = null

    @Synchronized
    fun takePicture(activity: Activity, callback: (success: Boolean, message: String, bitmap: Bitmap?) -> Unit) {
        if (isCapturing.getAndSet(true)) {
            Log.w(TAG, "拍照任务正在进行中，请勿重复调用。")
            callback(false, "任务进行中", null)
            return
        }
        this.resultCallback = callback
        this.cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        startBackgroundThread()
        backgroundHandler?.post {
            openCamera(activity)
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            handleFinish(false, "没有相机权限", null); return
        }
        try {
            // 【【【 雄狮觉醒！调用我们新的目标摄像头查找方法！ 】】】
            val cameraId = getTargetCameraId() ?: run { handleFinish(false, "未找到目标摄像头(后置高清)", null); return }
            Log.i(TAG, "目标摄像头锁定: $cameraId")

            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: run { handleFinish(false, "无法获取摄像头参数", null); return }

            val jpegSizes = map.getOutputSizes(ImageFormat.JPEG)
            if (jpegSizes.isNullOrEmpty()) {
                handleFinish(false, "不支持JPEG输出", null); return
            }

            val bestSize = chooseOptimalSize(jpegSizes)
            Log.i(TAG, "旗舰画质已锁定，尺寸: ${bestSize.width}x${bestSize.height}")

            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("等待相机锁超时")
            }
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraOpenCloseLock.release()
                    cameraDevice = camera
                    createCaptureSession(bestSize)
                }
                override fun onDisconnected(camera: CameraDevice) { cameraOpenCloseLock.release(); handleFinish(false, "摄像头断开", null) }
                override fun onError(camera: CameraDevice, error: Int) { cameraOpenCloseLock.release(); handleFinish(false, "摄像头错误 $error", null) }
            }, backgroundHandler)
        } catch (e: Exception) {
            handleFinish(false, "打开摄像头异常: ${e.message}", null)
        }
    }

    private fun chooseOptimalSize(choices: Array<Size>): Size {
        return choices.maxWithOrNull(compareBy { it.width.toLong() * it.height }) ?: Size(640, 480)
    }

    private fun createCaptureSession(size: Size) {
        try {
            imageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 1).apply {
                setOnImageAvailableListener({ reader ->
                    val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                    val bytes = image.planes[0].buffer.let { buffer -> ByteArray(buffer.remaining()).also { buffer.get(it) } }
                    image.close()

                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val matrix = Matrix()

                    // 【【【 继承胜利果实：终极校准指令！ 】】】
                    matrix.postRotate(180f)
                    matrix.postScale(1f, -1f, bitmap.width / 2f, bitmap.height / 2f)

                    val finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    bitmap.recycle()
                    handleFinish(true, "拍照成功！帝国万岁！", finalBitmap)
                }, backgroundHandler)
            }
            cameraDevice?.createCaptureSession(listOf(imageReader!!.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) { captureSession = session; triggerCapture() }
                override fun onConfigureFailed(session: CameraCaptureSession) { handleFinish(false, "配置会话失败", null) }
            }, null)
        } catch (e: CameraAccessException) {
            handleFinish(false, "创建会话失败: ${e.message}", null)
        }
    }

    // ▼▼▼【【【 核心改造：从“寻找前置”变为“寻找后置”！ 】】】▼▼▼
    private fun getTargetCameraId(): String? {
        Log.i(TAG, "开始搜寻目标摄像头 (LENS_FACING_BACK)...")
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            Log.d(TAG, "检测到摄像头 ID: $cameraId, 朝向: $facing")
            if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                Log.i(TAG, "已找到后置摄像头: $cameraId，锁定为目标！")
                return cameraId
            }
        }
        Log.e(TAG, "搜寻失败！未找到任何标记为 LENS_FACING_BACK 的摄像头！")
        return null
    }
    // ▲▲▲【【【 核心改造完成！ 】】】▲▲▲

    // ... 其他所有方法 (triggerCapture, handleFinish, 等) 保持不变 ...
    private fun triggerCapture() { try { val builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE); builder.addTarget(imageReader!!.surface); captureSession?.capture(builder.build(), null, backgroundHandler) } catch (e: CameraAccessException) { handleFinish(false, "拍照请求失败: ${e.message}", null) } }
    @Synchronized fun shutdown() { closeCamera() }
    private fun handleFinish(success: Boolean, message: String, bitmap: Bitmap?) { mainHandler.post { resultCallback?.invoke(success, message, bitmap) }; closeCamera(); isCapturing.set(false) }
    private fun closeCamera() { try { cameraOpenCloseLock.acquire(); captureSession?.close(); cameraDevice?.close(); imageReader?.close() } catch (e: InterruptedException) { Thread.currentThread().interrupt() } finally { captureSession = null; cameraDevice = null; imageReader = null; cameraOpenCloseLock.release(); stopBackgroundThread() } }
    private fun startBackgroundThread() { if (backgroundThread?.isAlive == true) return; backgroundThread = HandlerThread("CameraBackground").also { it.start() }; backgroundHandler = Handler(backgroundThread!!.looper) }
    private fun stopBackgroundThread() { backgroundThread?.quitSafely(); try { backgroundThread?.join(50) } catch (e: InterruptedException) { Thread.currentThread().interrupt() } finally { backgroundThread = null; backgroundHandler = null } }
}

