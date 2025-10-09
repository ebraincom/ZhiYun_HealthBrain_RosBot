package com.zhiyun.agentrobot.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    // 1. 顶部栏标题
    title: String,
    // 2. 是否显示返回按钮
    showBackButton: Boolean = false,
    // 3. 右侧的操作按钮，可以传递0个、1个或多个
    actions: @Composable RowScope.() -> Unit = {},
    // 4. 中间主工作区的内容插槽
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        // 全局统一的背景色
        containerColor = Color(0xFFF0F4FF), // 一个更柔和的淡蓝色背景
        topBar = {
            // 将顶部栏也抽离成一个可复用的组件
            AppTopBar(
                title = title,
                showBackButton = showBackButton,
                actions = actions
            )
        },
        bottomBar = {
            // 全局统一的底部栏
            AppBottomBar()
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

// 顶部栏组件
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    title: String,
    showBackButton: Boolean,
    actions: @Composable RowScope.() -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black) },
        navigationIcon = {
            if (showBackButton) {
                // TODO: P1阶段 - 在这里实现返回按钮的UI和返回逻辑
                TextButton(onClick = { /* TODO: onBackClicked */ }) {
                    Text(
                        text = "‹ 返回上页",
                        fontSize = 18.sp,
                        color = Color(0xFF5A5A5A)
                    )
                }
            } else {
                // 如果不需要返回按钮，可以留空或者放一个占位符
                Spacer(modifier = Modifier.width(80.dp)) // 增加一个占位符以保持平衡
            }
        },
        actions = actions, // 直接使用传递进来的右侧按钮
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent // 背景透明，使用Scaffold的背景色
        )
    )
}

// 底部栏组件
@Composable
private fun AppBottomBar() {
    // TODO: P2阶段 - 在这里实现您全局统一的底部栏UI（机器人、输入框、时间等）
    // P0阶段我们先用一个有高度的简单占位符
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // 模拟底部栏的高度
            .background(Color.Transparent)
    ) {
        // 可以在这里放一个简单的文本示意
        // Text("全局底部栏占位", modifier = Modifier.align(Alignment.Center))
    }
}


@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
fun AppScaffoldPreview() {
    ZhiyunAgentRobotTheme {
        AppScaffold(
            title = "预览标题",
            showBackButton = true,
            actions = {
                Button(onClick = {}) { Text("按钮1") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {}) { Text("按钮2") }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                Text(text = "这里是主工作区的内容")
            }
        }
    }
}
