// 文件名: ChatActivity.kt
// 本次更新：【最终加固版】在您修复核心Bug的基础上，精简Agent职责，强化退出机制。

package com.zhiyun.agentrobot // 请确保包名与您项目一致

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.ainirobot.agent.AgentCore
import com.ainirobot.agent.OnTranscribeListener
import com.ainirobot.agent.PageAgent
import com.ainirobot.agent.base.llm.LLMMessage
import com.ainirobot.agent.base.llm.LLMConfig
import com.ainirobot.agent.base.llm.Role as LLMRole // 使用别名避免与我们自己的Role类冲突
import com.ainirobot.agent.coroutine.AOCoroutineScope
import com.ainirobot.agent.base.Transcription
import com.ainirobot.agent.action.Actions
import com.zhiyun.agentrobot.data.Role // 【关键导入】我们自己的Role数据类
import kotlinx.coroutines.launch
import android.os.Build // 【新增导入】需要导入Build来判断系统版本


class ChatActivity : ComponentActivity() {
    private lateinit var roleData: Role
    private lateinit var pageAgent: PageAgent
    private val TAG = "ChatActivity_Final" // 使用新Tag以示区别
    // 【圣经化改造】: 采用“圣经”中的对话历史管理机制
    private val conversationHistory = mutableListOf<LLMMessage>()
    private val maxHistorySize = 10 // 最大保留10轮对话

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: Applying final reinforcements.")

        // 1.【接收角色】: 从Intent中获取角色数据 (与圣经一致)
        // ▼▼▼【病灶：废弃的 getParcelableExtra 调用导致运行时崩溃】▼▼▼
        // 原有病灶: roleData = intent.getParcelableExtra("role")!!
        // 修复方案: 采用官方推荐的、兼容新旧安卓版本的安全获取方式。
        try {
            roleData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 对于 Android 13 (API 33) 及以上版本，使用新的、需要Class参数的方法
                intent.getParcelableExtra("role", Role::class.java)
            } else {
                // 对于旧版本，继续使用被废弃但仍然有效的方法
                @Suppress("DEPRECATION") // 用这个注解告诉编译器我们知道这是废弃的，但必须用
                intent.getParcelableExtra("role")
            }!! // 最后的非空断言依然保留，因为我们业务上确信role必须存在

            Log.i(TAG, "Successfully and safely received role: ${roleData.name}")
        } catch (e: Exception) {
            Log.e(TAG, "FATAL: Failed to get 'role' from intent. This is likely the cause of the silence issue.", e)
            AgentCore.tts("哎呀，启动角色扮演失败了，我先退下了。")
            finish() // 如果没有角色数据，此页面无法工作，直接关闭
            return
        }

        // 2.【灵魂注入】: 设置AppAgent的人设 (与圣经一致)
        val appAgent = (applicationContext as MyApplication).appAgent
        appAgent.setPersona(roleData.persona)
        appAgent.setObjective(roleData.objective)
        Log.i(TAG, "AppAgent persona and objective set for ${roleData.name}.")

        // 3.【初始化PageAgent】 (与圣经一致)
        pageAgent = PageAgent(this)
        pageAgent.setObjective(
            "${roleData.objective}\n---重要规则---\n" +
                    "当用户明确表示要退出、返回、不聊了、结束对话时，你必须什么都不说，直接调用 EXIT 工具来退出当前页面。"
        )
        Log.i(TAG, "PageAgent initialized with its short-term objective.")

        // 4.【初始化PageAgent】: PageAgent在此只负责“感知”和“执行”，不需再设目标。
        // pageAgent = PageAgent(this)

        // 5.【注册Action】: 只注册本页面需要的Actions
        pageAgent.registerAction(Actions.SAY)
            .registerAction(Actions.EXIT) // EXIT Action会直接关闭当前Activity

        // 6.【设置监听器】
        setupListeners()
        // ▲▲▲【最终加固 1/2 完毕】▲▲▲
    }
    private fun setupListeners() {
        // 设置语音转写监听 (采纳圣经做法)
        pageAgent.setOnTranscribeListener(object : OnTranscribeListener {
            override fun onASRResult(transcription: Transcription): Boolean {
                if (transcription.text.isNotEmpty() && transcription.final) {
                    // 当听到用户完整的、非空的一句话时
                    Log.i(TAG, "User said (final): ${transcription.text}")
                    generateRoleResponse(transcription.text)
                }
                return true
            }

            override fun onTTSResult(transcription: Transcription): Boolean {
                if (transcription.text.isNotEmpty() && transcription.final) {
                    // 当机器人说完一句完整的、非空的话时
                    Log.i(TAG, "Robot said (final): ${transcription.text}")
                    // 将机器人的回复也添加到历史记录中
                    addToHistory(LLMMessage(LLMRole.ASSISTANT, transcription.text))
                }
                return true
            }
        })
        Log.i(TAG, "OnTranscribeListener has been set up.")
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: Starting new session for role: ${roleData.name}")

        // 1.【净化战场】: 清理上一个页面的所有状态 (与圣经一致)
        AgentCore.stopTTS()
        AgentCore.clearContext()
        clearHistory()
        // 2. 在对话页，我们不希望主页的那些Action（如查询天气）被触发，
        //    所以上传一个空的接口信息，覆盖掉之前的。
        AgentCore.uploadInterfaceInfo(" ")
        // ▲▲▲【最终加固 2/2 完毕】▲▲▲

        // 3.【圣经化改造】: 不再说写死的开场白，而是让LLM动态生成！
        AOCoroutineScope.launch {
            kotlinx.coroutines.delay(250) // 短暂延迟，确保Agent状态就绪
            generateInitialIntroduction()
        }

        // 4.【进入倾听模式】: 设置为被动模式 (与圣经一致)
        AgentCore.isDisablePlan = true
        Log.i(TAG, "Agent mode set to PASSIVE (isDisablePlan = true). Ready for conversation.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: Cleaning up session.")
        // 页面销毁时再次清理 (与圣经一致)
        AgentCore.stopTTS()
        AgentCore.clearContext()
        clearHistory()
    }

    /**
     * 【圣经化改造】: 采用“圣经”中更专业的生成回复逻辑
     */
    private fun generateRoleResponse(userQuery: String) {
        AOCoroutineScope.launch {
            try {
                // 1. 先将用户的当前输入加入历史，确保上下文完整 (圣经做法)
                addToHistory(LLMMessage(LLMRole.USER, userQuery))

                val messages = mutableListOf<LLMMessage>()

                // 2. 添加“圣经版”系统提示词
                messages.add(
                    LLMMessage(
                        LLMRole.SYSTEM,
                        """你现在扮演的角色是：${roleData.name}
                        |角色设定：${roleData.persona}
                        |行为准则：${roleData.objective}
                        |---
                        |要求：
                        |1. 完全沉浸在角色中，展现角色特色
                        |2. 回复要自然流畅，富有情感
                        |3. 每次回复不超过50字
                        |4. 不要暴露是AI的身份
                        |5. 要有自己的态度和个性
                        |6. 保持对话的连贯性和上下文
                        |7. 说话要符合角色的语言风格和时代背景
                        |8. 根据之前的对话历史，保持角色的一致性和连贯性""".trimMargin()
                    )
                )

                // 3. 添加全部历史对话记录 (现在已包含用户的最新提问)
                messages.addAll(conversationHistory)

                // 4. 配置LLM参数
                val config = LLMConfig(temperature = 0.8f, maxTokens = 150)

                // 5. 发送给LLM并流式播放TTS
                AgentCore.llmSync(messages, config, timeoutMillis = 20 * 1000, isStreaming = true)

                Log.i(TAG, "LLM request sent with Bible architecture for query: '$userQuery'")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate LLM response with Bible architecture", e)
            }
        }
    }

    private fun generateInitialIntroduction() {
        AOCoroutineScope.launch {
            try {
                val introQuery = "你需要用你的角色身份，对我做一个简短的、不超过30字的自我介绍，要自然亲切。"
                val messages = mutableListOf<LLMMessage>()
                messages.add(
                    LLMMessage(
                        LLMRole.SYSTEM,
                        """你现在扮演的角色是：${roleData.name}
                        |角色设定：${roleData.persona}
                        |行为准则：${roleData.objective}
                        |---
                        |现在请严格按照你的角色，生成一句符合角色个性的、简短的自我介绍。""".trimMargin()
                    )
                )
                messages.add(LLMMessage(LLMRole.USER, introQuery))

                val config = LLMConfig(temperature = 0.8f, maxTokens = 80)

                // 【注意】开场白不需要加入历史记录，因为它不是一个真实对话

                // 生成并流式播放开场白
                AgentCore.llmSync(messages, config, 20 * 1000, isStreaming = true)
                Log.i(TAG, "Dynamic initial introduction request sent to LLM.")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate initial introduction", e)
                // 如果动态生成失败，说一句备用语
                AgentCore.tts("您好，我是${roleData.name}，我们开始聊天吧。")
            }
        }
    }

    private fun addToHistory(message: LLMMessage) {
        conversationHistory.add(message)
        // 如果历史记录过长，移除最早的一轮对话（一问一答）
        while (conversationHistory.size > maxHistorySize * 2) {
            // 确保移除的是一对 USER-ASSISTANT
            if (conversationHistory.firstOrNull()?.role == LLMRole.USER && conversationHistory.getOrNull(1)?.role == LLMRole.ASSISTANT) {
                conversationHistory.removeAt(0)
                conversationHistory.removeAt(0)
            } else {
                // 如果队首不是 USER，只移除一个以避免死循环
                conversationHistory.removeAt(0)
            }
        }
        Log.d(TAG, "History updated. Current size: ${conversationHistory.size}")
    }

    private fun clearHistory() {
        conversationHistory.clear()
        Log.i(TAG, "Conversation history cleared.")
    }
}
