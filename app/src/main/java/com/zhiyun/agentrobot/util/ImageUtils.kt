package com.zhiyun.agentrobot.util

// ▼▼▼【核心修正：确保每一个import语句都独立成行且正确无误】▼▼▼
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import java.io.ByteArrayOutputStream

/**
 * 图像处理工具类
 */
object ImageUtils {
    /**
     * 将YUV_420_888格式的Image对象转换为Bitmap.
     */
    fun yuvImageToBitmap(image: Image): Bitmap {
        if (image.format != ImageFormat.YUV_420_888) {
            throw IllegalArgumentException("Input image format must be YUV_420_888")
        }

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Y plane
        yBuffer.get(nv21, 0, ySize)
        // U and V planes - 对于NV21格式, V在前, U在后, 且是交错存储
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()

        Log.i("ImageUtils", "Successfully converted YUV Image to Bitmap.")
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}

