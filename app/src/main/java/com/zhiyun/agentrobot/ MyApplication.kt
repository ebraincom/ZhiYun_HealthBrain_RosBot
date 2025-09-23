package com.zhiyun.agentrobot

import android.app.Application
import android.os.Bundle // 需要导入 Bundle
import android.util.Log // 建议添加 Log 用于调试
import com.ainirobot.agent.AppAgent
import com.ainirobot.agent.action.Action // 需要导入 Action
// import com.ainirobot.agent.action.ActionExecutor // 导入 ActionExecutor 接口

class MyApplication : Application() {

    lateinit var appAgent: AppAgent
        private set // 可选：如果希望 AppAgent 实例在 Application 外部是只读的

    override fun onCreate() {
        Log.d("MyApplication", "Application onCreate: START") // 更早的日志
        super.onCreate()
        Log.d("MyApplication", "Application super.onCreate() called")

        try {
            Log.d("MyApplication", "AppAgent initialization: START")
            appAgent = object : AppAgent(this@MyApplication) {
                override fun onCreate() {
                    // 这是 AppAgent 初始化完成后的回调
                    // 在这里设置 Persona 和 Style
                    Log.d("MyApplication", "AppAgent internal onCreate: START")
                    setPersona("我是智芸小智，您的专属智能助手，致力于提供专业和细致的康养服务。")
                    setStyle("专业、友好、乐于助人")
                    Log.d("MyApplication", "AppAgent internal onCreate: Persona and Style SET")
                    //Log.d("MyApplication", "AppAgent internal onCreate called.")
                    // this@MyApplication.registerHelloWorldAction() // 或者直接调用 registerHelloWorldAction() 也可以，因为作用
                    Log.d("MyApplication", "AppAgent internal onCreate: FINISHED")
                }

                override fun onExecuteAction(action: Action, params: Bundle?): Boolean {
                    Log.d("MyApplication", "AppAgent onExecuteAction for action: ${action.name}")
                    return false
                }
            }
            Log.d("MyApplication", "AppAgent initialization: FINISHED")
        } catch (e: Throwable) { // 捕获所有可能的异常和错误
            Log.e("MyApplication", "FATAL ERROR during AppAgent initialization: ${e.message}", e)
            // 在这里可以考虑添加更强的错误处理，比如上传错误报告等
            throw e // 重新抛出，让系统知道发生了严重错误（或者不抛出，但应用可能状态不一致）
        }
        Log.d("MyApplication", "Application onCreate: FINISHED")
    }
}
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
                    // return true // 表示 Action 已被处理
                // }
           // }
        //)

        // appAgent.registerAction(helloWorldAction)
        // Log.d("MyApplication", "Registered action: ${helloWorldAction.name}")

    // }
// }