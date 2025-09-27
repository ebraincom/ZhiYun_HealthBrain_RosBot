package com.zhiyun.agentrobot

import android.app.Application
import android.os.Bundle
import android.util.Log // 添加 Log 用于调试
import com.ainirobot.agent.AppAgent
import com.ainirobot.agent.action.Action // 需要导入 Action
// import com.ainirobot.agent.action.ActionExecutor // 导入 ActionExecutor 接口

class MyApplication : Application() {
    // lateinit var appAgent: AppAgent // 保持 latein
    // 或者，如果您希望更安全地检查是否已初始化：
    var appAgent: AppAgent? = null
        private set

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
        if (appAgent == null) {// 检查是否已初始化
            try {
                Log.d("MyApplication", "AppAgent initialization: START")
                this.appAgent = object : AppAgent(this@MyApplication) {
                    override fun onCreate() {
                        // 这是 AppAgent 初始化完成后的回调
                        // 在这里设置 Persona 和 Style
                        Log.d("MyApplication", "AppAgent internal onCreate: START")
                        setPersona("我是智芸小智，您的专属智能助手，致力于提供专业和细致的康养服务。")
                        setStyle("专业、友好、乐于助人")
                        Log.d("MyApplication", "AppAgent internal onCreate: Persona and Style SET")
                        Log.d("MyApplication", "AppAgent internal onCreate: FINISHED")
                    }

                    override fun onExecuteAction(action: Action, params: Bundle?): Boolean {
                        Log.d(
                            "MyApplication",
                            "AppAgent onExecuteAction for action: ${action.name}"
                        )
                        return false
                    }
                }
                Log.d(
                    "MyApplication",
                    "AppAgent initialization: FINISHED(called from Activity)"
                )
            } catch (e: Throwable) {
                Log.e(
                    "MyApplication",
                    "FATAL ERROR during AppAgent initialization: ${e.message}",
                    e
                )
                // 考虑是否真的要在这里重新抛出，或者只是记录错误
                // 如果这里抛出，而调用方没有捕获，调用方可能会崩溃
                // throw e
            }
        } else {
            Log.d("MyApplication", "AppAgent already initialized.")
        }
    }
}



// 在这里可以考虑添加更强的错误处理，比如上传错误报告等
// 将 registerHelloWorldAction 定义为 MyApplication 类的一个私有方法
    // private fun registerHelloWorldAction() {
        // 定义一个简单的 Action
        // val helloWorldAction = Action(
            // name = "com.zhiyun.agentrobot.action.HELLO_WORLD", // Action 的唯一名称
            // displayName = "打个招呼", // 显示名称
            // desc = "一个简单的打招呼动作，会打印一条日志。", // 描述，给大模型理解用
            // parameters = emptyList(), // 使用 emptyList() 表示没有参数
            // executor = object : ActionExecutor { // Action 的执行器
                // override fun onExecute(action: Action, params: Bundle?): Boolean {
                   //  Log.i("HelloWorldAction", "Hello World from AppAgent! Action ID: ${action.sid}, User Query: ${action.userQuery}")
                    // action.notify() // 通知 Action 执行完成 (默认成功)
