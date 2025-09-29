package com.zhiyun.agentrobot

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ainirobot.agent.AgentCore
import com.ainirobot.agent.OnTranscribeListener
import com.ainirobot.agent.PageAgent
import com.ainirobot.agent.TTSCallback // 确保这个导入是正确的
import com.ainirobot.agent.base.Transcription

class VoiceEchoTestActivity : ComponentActivity() {

    private lateinit var pageAgent: PageAgent
    private val TAG = "VoiceEchoTest"
    // textToEcho 需要在 onTaskEnd 中也能访问，或者通过参数传递
    // 为了简单，我们可以在 onASRResult 中声明它，并在 onTaskEnd 的 Log 中直接引用（如果 Log 目的明确）
    // 或者，如果 onTaskEnd 的逻辑需要 textToEcho，应考虑如何正确传递

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pageAgent = PageAgent(this)
        pageAgent.setObjective("语音回显功能测试页面")

        setupTranscribeListener()

        setContent {
            VoiceEchoTestScreen()
        }

        Log.i(TAG, "VoiceEchoTestActivity Created and PageAgent initialized.")
        // 主动TTS可以保留或注释，根据测试需要
        AgentCore.tts("语音回显测试已准备就绪，请尝试对我说话。")
    }

    private fun setupTranscribeListener() {
        pageAgent.setOnTranscribeListener(object : OnTranscribeListener {
            override fun onASRResult(transcription: Transcription): Boolean {
                if (transcription.final && transcription.text.isNotEmpty()) {
                    val userSaid = transcription.text
                    Log.d(TAG, "ASR Final Result: '$userSaid'")

                    val textToEcho = "你刚才说的是：$userSaid" // textToEcho 在这里声明
                    Log.d(TAG, "TTS Prepared: '$textToEcho'")

                    AgentCore.tts(
                        text = textToEcho,
                        timeoutMillis = 180000L,
                        callback = object : TTSCallback {
                            override fun onTaskEnd(status: Int, result: String?) {
                                // 在 Log 中引用外层作用域的 textToEcho 是可以的
                                Log.i(TAG, "TTS Task Ended - Status: $status, Result: $result, Original Text for Echo: '$textToEcho'")

                                when (status) {
                                    1 -> { // 文档确认: status = 1 表示成功
                                        Log.i(TAG, "TTS Task SUCCESS (Status: 1)")
                                    }
                                    2 -> { // 文档确认: status = 2 表示失败
                                        Log.e(TAG, "TTS Task FAILURE (Status: 2), ErrorDetails: $result")
                                    }
                                    else -> {
                                        Log.w(TAG, "TTS Task Ended with UNKNOWN status: $status, Result: $result")
                                    }
                                }
                            }
                        }
                    )
                } else if (transcription.text.isNotEmpty()) {
                    Log.d(TAG, "ASR Intermediate Result: '${transcription.text}'")
                }
                return true
            }

            override fun onTTSResult(transcription: Transcription): Boolean {
                if (transcription.final && transcription.text.isNotEmpty()) {
                    Log.d(TAG, "TTS Engine Playback Final: '${transcription.text}' (from onTTSResult)")
                } else if (transcription.text.isNotEmpty()) {
                    Log.d(TAG, "TTS Engine Playback Intermediate: '${transcription.text}' (from onTTSResult)")
                }
                return true
            }
        })
        Log.i(TAG, "OnTranscribeListener has been set on PageAgent.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: VoiceBar is enabled: ${AgentCore.isEnableVoiceBar}")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }
}

@Composable
fun VoiceEchoTestScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("请对我说话进行回显测试...")
    }
}

