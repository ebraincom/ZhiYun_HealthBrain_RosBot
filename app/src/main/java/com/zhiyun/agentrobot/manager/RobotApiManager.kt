// 文件路径: com/zhiyun/agentrobot/manager/RobotApiManager.kt
package com.zhiyun.agentrobot.manager

// ▼▼▼ 【最终胜利版 v10】: 采用最精简、最正确的 import 语句 ▼▼▼
import android.util.Log
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.CommandListener
import org.json.JSONException
import org.json.JSONObject
// ▲▲▲ 【最终胜利版 v10】▲▲▲

object RobotApiManager {

    @Volatile
    private var isApiReady: Boolean = false

    fun onApiReady() {
        isApiReady = true
        Log.i("RobotApiManager", "RobotAPI 控制权已恢复或连接成功！")
    }

    fun onApiSuspend() {
        isApiReady = false
        Log.w("RobotApiManager", "RobotAPI 控制权已挂起或连接断开！")
    }

    // --- 头部云台控制区 ---
    fun moveHead(vAngle: Int, hAngle: Int = 0) {
        if (!isApiReady) {
            Log.e("RobotApiManager", "无法移动头部：RobotAPI 未就绪")
            return
        }
        val reqId = 1
        Log.d("RobotApiManager", "执行 moveHead: vAngle=$vAngle, hAngle=$hAngle (reqId: $reqId)")
        RobotApi.getInstance().moveHead(
            reqId,
            "relative",
            "relative",
            hAngle,
            vAngle,
            createGenericCommandListener("moveHead", reqId)
        )
    }

    fun resetHead() {
        if (!isApiReady) {
            Log.e("RobotApiManager", "无法复位头部：RobotAPI 未就绪")
            return
        }
        val reqId = 2
        Log.d("RobotApiManager", "执行 resetHead (reqId: $reqId)")
        RobotApi.getInstance().resetHead(
            reqId,
            createGenericCommandListener("resetHead", reqId)
        )
    }

    // --- 通用回调处理区 ---
    // ▼▼▼ 【最终胜利版 v10】: CommandListener 是一个类，所以必须调用它的构造函数！▼▼▼
    private fun createGenericCommandListener(actionName: String, reqId: Int): CommandListener {
        // 【核心修正】: CommandListener是一个类，必须用 object : CommandListener() 来创建匿名子类实例。
        return object : CommandListener() {
            // 【核心修正】: onResult的参数根据官方文档，应该是 (Int, String)
            override fun onResult(result: Int, message: String) {
                try {
                    if (result != Definition.RESULT_OK) {
                        Log.e("RobotApiManager", "[$actionName #$reqId] 操作失败，底层返回错误: result=$result, message=$message")
                        return
                    }

                    // 严格按照官方文档，使用 getString
                    val json = JSONObject(message)
                    val status = json.getString("status")

                    if (Definition.CMD_STATUS_OK == status) {
                        Log.i("RobotApiManager", "[$actionName #$reqId] 操作成功！Status OK.")
                    } else {
                        Log.w("RobotApiManager", "[$actionName #$reqId] 操作状态非OK: $message")
                    }
                } catch (e: JSONException) {
                    Log.e("RobotApiManager", "[$actionName #$reqId] 解析回调JSON失败: $message", e)
                } catch (e: Exception) {
                    Log.e("RobotApiManager", "[$actionName #$reqId] 处理回调时发生未知错误", e)
                }
            }

            // 【核心修正】: 既然 CommandListener 是一个类，我们只需要重写我们需要的方法。
            // 如果 onStatusUpdate 不是抽象方法，我们甚至不需要写出来。
            // 为了保险起见，我们先假设它不是必须的。如果编译还需要，再补上。
            // 经过对文档的再次审视，moveHead和resetHead的回调只有 onResult(int, String)。
        }
    }
    // ▲▲▲ 【最终胜利版 v10】▲▲▲
}
