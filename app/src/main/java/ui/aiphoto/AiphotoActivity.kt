package com.zhiyun.agentrobot.ui.aiphoto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// ‼️‼️‼️【V2.0 终极修正】: 导入您项目中真实存在的主题！‼️‼️‼️
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import com.zhiyun.agentrobot.viewmodel.FaceExpressionViewModel

/**
 * AI写真功能的主Activity，负责承载Compose UI和创建ViewModel.
 * 由总司令阁下亲自命名并下令创建！
 */
class AiphotoActivity : ComponentActivity() {

    private val viewModel: FaceExpressionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // ‼️‼️‼️【V2.0 终极修正】: 使用您项目中真实存在的主题！‼️‼️‼️
            ZhiyunAgentRobotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 【关键改造 #3】 传入 onBackClick 事件 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
                    AiphotoScreen(
                        viewModel = viewModel,
                        onBackClick = { this.finish() } // 点击时结束当前Activity
                    )
                    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 【关键改造 #3】 传入 onBackClick 事件 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
                }
            }
        }
    }
}

