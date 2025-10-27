// =================================================================================
// 文件路径: app/src/main/java/com/zhiyun/agentrobot/util/ImageUtils.kt
// 【最终决战兵器】 - 包含了所有必须方法的最终正确版本
// =================================================================================
package com.zhiyun.agentrobot.util // 包名声明，与您的文件路径完全一致

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {

    private const val tag = "ImageUtilsFinal" // 遵循Kotlin命名规范

    /**
     * 【核心工具1】将 YUV_420_888 的 Image 对象转换为 NV21 格式的 byte 数组。
     */
    fun convertYUV420888ToNV21(image: Image): ByteArray {
        val width = image.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 4

        val nv21 = ByteArray(ySize + uvSize * 2)

        val yBuffer: ByteBuffer = image.planes[0].buffer // Y
        val uBuffer: ByteBuffer = image.planes[1].buffer // U
        val vBuffer: ByteBuffer = image.planes[2].buffer // V

        val yRowStride = image.planes[0].rowStride
        if (yRowStride == width) {
            yBuffer.get(nv21, 0, ySize)
        } else {
            val yBufferPos = yBuffer.position()
            for (row in 0 until height) {
                yBuffer.position(yBufferPos + row * yRowStride)
                yBuffer.get(nv21, row * width, width)
            }
        }

        val vRowStride = image.planes[2].rowStride
        val vPixelStride = image.planes[2].pixelStride
        val uRowStride = image.planes[1].rowStride
        val uPixelStride = image.planes[1].pixelStride

        // 将U和V交错填充到NV21的UV区域
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuIndex = ySize + row * width + col * 2
                val vIndex = row * vRowStride + col * vPixelStride
                val uIndex = row * uRowStride + col * uPixelStride
                nv21[vuIndex] = vBuffer.get(vIndex)
                nv21[vuIndex + 1] = uBuffer.get(uIndex)
            }
        }
        return nv21
    }

    /**
     * 【核心工具2】将 NV21 格式的 byte 数组保存为 JPEG 文件。
     * 这个方法的存在，是解决您最新报错的关键！
     */
    fun saveNv21DataToJpeg(nv21Data: ByteArray, width: Int, height: Int, storageDir: File?): String? {
        if (storageDir == null) {
            Log.e(tag, "Storage directory is null.")
            return null
        }
        return try {
            val yuvImage = YuvImage(nv21Data, ImageFormat.NV21, width, height, null)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val photoFile = File(storageDir, "IMG_$timeStamp.jpg")

            FileOutputStream(photoFile).use { out ->
                yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
            }
            Log.i(tag, "Image successfully saved from byte array to: ${photoFile.absolutePath}")
            photoFile.absolutePath
        } catch (e: Exception) {
            Log.e(tag, "Failed to save NV21 data to JPEG.", e)
            null
        }
    }
}
