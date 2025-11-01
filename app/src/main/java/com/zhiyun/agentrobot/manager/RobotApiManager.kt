// In file: com/zhiyun/agentrobot/manager/RobotApiManager.kt

package com.zhiyun.agentrobot.manager

// ▼▼▼ 【最终决议 1/6：使用【绝对正确】的引用】 ▼▼▼
import android.util.Log
import com.ainirobot.agent.AgentCore
import com.ainirobot.agent.action.Action
import com.ainirobot.agent.base.ActionResult
import com.ainirobot.agent.base.ActionStatus
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.CommandListener
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
// ▲▲▲ 【最终决议 1/6】 ▲▲▲

object RobotApiManager {

    private const val TAG = "RobotApiManager"

    // ▼▼▼ 【最终决议 2/6：修复编译错误 - `@Volatile`只能用于`var`】 ▼▼▼
    @Volatile
    private var isApiReady: Boolean = false // ✅ 修正：val -> var
    // ▲▲▲ 【最终决议 2/6】 ▲▲▲

    // ▼▼▼ 【最终决议 3/6：引入单例锁 (保持不变)】 ▼▼▼
    private val isMotorBusy = AtomicBoolean(false) // ✅ 这个是 val，因为 AtomicBoolean 对象本身是不可变的，但其内部值是可变的，所以不需要 @Volatile
    // ▲▲▲ 【最终决议 3/6】 ▲▲▲

    fun onApiReady() {
        this.isApiReady = true // 使用 this 明确指向
        Log.i(TAG, "RobotAPI 控制权已恢复或连接成功！")
    }

    fun onApiSuspend() {
        this.isApiReady = false
        Log.w(TAG, "RobotAPI 控制权已挂起或连接断开！")
    }

    // ▼▼▼ 【最终决议 4/6：改造运动方法，并调用【正确】的回调】 ▼▼▼
    // --- 头部云台控制区 ---
    fun moveHead(action: Action, vAngle: Int, hAngle: Int) {
        // ✅ 修正：方法内部直接引用 isApiReady
        if (!isApiReady) { handleApiNotReady(action); return }
        if (!isMotorBusy.compareAndSet(false, true)) { handleMotorBusy(action); return }
        val reqId = 1
        // 【关键修正】调用头部专用的回调处理器
        RobotApi.getInstance().moveHead(reqId, "relative", "relative", hAngle, vAngle, createHeadListener(action))
    }

    fun resetHead(action: Action) {
        if (!isApiReady) { handleApiNotReady(action); return }
        if (!isMotorBusy.compareAndSet(false, true)) { handleMotorBusy(action); return }
        val reqId = 2
        // 【关键修正】调用头部专用的回调处理器
        RobotApi.getInstance().resetHead(reqId, createHeadListener(action))
    }

    // --- 基础运动控制区 ---
    fun goForward(action: Action, distance: Float) {
        if (!isApiReady) { handleApiNotReady(action); return }
        if (!isMotorBusy.compareAndSet(false, true)) { handleMotorBusy(action); return }
        val reqId = 3
        // 【关键修正】调用基础运动专用的回调处理器
        // ✅ 修正：根据官方文档，goForward 有一个可选的 avoid 参数，我们暂时用默认值 true
        RobotApi.getInstance().goForward(reqId, 0.2f, distance, true, createMotionListener(action))
    }
    // ▼▼▼ 【总攻-补完计划】 ▼▼▼
    fun goBackward(action: Action, distance: Float) {
        if (!isApiReady) { handleApiNotReady(action); return }
        if (!isMotorBusy.compareAndSet(false, true)) { handleMotorBusy(action); return }
        val reqId = 4
        RobotApi.getInstance().goBackward(reqId, 0.2f, distance, createMotionListener(action))
    }

    fun turnLeft(action: Action, angle: Float) {
        if (!isApiReady) { handleApiNotReady(action); return }
        if (!isMotorBusy.compareAndSet(false, true)) { handleMotorBusy(action); return }
        val reqId = 5
        RobotApi.getInstance().turnLeft(reqId, 20f, angle, createMotionListener(action))
    }

    fun turnRight(action: Action, angle: Float) {
        if (!isApiReady) { handleApiNotReady(action); return }
        if (!isMotorBusy.compareAndSet(false, true)) { handleMotorBusy(action); return }
        val reqId = 6
        RobotApi.getInstance().turnRight(reqId, 20f, angle, createMotionListener(action))
    }

    fun stopMove(action: Action) {
        // 停止操作比较特殊，它应该能打断其他操作，所以不检查 isMotorBusy
        // 但它依然需要一个完整的交权和解锁流程
        if (!isApiReady) { handleApiNotReady(action); return }

        val reqId = 99
        Log.d(TAG, "执行 stopMove (reqId: $reqId)")
        // 注意：停止操作也使用基础移动回调，并标记isStopAction为true来确保锁能被正确释放
        RobotApi.getInstance().stopMove(reqId, createMotionListener(action))
    }


    // ▼▼▼ 【最终决议 5/6：创建【两种】绝对正确的回调处理器和释放STOP锁】 ▼▼▼
    /**
     *  回调处理器1：用于处理 moveHead/resetHead 的复杂JSON回调 (参照官方文档)
     */
    private fun createHeadListener(action: Action): CommandListener {
        return object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                try {
                    val status = JSONObject(message).getString("status")
                    val isSuccess = Definition.CMD_STATUS_OK.equals(status, ignoreCase = true)
                    Log.d(action.name, "头部回调: message='$message', success=$isSuccess")
                    action.notify(ActionResult(if (isSuccess) ActionStatus.SUCCEEDED else ActionStatus.FAILED))
                } catch (e: Exception) {
                    Log.e(action.name, "头部回调解析失败", e)
                    action.notify(ActionResult(ActionStatus.FAILED))
                } finally {
                    isMotorBusy.set(false) // ✅ 释放锁
                    Log.i(TAG, "[${action.name}] 单例锁已释放！")
                }
            }
        }
    }

    /**
     *  回调处理器2：用于处理 goForward/turnLeft 等基础移动的简单 "succeed" 字符串回调 (参照官方文档)
     */
    private fun createMotionListener(action: Action): CommandListener {
        return object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                try {
                    val isSuccess = "succeed".equals(message, ignoreCase = true)
                    Log.d(action.name, "基础移动回调: result=$result, message='$message', success=$isSuccess")
                    action.notify(ActionResult(if (isSuccess) ActionStatus.SUCCEEDED else ActionStatus.FAILED))
                } catch (e: Exception) {
                    Log.e(action.name, "基础移动回调处理异常", e)
                    action.notify(ActionResult(ActionStatus.FAILED))
                } finally {
                    isMotorBusy.set(false) // ✅ 释放锁
                    Log.i(TAG, "[${action.name}] 单例锁已释放！")
                }
            }
        }
    }
    // stopMove成功后，必须把那个被中断的动作占用的锁给释放掉，否则后续所有动作都会被卡死。
    private fun createStopListener(action: Action): CommandListener {
        return object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                try {
                    val isSuccess = "succeed".equals(message, ignoreCase = true)
                    Log.d(action.name, "停止回调: result=$result, message='$message', success=$isSuccess")
                    action.notify(ActionResult(if (isSuccess) ActionStatus.SUCCEEDED else ActionStatus.FAILED))
                } catch (e: Exception) {
                    Log.e(action.name, "停止回调处理异常", e)
                    action.notify(ActionResult(ActionStatus.FAILED))
                } finally {
                    // 【核心】stopMove成功后，必须释放锁，否则被它中断的那个动作的锁就永远释放不掉了！
                    if (isMotorBusy.get()) {
                        isMotorBusy.set(false)
                        Log.i(TAG, "[${action.name}] 强制释放了被中断动作的单例锁！")
                    } else {
                        // 如果调用stopMove时，机器人本来就没在动，那就没锁可放，这也是正常情况
                        Log.i(TAG, "[${action.name}] 执行时没有检测到运动锁，无需释放。")
                    }
                }
            }
        }
    }


    // ▼▼▼ 【最终决议 6/6：辅助函数 (使用正确接口)】 ▼▼▼
    private fun handleApiNotReady(action: Action) {
        Log.e(TAG, "无法执行操作：RobotAPI 未就绪")
        AgentCore.tts("机器人还没有准备好，请稍等一下。")
        action.notify(ActionResult(ActionStatus.FAILED))
    }

    private fun handleMotorBusy(action: Action) {
        Log.w(TAG, "无法执行操作：机器人正忙！")
        AgentCore.tts("我正在执行上一个动作，请稍等。")
        action.notify(ActionResult(ActionStatus.FAILED))
    }
    // ▲▲▲ 【最终决议 6/6】 ▲▲▲
}

