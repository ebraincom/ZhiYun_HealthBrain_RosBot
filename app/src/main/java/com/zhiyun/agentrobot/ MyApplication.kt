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
import com.zhiyun.agentrobot.data.Role // <-- 1. 导入我们创建的 Role 类
import com.zhiyun.agentrobot.data.defaultRole // <-- 2. 导入我们定义的 defaultRole
import android.content.Intent // 1. 导入 Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager // 2. 导入 LocalBroadcastManager
import kotlinx.coroutines.launch

class MyApplication : Application() {
    // 【修改1】: 将 appAgentInstance 重命名为 appAgent，并设为私有 setter
    lateinit var appAgent: AppAgent
        private set
    private var appAgentInstance: AppAgent? = null
    private var isAgentSDKInitialized: Boolean = false
    private var TAG = "MyApplication"

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
            // 【修改2】: 直接为 appAgent 赋值
            appAgent = object : AppAgent(this@MyApplication) {
                override fun onCreate() {
                    Log.i(TAG, "AppAgent: onCreate callback invoked.")
                    // --- 【修改3】: 使用默认角色初始化 ---
                    setPersona(defaultRole.persona)
                    setObjective(defaultRole.objective)
                    Log.i(TAG, "AppAgent initialized with default role: ${defaultRole.name}")
                    // --- 角色初始化结束 ---


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
                            Log.d(
                                TAG,
                                "AppAgent ASR Result: '${transcription.text}', final: ${transcription.final}"
                            )
                            return false // 返回 false 表示不消费这个事件，让 SDK 继续处理
                        }

                        override fun onTTSResult(transcription: com.ainirobot.agent.base.Transcription): Boolean {
                            Log.d(
                                TAG,
                                "AppAgent TTS Result: '${transcription.text}', final: ${transcription.final}"
                            )
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
            Log.i(
                TAG,
                "MyApplication: AgentSDK initialized successfully. AppAgent instance created."
            )
        } else {
            Log.i(TAG, "MyApplication: AgentSDK already initialized.")
        }
    }
    // 【修改4】: 移除 getAppAgent() 方法，因为 appAgent 已经是公共可读的了

    // --- 【新增】: 添加一个公共方法，用于后续的角色切换 ---
    fun switchAgentRole(newRole: Role) {
        // 增加一个判断，确保 appAgent 已经初始化
        if (::appAgent.isInitialized) {
            appAgent.setPersona(newRole.persona)
            appAgent.setObjective(newRole.objective)
            // 添加日志来确认角色切换成功
            Log.i(TAG, "Agent role switched to: ${newRole.name}")
            // ---【核心修正】---
            // 重置大模型的对话上下文，让新的 Persona 和 Objective 立即生效
            AgentCore.clearContext()
            Log.i(TAG, "AgentCore context has been cleared.")
            // --- 【核心修正】: 发送一个本地广播通知前台页面角色已更新 ---
            val intent = Intent(ACTION_ROLE_SWITCHED)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            Log.i(TAG, "Broadcast sent: ACTION_ROLE_SWITCHED")
            // --- 修正结束 ---
        } else {
            Log.w(TAG, "Cannot switch role because AppAgent is not initialized yet.")
        }
    }

    companion object {
        const val ACTION_ROLE_SWITCHED = "com.zhiyun.agentrobot.ACTION_ROLE_SWITCHED"
    }
}