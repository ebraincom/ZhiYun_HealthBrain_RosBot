// 文件路径: app/src/main/java/com/zhiyun/agentrobot/ui/guide/AiPhotoVictoryScreen.kt

package com.zhiyun.agentrobot.ui.guide

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ✅✅✅【V23.0 独立胜利模块】✅✅✅
 * 这是一个独立的、可复用的Composable函数，专门用于展示AI写真生成的胜利界面。
 * @param finalEmoticon 最终生成的AI写真图
 * @param qrCode 最终生成的二维码图
 */
@Composable
fun AiPhotoVictoryScreen(
    finalEmoticon: Bitmap, // 注意：这里我们假定调用它时，图片一定存在
    qrCode: Bitmap?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 背景层：使用AI写真作为模糊背景
        Image(
            bitmap = finalEmoticon.asImageBitmap(),
            contentDescription = "AI Emoticon Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // 裁切以铺满
        )
        // 高斯模糊遮罩层 (可选，但效果很好)
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

        // 内容层：居中显示写真和二维码
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. AI写真主体
            Image(
                bitmap = finalEmoticon.asImageBitmap(),
                contentDescription = "AI Emoticon Main",
                modifier = Modifier
                    .height(600.dp) // 给一个主体高度
                    .aspectRatio(1f) // 保持1:1比例
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. 提示文字
            Text(
                text = "您的专属AI写真已生成！",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 二维码
            if (qrCode != null) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp) // 给白底留边
                ) {
                    Image(
                        bitmap = qrCode.asImageBitmap(),
                        contentDescription = "Scan to download",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


