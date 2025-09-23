package com.zhiyun.agentrobot

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button // 导入 Compose Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ainirobot.agent.AgentCore // 导入 AgentCore
import com.zhiyun.agentrobot.ui.screens.HomeScreen
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme // 确保您的主题名称正确

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "Activity onCreate: START") // 日志：Activity onCreate 开始

        try {
            super.onCreate(savedInstanceState)
            Log.d("MainActivity", "Activity super.onCreate() called") // 日志：super.onCreate() 调用完毕

            // 如果您需要 enableEdgeToEdge()，通常在这里调用
            // enableEdgeToEdge()
            // Log.d("MainActivity", "enableEdgeToEdge() called if uncommented")

            Log.d("MainActivity", "setContent: START") // 日志：setContent 开始
            setContent {
                // 这个 lambda 表达式是 @Composable 上下文
                Log.d(
                    "MainActivity",
                    "setContent Composable lambda: ENTER"
                ) // 日志：进入 setContent 的 Composable lambda

                ZhiyunAgentRobotTheme { // 应用您的自定义 Compose 主题
                    // Surface 通常作为根 Composable，提供背景色和平面效果
                    Surface(
                        modifier = Modifier.fillMaxSize(), // 让 Surface 填满整个屏幕
                        color = MaterialTheme.colorScheme.background // 使用主题中定义的背景色
                    ) {
                        Log.d("MainActivity", "Rendering HomeScreen") // 日志：准备渲染 HomeScreen
                        HomeScreen() // 调用您的主屏幕 Composable
                    }
                }
                Log.d(
                    "MainActivity",
                    "setContent Composable lambda: EXIT"
                ) // 日志：退出 setContent 的 Composable lambda
            }
            Log.d("MainActivity", "setContent: FINISHED") // 日志：setContent 完成

        } catch (e: Throwable) { // 捕获在 onCreate 过程中发生的任何异常或错误
            Log.e("MainActivity", "FATAL ERROR during Activity onCreate: ${e.message}", e)
            // 记录致命错误，包含错误消息和完整的堆栈跟踪 (e)
            // 在生产环境中，您可能希望在这里将错误报告给分析服务

            throw e // 重新抛出异常，让系统知道发生了严重错误，并按预期使应用崩溃
            // 这有助于在开发过程中发现问题，而不是让应用处于不一致的状态
        }

        Log.d("MainActivity", "Activity onCreate: FINISHED") // 日志：Activity onCreate 完成
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
        ZhiyunAgentRobotTheme {
            ActionTriggerScreen(modifier = Modifier.fillMaxSize())
        }
    }
}
