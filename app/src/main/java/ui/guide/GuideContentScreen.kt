package com.zhiyun.agentrobot.ui.guide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiyun.agentrobot.R // 确保您的R文件路径正确
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme // 确保您的主题路径正确

@Composable
fun GuideContentScreen(modifier: Modifier = Modifier) {
    // 使用一个Box来应用全局内边距
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 32.dp) // 根据设计图调整合适的全局内边距
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 左侧轮播图占位区 (权重7)
            Box(
                modifier = Modifier
                    .weight(7f)
                    .fillMaxHeight()
                    .background(Color(0xFF3D3D3D), RoundedCornerShape(16.dp)), // 使用一个深灰色作为占位背景
                contentAlignment = Alignment.Center
            ) {
                // TODO: P1阶段 - 替换为您的占位图
                // 您可以将图片命名为 "placeholder_carousel.png" 并放入 drawable 文件夹
                // Image(painter = painterResource(id = R.drawable.placeholder_carousel), contentDescription = "轮播图占位")
                Text("轮播图占位区", color = Color.White, fontSize = 32.sp)
            }

            // 2. 左右间距
            Spacer(modifier = Modifier.width(40.dp))

            // 3. 右侧按钮区 (权重3)
            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween // 自动均分垂直间距
            ) {
                // 三个可复用的按钮
                // TODO: 请将 R.drawable.icon_company 等替换为您真实的图标资源文件名
                GuideButton(text = "公司概览", iconResId = R.drawable.ic_launcher_foreground) // 暂时用默认图标占位
                GuideButton(text = "智芸数据", iconResId = R.drawable.ic_launcher_foreground)
                GuideButton(text = "智芸数字人", iconResId = R.drawable.ic_launcher_foreground)
            }
        }
    }
}

// 抽离出的可复用按钮组件
@Composable
private fun GuideButton(text: String, iconResId: Int) {
    // 按钮背景的线性渐变，颜色值来自您的设计图
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF779DFF), Color(0xFFCCDCFF))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // 遵循设计图高度
            .clip(RoundedCornerShape(20.dp)) // 设置圆角
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = text,
                modifier = Modifier.size(94.dp), // 遵循设计图图标尺寸
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 24.sp, // 字体大小可微调
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 预览函数，方便您在Android Studio中直接查看UI效果，无需运行到真机
@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun GuideContentScreenPreview() {
    ZhiyunAgentRobotTheme {
        // 为了在预览中看到正确的背景色，我们把它放在一个Box里
        Box(modifier = Modifier.background(Color(0xFFF0F4FF))) {
            GuideContentScreen()
        }
    }
}



