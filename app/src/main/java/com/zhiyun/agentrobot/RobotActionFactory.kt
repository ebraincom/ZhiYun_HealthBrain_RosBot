// 文件路径: com/zhiyun/agentrobot/RobotActionFactory.kt
// 状态: 【终极决战-圣经遵从版】

package com.zhiyun.agentrobot

import android.os.Bundle
import android.util.Log
import com.ainirobot.agent.action.Action
import com.ainirobot.agent.action.ActionExecutor
import com.ainirobot.agent.base.ActionResult
import com.ainirobot.agent.base.ActionStatus
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.CommandListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject

object RobotActionFactory {

    private const val TAG = "RobotActionFactory"
    private const val ACTION_PREFIX = "com.zhiyun.agentrobot.action."
    private var reqId = 0
    private val isMotorBusy = java.util.concurrent.atomic.AtomicBoolean(false)

    // ✅ 【回调处理器 1/2：字符串模式】 (用于底盘运动)
    private fun createStringBasedListener(action: Action, actionNameForLog: String): CommandListener {
        return object : CommandListener() {
            override fun onResult(result: Int, message: String?) {
                try {
                    Log.d(TAG, "Action[$actionNameForLog] onResult: result=$result, message='$message'")
                    // 修正：增加对"Motion avoid stop"的判断
                    val isSuccess = "succeed".equals(message, ignoreCase = true) || message?.contains("stop", ignoreCase = true) == true
                    val finalStatus = if (isSuccess) ActionStatus.SUCCEEDED else ActionStatus.FAILED
                    Log.i(TAG, "Action[$actionNameForLog] completed. Notifying AgentOS with status: $finalStatus")
                    action.notify(ActionResult(finalStatus))
                } finally {
                    isMotorBusy.set(false)
                    Log.d(TAG, "并发锁已释放 by Action[$actionNameForLog].")
                }
            }
        }
    }

    // ✅ 【回调处理器 2/2：JSON模式】 (用于头部运动)
    private fun createJsonBasedListener(action: Action, actionNameForLog: String): CommandListener {
        return object : CommandListener() {
            override fun onResult(result: Int, message: String?) {
                try {
                    Log.d(TAG, "Action[$actionNameForLog] onResult: result=$result, message='$message'")
                    var isSuccess = false
                    if (message != null) {
                        try {
                            val status = JSONObject(message).getString("status")
                            if (Definition.CMD_STATUS_OK.equals(status, ignoreCase = true)) {
                                isSuccess = true
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "无法从JSON中解析出成功状态 for $actionNameForLog: $message")
                        }
                    }
                    val finalStatus = if (isSuccess) ActionStatus.SUCCEEDED else ActionStatus.FAILED
                    Log.i(TAG, "Action[$actionNameForLog] completed. Notifying AgentOS with status: $finalStatus")
                    action.notify(ActionResult(finalStatus))
                } finally {
                    isMotorBusy.set(false)
                    Log.d(TAG, "并发锁已释放 by Action[$actionNameForLog].")
                }
            }
        }
    }

    // ✅ 【拒绝执行时开口说话】
    private fun createExecutor(scope: CoroutineScope, isStopAction: Boolean = false, block: (action: Action, params: Bundle?) -> Unit): ActionExecutor {
        return object : ActionExecutor {
            override fun onExecute(action: Action, params: Bundle?): Boolean {
                if (!isStopAction && !isMotorBusy.compareAndSet(false, true)) {
                    Log.w(TAG, "无法执行 [${action.name}]：机器人正忙！")
                    action.notify(ActionResult(ActionStatus.FAILED))
                    return false
                }
                if (isStopAction) Log.d(TAG, "执行停止动作，绕过加锁检查。")
                else Log.d(TAG, "并发锁已获取 by Action[${action.name}].")

                scope.launch { block(action, params) }
                return true
            }
        }
    }

    fun createAllRobotActions(scope: CoroutineScope): List<Action> {
        val headActions = createHeadActions(scope)
        val motionActions = createMotionActions(scope)
        val allActions = mutableListOf<Action>()
        allActions.addAll(headActions)
        allActions.addAll(motionActions)
        Log.i(TAG, "兵工厂总装完毕，共计 ${allActions.size} 个机器人动作。")
        return allActions
    }

    // ✅✅✅ 【【【 终极生产线修正 】】】 ✅✅✅
    private fun createHeadActions(scope: CoroutineScope): List<Action> {
        val moveHeadAction = Action(ACTION_PREFIX + "HEAD_MOVE")
        moveHeadAction.executor = createExecutor(scope) { action, params ->
            var vAngle = 0 // 默认值
            if (params != null) {
                // ✅✅✅ 【【【 终极健壮参数解析 】】】 ✅✅✅
                // 1. 先尝试按数字类型获取
                vAngle = params.getInt("vAngle", -999)
                if (vAngle == -999) {
                    // 2. 如果失败，再尝试按字符串类型获取并转换
                    vAngle = params.getString("vAngle")?.toFloatOrNull()?.toInt() ?: 0
                }
            }

            val hAngle = 0 // 永远是0
            Log.i(TAG, "Executing HEAD_MOVE: vAngle=$vAngle (最终解析值), hAngle=$hAngle")
            RobotApi.getInstance().moveHead(reqId++, "relative", "relative", hAngle, vAngle, createJsonBasedListener(action, "移动头部"))
        }

        val resetHeadAction = Action(ACTION_PREFIX + "HEAD_RESET")
        resetHeadAction.executor = createExecutor(scope) { action, _ ->
            Log.i(TAG, "Executing HEAD_RESET")
            // 使用100%遵从文档的 JSON Listener
            RobotApi.getInstance().resetHead(reqId++, createJsonBasedListener(action, "头部回正"))
        }

        return listOf(moveHeadAction, resetHeadAction)
    }

    private fun createMotionActions(scope: CoroutineScope): List<Action> {
        val goForwardAction = Action(ACTION_PREFIX + "MOTION_GO_FORWARD")
        goForwardAction.executor = createExecutor(scope) { action, params ->
            val distance = params?.getString("distance")?.toFloatOrNull() ?: 0.3f
            Log.i(TAG, "Executing MOTION_GO_FORWARD: distance=$distance")
            RobotApi.getInstance().goForward(reqId++, 0.2f, distance, true, createStringBasedListener(action, "前进"))
        }

        val goBackwardAction = Action(ACTION_PREFIX + "MOTION_GO_BACKWARD")
        goBackwardAction.executor = createExecutor(scope) { action, params ->
            val distance = params?.getString("distance")?.toFloatOrNull() ?: 0.3f
            Log.i(TAG, "Executing MOTION_GO_BACKWARD: distance=$distance")
            RobotApi.getInstance().goBackward(reqId++, 0.2f, distance, createStringBasedListener(action, "后退"))
        }

        val turnLeftAction = Action(ACTION_PREFIX + "MOTION_TURN_LEFT")
        turnLeftAction.executor = createExecutor(scope) { action, params ->
            val angle = params?.getString("angle")?.toFloatOrNull() ?: 90f
            Log.i(TAG, "Executing MOTION_TURN_LEFT: angle=$angle")
            RobotApi.getInstance().turnLeft(reqId++, 20f, angle, createStringBasedListener(action, "左转"))
        }

        val turnRightAction = Action(ACTION_PREFIX + "MOTION_TURN_RIGHT")
        turnRightAction.executor = createExecutor(scope) { action, params ->
            val angle = params?.getString("angle")?.toFloatOrNull() ?: 90f
            Log.i(TAG, "Executing MOTION_TURN_RIGHT: angle=$angle")
            RobotApi.getInstance().turnRight(reqId++, 20f, angle, createStringBasedListener(action, "右转"))
        }

        val stopMoveAction = Action(ACTION_PREFIX + "MOTION_STOP_MOVE")
        stopMoveAction.executor = createExecutor(scope, isStopAction = true) { action, _ ->
            Log.i(TAG, "Executing MOTION_STOP_MOVE")
            RobotApi.getInstance().stopMove(reqId++, createStringBasedListener(action, "停止移动"))
        }

        return listOf(goForwardAction, goBackwardAction, turnLeftAction, turnRightAction, stopMoveAction)
    }
}
