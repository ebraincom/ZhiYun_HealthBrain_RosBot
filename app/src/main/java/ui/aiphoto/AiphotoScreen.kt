package com.zhiyun.agentrobot.ui.aiphoto

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhiyun.agentrobot.viewmodel.FaceExpressionViewModel

// ... AiphotoScreen, InitialAndLoadingState, ResultState 的完整代码保持不变 ...

/**
 * AI写真功能的Compose主屏幕，负责展示UI和响应用户交互.
 */
@Composable
fun AiphotoScreen(viewModel: FaceExpressionViewModel,onBackClick: () -> Unit) {
    val statusText by viewModel.statusText.collectAsStateWithLifecycle()
    val finalEmoticon by viewModel.finalEmoticon.collectAsStateWithLifecycle()
    val qrCode by viewModel.qrCode.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (finalEmoticon == null) {
            InitialAndLoadingState(
                statusText = statusText,
                onStartClick = { viewModel.startFaceCaptureProcess() },
                onBackClick = onBackClick // 传递返回事件
            )
        } else {
            ResultState(
                emoticonBitmap = finalEmoticon!!,
                qrCodeBitmap = qrCode,
                onResetClick = { viewModel.resetState() },
                onBackClick = onBackClick // 传递返回事件
            )
        }
    }
}

/**
 * 初始与加载状态的UI
 */
@Composable
private fun InitialAndLoadingState(
    statusText: String,
    onStartClick: () -> Unit,
    onBackClick: () -> Unit // 接收返回事件
) {
    // 【根布局】: 使用Box布局，因为它允许子组件自由地对齐到各个角落
    Box(modifier = Modifier.fillMaxSize()) {

        // 【核心内容】：保持原有的居中布局，用于显示状态文本和开始按钮
        Column(
            modifier = Modifier.align(Alignment.Center), // 将Column整体置于Box的中央
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = statusText,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp) // 增加水平内边距防止文字过长时贴边
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (statusText.contains("待机中")) {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp) // 增大按钮点击区域
                ) {
                    Text(text = "表情包合影", fontSize = 24.sp)
                }
            }
        }

        // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 【关键改造】 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
        // 【返回按钮】：独立于核心内容之外，对齐到左上角
        Button(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart) // 精准对齐到父布局（Box）的左上角
                .padding(16.dp) // 留出安全边距
        ) {
            Text(text = "返回", fontSize = 20.sp)
        }
        // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 【关键改造】 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    }
}

/**
 * 结果展示状态的UI
 */
/**
 * 结果展示状态的UI (V3.0 黄金布局版)
 */
@Composable
private fun ResultState(
    emoticonBitmap: Bitmap,
    qrCodeBitmap: Bitmap?,
    onResetClick: () -> Unit,
    onBackClick: () -> Unit // 添加 onBackClick
) {
    // 【根布局】: 使用一个垂直的Column将屏幕分为上下两部分
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 【上部分】: 使用一个水平的Row，权重为1，占据大部分空间，用于放图片和二维码
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 【左侧：AI写真】
            Image(
                bitmap = emoticonBitmap.asImageBitmap(),
                contentDescription = "AI Emoticon",
                modifier = Modifier
                    .fillMaxHeight(0.9f) // 高度占满上部分空间的90%
                    .aspectRatio(emoticonBitmap.width.toFloat() / emoticonBitmap.height.toFloat()) // 保持原始宽高比
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(32.dp))

            // 【右侧：二维码和说明】
            if (qrCodeBitmap != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight() // 占满父布局（Row）的高度
                ) {
                    Text("AI绘图成功！", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("请扫码保存您的专属写真", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Image(
                        bitmap = qrCodeBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(250.dp) // ‼️‼️ 搞的大大的！尺寸增加到 250.dp ‼️‼️
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 【下部分】: 底部按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ‼️‼️ 全新的“返回”按钮 ‼️‼️
            Button(onClick = onBackClick) {
                Text(text = "返回首页", fontSize = 24.sp, modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp))
            }

            Spacer(modifier = Modifier.width(64.dp))

            // “再玩一次”按钮放在这里
            Button(onClick = onResetClick) {
                Text(text = "再玩一次", fontSize = 24.sp, modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp))
            }
        }
    }
}


