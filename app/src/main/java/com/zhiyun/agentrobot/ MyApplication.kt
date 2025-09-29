package com.zhiyun.agentrobot

import android.app.Application
import android.os.Bundle
import android.util.Log // 添加 Log 用于调试
import com.ainirobot.agent.AppAgent
import com.ainirobot.agent.AgentCore
import com.ainirobot.agent.action.Action
import com.ainirobot.agent.action.ActionExecutor
import com.ainirobot.agent.action.Actions
import com.ainirobot.agent.coroutine.AOCoroutineScope
import kotlinx.coroutines.launch
import com.ainirobot.agent.base.ActionResult
import com.ainirobot.agent.base.ActionStatus


class MyApplication : Application() {
    private var appAgentInstance: AppAgent? = null
    private var isAgentSDKInitialized: Boolean = false
    private var TAG  = "MyApplication"

    override fun onCreate() {
        Log.d("MyApplication", "Application onCreate: START") // 更早的日志
        super.onCreate()
        Log.d("MyApplication", "Application super.onCreate() called")
        // 不要在這裡出事哈！不要在这里初始化，appAgent !!
        Log.d(
            "MyApplication",
            "Application onCreate: FINISHED (AppAgent will be initialized later)"
        )
    }
    fun initializeAgentSDK() {
        Log.d(TAG, "MyApplication: Attempting to initialize AgentSDK...")
        if (!isAgentSDKInitialized) {
            appAgentInstance = object : AppAgent(this@MyApplication) {
                override fun onCreate() {
                    Log.i(TAG, "AppAgent: onCreate callback invoked.")
                    setPersona("你是一个乐于助人的智芸康养机器人助手。")

                    // 1. 注册系统内置的 SAY Action (推荐)
                    registerAction(Actions.SAY)
                    Log.i(TAG, "AppAgent: Actions.SAY registered.")

                    // 2. 定义并注册我们的自定义“打招呼” Action
                    val greetingAction = Action(
                        name = "com.zhiyun.agentrobot.action.GREETING", // Action 的唯一名称
                        displayName = "打个招呼",
                        desc = "当用户向我说你好、hello、嗨等问候语时执行此操作，我会回应一个友好的问候。",
                        parameters = emptyList<com.ainirobot.agent.base.Parameter>(),
                        object : ActionExecutor {
                            override fun onExecute(action: Action, params: Bundle?): Boolean {
                                Log.i(TAG, "AppAgent: Executing GREETING Action.")
                                val greetingResponse = "你好，有什么可以帮您？"

                                AOCoroutineScope.launch {
                                    try {
                                        Log.d(TAG, "AppAgent: Playing TTS: $greetingResponse")
                                        AgentCore.ttsSync(greetingResponse)
                                        Log.i(
                                            TAG,
                                            "AppAgent: TTS playback finished for GREETING Action."
                                        )
                                        action.notify() // TTS 成功播放后通知 Action 完成
                                    } catch (e: Exception) {
                                        Log.e(
                                            TAG,
                                            "AppAgent: Error during TTS playback for GREETING",
                                            e
                                        )
                                        // 即使TTS失败，也要通知Action完成，并标记为失败
                                        action.notify(result = ActionResult(ActionStatus.FAILED))
                                    }
                                }
                                return true
                            }
                        }
                    )
                    registerAction(greetingAction)
                    Log.i(TAG, "AppAgent: GREETING Action registered.")

                    //可选：添加 OnTranscribeListener 用于调试 ASR 结果
                    this.setOnTranscribeListener(object : com.ainirobot.agent.OnTranscribeListener {
                        override fun onASRResult(transcription: com.ainirobot.agent.base.Transcription): Boolean {
                            Log.d(TAG, "AppAgent ASR Result: '${transcription.text}', final: ${transcription.final}")
                            return false // 返回 false 表示不消费这个事件，让 SDK 继续处理
                        }
                        override fun onTTSResult(transcription: com.ainirobot.agent.base.Transcription): Boolean {
                            Log.d(TAG, "AppAgent TTS Result: '${transcription.text}', final: ${transcription.final}")
                            return false
                        }
                    })
                    Log.i(TAG, "AppAgent: OnTranscribeListener registered for debugging.")

                }

                override fun onExecuteAction(action: Action, params: Bundle?): Boolean {
                    Log.d(TAG, "AppAgent: onExecuteAction (for static actions) for ${action.name}")
                    return false
                }
            }
            isAgentSDKInitialized = true
            Log.i(TAG, "MyApplication: AgentSDK initialized successfully. AppAgent instance created.")
        } else {
            Log.i(TAG, "MyApplication: AgentSDK already initialized.")
        }
    }

    fun getAppAgent(): AppAgent? {
        return appAgentInstance
    }
}

