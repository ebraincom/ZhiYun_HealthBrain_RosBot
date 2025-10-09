package com.zhiyun.agentrobot.ui.guide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zhiyun.agentrobot.ui.common.AppScaffold
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme

class GuideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZhiyunAgentRobotTheme {
                GuidePage()
            }
        }
    }
}

@Composable
fun GuidePage() {
    // 使用我们全局的AppScaffold模板
    AppScaffold(
        title = "导览界面", // 设置标题
        showBackButton = false, // 导览页作为主导航页之一，不显示返回按钮
        actions = {
            // TODO: P1/P2阶段 - 在这里实现顶部栏右侧的真实按钮
            // P0阶段先留空
        }
    ) { paddingValues ->
        // 在主工作区插槽中，放入我们的导览页核心内容
        GuideContentScreen(
            modifier = Modifier.padding(paddingValues)
        )
    }
}
