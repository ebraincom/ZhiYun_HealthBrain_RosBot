// =================================================================================
// 文件路径: app/src/main/java/com/zhiyun/agentrobot/util/ImageUtils.kt
// 【最终正确版】 - 新增从byte[]保存的方法
// =================================================================================
package com.zhiyun.agentrobot.util

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

    private const val TAG = "ImageUtils_FINAL"

    /**
     * 【核心工具】将 YUV_420_888 的 Image 对象转换为 NV21 格式的 byte 数组。
     * 这是最关键的一步，用于在回调中“榨取”数据。
     * @param image 必须是 YUV_420_888 格式。
     * @return 转换后的 NV21 byte 数组。
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

        var rowStride = image.planes[0].rowStride
        if (rowStride == width) {
            yBuffer.get(nv21, 0, ySize)
        } else {
            val yBufferPos = yBuffer.position()
            for (row in 0 until height) {
                yBuffer.position(yBufferPos + row * rowStride)
                yBuffer.get(nv21, row * width, width)
            }
        }

        rowStride = image.planes[2].rowStride
        val pixelStride = image.planes[2].pixelStride

        val uBufferPos = uBuffer.position()
        val vBufferPos = vBuffer.position()

        if (pixelStride == 2 && rowStride == width) {
            // V-U 交错存储，符合 NV21
            vBuffer.get(nv21, ySize, uvSize * 2)
        } else {
            // 需要手动交错合并 V 和 U
            for (row in 0 until height / 2) {
                for (col in 0 until width / 2) {
                    val vu_index = ySize + row * width + col * 2
                    val u_index = uBufferPos + row * rowStride + col * pixelStride
                    val v_index = vBufferPos + row * rowStride + col * pixelStride

                    nv21[vu_index] = vBuffer.get(v_index)
                    nv21[vu_index + 1] = uBuffer.get(u_index)
                }
            }
        }
        return nv21
    }

    /**
     * 【核心保存方法】将 NV21 格式的 byte 数组保存为 JPEG 文件。
     * @param nv21Data NV21 格式的图像数据。
     * @param width 图像宽度。
     * @param height 图像高度。
     * @param storageDir 保存目录。
     * @return 成功则返回文件路径，否则返回 null。
     */
    fun saveNv21DataToJpeg(nv21Data: ByteArray, width: Int, height: Int, storageDir: File?): String? {
        if (storageDir == null) {
            Log.e(TAG, "Storage directory is null.")
            return null
        }
        return try {
            val yuvImage = YuvImage(nv21Data, ImageFormat.NV21, width, height, null)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val photoFile = File(storageDir, "IMG_$timeStamp.jpg")

            FileOutputStream(photoFile).use { out ->
                yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
            }
            Log.i(TAG, "Image successfully saved from byte array to: ${photoFile.absolutePath}")
            photoFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save NV21 data to JPEG.", e)
            null
        }
    }
}
