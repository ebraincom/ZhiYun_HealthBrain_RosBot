package com.zhiyun.agentrobot

import android.app.Application
import android.os.Bundle // 需要导入 Bundle
import android.util.Log // 建议添加 Log 用于调试
import com.ainirobot.agent.AppAgent
import com.ainirobot.agent.action.Action // 需要导入 Action
import com.ainirobot.agent.action.ActionExecutor // 导入 ActionExecutor 接口

class MyApplication : Application() {

    lateinit var appAgent: AppAgent
        private set // 可选：如果希望 AppAgent 实例在 Application 外部是只读的

    override fun onCreate() {
        super.onCreate()

        appAgent = object : AppAgent(this) {
            override fun onCreate() {
                // 这是 AppAgent 初始化完成后的回调
                // 在这里设置 Persona 和 Style
                setPersona("我是智芸小智，您的专属智能助手，致力于提供专业和细致的康养服务。")
                setStyle("专业、友好、乐于助人")
                // 您可以在这里进行 AppAgent 相关的进一步配置，或者动态注册全局 Action
                Log.d("MyApplication", "AppAgent internal onCreate called.")
                // 在这里注册我们的第一个全局 Action
                this@MyApplication.registerHelloWorldAction() // 或者直接调用 registerHelloWorldAction() 也可以，因为作用
            }

            override fun onExecuteAction(action: Action, params: Bundle?): Boolean {
                // 处理通过 actionRegistry.json 静态注册的 Action 的执行
                // 如果您没有使用静态注册的 Action，这个方法可能不会被频繁调用
                Log.d("MyApplication", "AppAgent onExecuteAction for action: ${action.name}")
                // 根据您是否处理了该 action 返回 true 或 false
                return false // 默认不处理，交由其他机制（例如 PageAgent 或动态注册的 ActionExecutor）
            }
        }
    }
    // 将 registerHelloWorldAction 定义为 MyApplication 类的一个私有方法
    private fun registerHelloWorldAction() {
        // 定义一个简单的 Action
        val helloWorldAction = Action(
            name = "com.zhiyun.agentrobot.action.HELLO_WORLD", // Action 的唯一名称
            displayName = "打个招呼", // 显示名称
            desc = "一个简单的打招呼动作，会打印一条日志。", // 描述，给大模型理解用
            parameters = emptyList(), // 使用 emptyList() 表示没有参数
            executor = object : ActionExecutor { // Action 的执行器
                override fun onExecute(action: Action, params: Bundle?): Boolean {
                    Log.i("HelloWorldAction", "Hello World from AppAgent! Action ID: ${action.sid}, User Query: ${action.userQuery}")
                    action.notify() // 通知 Action 执行完成 (默认成功)
                    return true // 表示 Action 已被处理
                }
            }
        )

        appAgent.registerAction(helloWorldAction)
        Log.d("MyApplication", "Registered action: ${helloWorldAction.name}")

    }
}