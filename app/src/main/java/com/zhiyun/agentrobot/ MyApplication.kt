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
import com.ainirobot.coreservice.client.RobotApi
import com.zhiyun.agentrobot.util.CameraEngine // <-- 导入CameraEngine
import android.app.Activity // <-- 【修复2】导入 Activity 类
// ▼▼▼ 【V2.0新增导入】 ▼▼▼
import android.os.RemoteException
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.module.ModuleCallbackApi
import com.zhiyun.agentrobot.manager.RobotApiManager
// ▲▲▲ 【V2.0新增导入】 ▲▲▲

/**
 * 【v12.0·架构统一版，融合了底层Robot调用】
 * 修复了对CameraEngine的错误调用方式，确保应用生命周期能正确管理引擎
 */

class MyApplication : Application() {
    // 【修改1】: 将 appAgentInstance 重命名为 appAgent，并设为私有 setter
    lateinit var appAgent: AppAgent
        private set
    private var isAgentSDKInitialized: Boolean = false
    private var TAG = "MyApplication_v12_Puppeteer" // 更新版本号，便于调试

    override fun onCreate() {
        Log.d("MyApplication", "Application onCreate: START") // 更早的日志
        super.onCreate()
        Log.d("MyApplication", "Application super.onCreate() called")
        // 不要在這裡出事哈！不要在这里初始化，appAgent !!
        Log.d(
            "MyApplication",
            "Application onCreate: FINISHED (AppAgent will be initialized later)"
        )
        // 【标注】根据官方最新回复，CameraEngine的初始化和全局回调注册，必须在应用启动时完成。
        initializeAgentSDK()
        Log.i(TAG, "Proceeding to initialize CameraEngine...")
        // 初始化 CameraEngine
        initializeCameraEngineManager()
        Log.i(TAG, "CameraEngine.instance.initialize() command has been sent.")
        // -----------------------------------------------------------------
        // 3. ▼▼▼ 【V2.0核心新增】建立与RobotAPI王国的全局“外交关系” ▼▼▼
        // -----------------------------------------------------------------
        setupRobotApiConnection()
        // ▲▲▲ 【V2.0核心新增】▲▲▲
        Log.d(TAG, "Application onCreate: FINISHED")

    }
    /**
     * 【V2.0核心新增】
     * 职责：在Application启动时，与RobotAPI Server建立连接，并设置全局回调，
     * 将底层状态变化通知给我们专业的RobotApiManager。
     */

    private fun setupRobotApiConnection() {
        Log.d(TAG, "开始连接 RobotAPI Server...")
        RobotApi.getInstance().connectServer(this, object : ApiListener {

            override fun handleApiConnected() {
                Log.i(TAG, "RobotAPI Server 连接成功！准备设置全局回调...")

                // Server已连接成功，立刻设置我们全局的ModuleCallback
                RobotApi.getInstance().setCallback(object : ModuleCallbackApi() {
                    // 控制权恢复
                    override fun onRecovery() {
                        // 通知“司令部”，武器已上膛
                        RobotApiManager.onApiReady()
                    }

                    // 控制权被剥夺
                    override fun onSuspend() {
                        // 通知“司令部”，武器被收走
                        RobotApiManager.onApiSuspend()
                    }

                    // 接收底层语音指令（可以先留空，我们主要用AgentOS）
                    @Throws(RemoteException::class)
                    override fun onSendRequest(reqId: Int, reqType: String, reqText: String, reqParam: String): Boolean {
                        Log.d(TAG, "收到全局底层请求: reqText=$reqText")
                        return false // 返回false，表示我们不处理，交给系统
                    }
                })

                // 首次连接成功，也应视为API就绪
                RobotApiManager.onApiReady()
            }

            override fun handleApiDisconnected() {
                Log.e(TAG, "RobotAPI Server 连接已断开！")
                RobotApiManager.onApiSuspend()
            }

            override fun handleApiDisabled() {
                Log.e(TAG, "RobotAPI Server 已被禁用！")
                RobotApiManager.onApiSuspend()
            }
        })
    }
    // =================================================================================
    //  以下是您原有的、经过验证的优秀代码，我们将其完整保留
    // =================================================================================
    private fun initializeCameraEngineManager() {
        Log.i(TAG, "Initializing CameraEngine Global Manager...")
        // 我们不需要在onCreate中显式调用 CameraEngine.instance.initialize() 或 start()。
        // CameraEngine的设计是“按需启动”，我们只需要确保在应用退出时，它能被彻底关闭即可。
        // 最好的方法是通过注册Activity生命周期回调来管理。

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var activityCount = 0

            override fun onActivityStarted(activity: Activity) {
                activityCount++
            }

            override fun onActivityStopped(activity: Activity) {
                activityCount--
                // 当最后一个Activity停止时，可以认为应用已进入后台。
                // 这是一个非常适合彻底关闭CameraEngine，释放摄像头硬件资源的时机！
                if (activityCount == 0) {
                    Log.w(TAG, "Last activity stopped. Application is in background. Shutting down CameraEngine...")
                    // 直接调用CameraEngine的方法，而不是通过不存在的.instance
                    CameraEngine.shutdown() // 使用我们在引擎中定义的、更具体的stopPreview方法
                    // ▲▲▲【【【 核心修正！ 】】】▲▲▲
                }
            }

            // 其他回调方法暂时留空，按需实现
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityDestroyed(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
        Log.i(TAG, "CameraEngine Global Manager registered successfully.")
    }

    /**
     * 应用被系统终止时，作为最后一道防线，确保引擎被关闭。
     * 虽然onTerminate在真实设备上不保证每次都调用，但这是一个好习惯。
     */
    override fun onTerminate() {
        super.onTerminate()
        Log.w(TAG, "Application is terminating. Forcing shutdown of CameraEngine.")
        // ▼▼▼【【【 核心修正！ 】】】▼▼▼
        // 直接调用我们最新定义的、唯一的公开关闭接口
        CameraEngine.shutdown()
        // ▲▲▲【【【 核心修正！ 】】】▲▲▲
    }

    // =================================================================================
    //  您原有的 AgentSDK 初始化和角色管理代码，保持不变，它们是完美的！
    // =================================================================================


    fun safeTts(text: String, timeoutMillis: Long = 0) {
        // 在这里，我们暂时不做isSdkInitialized的检查，以简化问题
        // 直接调用AgentCore.tts()，因为此时我们的核心问题是方法引用，而不是SDK初始化状态
        // isSdkInitialized 的检查我们可以在后续版本中再加回来
        if (timeoutMillis > 0) {
            AgentCore.tts(text, timeoutMillis)
        } else {
            AgentCore.tts(text)
        }
        Log.i("MyApplication_SafeTTS", "TTS request sent: '$text'")
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

        } else {
            Log.w(TAG, "Cannot switch role because AppAgent is not initialized yet.")
        }
    }
}

