package com.zhiyun.agentrobot

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button // 导入 Compose Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ainirobot.agent.AgentCore // 导入 AgentCore
import com.zhiyun.agentrobot.ui.theme.ZhiYun_AgentRobotTheme // 确保您的主题名称正确

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 可选，用于边缘到边缘的显示效果
        setContent {
            // 确保您的主题名称 ZhiYun_AgentRobotTheme 与您项目中定义的名称一致
            ZhiYun_AgentRobotTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 调用我们新的 Composable 函数，它包含按钮
                    ActionTriggerScreen(
                        modifier = Modifier
                            .padding(innerPadding) // 使用 Scaffold 提供的内边距
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

// 创建一个新的 Composable 函数来包含我们的按钮和文本
@Composable
fun ActionTriggerScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier, // 应用从外部传入的 modifier
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "点击下方按钮打个招呼！", // 您可以自定义这个文本
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = {
            Log.d("MainActivity", "Compose Button clicked, sending query to AgentCore.")
            // 重要：确保这里的文本 ("你好，跟我打个招呼")
            // 能够被 AgentOS 的 NLU 理解并映射到您在 MyApplication 中
            // 注册的 HelloWorldAction。
            // 您可能需要根据 HelloWorldAction 的 intentFilter 或 displayName 来调整此处的文本。
            AgentCore.query("你好，跟我打个招呼")
        }) {
            Text("打个招呼") // 按钮上显示的文本
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActionTriggerScreenPreview() {
    ZhiYun_AgentRobotTheme {
        ActionTriggerScreen(modifier = Modifier.fillMaxSize())
    }
}
