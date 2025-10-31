// 文件路径: .../com/zhiyun/agentrobot/RobotActionFactory.kt

package com.zhiyun.agentrobot

import android.os.Bundle
import android.util.Log
import com.ainirobot.agent.action.Action
import com.ainirobot.agent.action.ActionExecutor
import com.ainirobot.agent.base.ActionResult
import com.ainirobot.agent.base.ActionStatus
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.CommandListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject


/**
 * 机器人动作兵工厂 (单例对象)
 * 吸收了官方DEMO的最佳实践，负责创建所有与RobotAPI交互的Action。
 */
object RobotActionFactory {

    private val TAG = "RobotActionFactory"
    private val ACTION_PREFIX = "com.zhiyun.agentrobot.action."
    private var reqId = 0

    /**
     * 【核心实现1/3：通用神经末梢】
     * 创建一个通用的、可复用的回调处理器。
     * 它能正确“翻译”RobotAPI返回的两种成功消息("succeed"或JSON)，
     * 并将最终结果(SUCCEEDED/FAILED)通知给AgentOS。
     */
    // ▼▼▼【最终加固1/2：升级通用神经末梢】▼▼▼
    private fun createUniversalListener(action: Action): CommandListener {
        return object : CommandListener() {
            override fun onResult(result: Int, message: String?) {
                Log.d(TAG, "Action[${action.name}] onResult: result=$result, message='$message'")

                var isSuccess = false
                // 关卡1：处理 "succeed"
                if ("succeed".equals(message, ignoreCase = true)) {
                    isSuccess = true
                }
                // 【新情报】关卡2：处理 "turn head success"
                else if (message?.contains("success", ignoreCase = true) == true) {
                    isSuccess = true
                }
                // 关卡3：处理JSON
                else {
                    try {
                        if (message != null) {
                            val status = JSONObject(message).getString("status")
                            if ("ok".equals(status, ignoreCase = true)) {
                                isSuccess = true
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Not a valid success JSON: $message")
                    }
                }

                val actionResult = ActionResult(if (isSuccess) ActionStatus.SUCCEEDED else ActionStatus.FAILED)
                Log.i(TAG, "Action[${action.name}] completed. Notifying AgentOS with status code: ${actionResult.status}")
                action.notify(actionResult)
            }
        }
    }
    // ▲▲▲【最终加固 1/2：升级通用神经末梢】▲▲▲


    /**
     * 【核心实现2/3：模块化动作创建】
     * 创建所有机器人相关的Action列表。
     * @param scope 用于在协程中执行API调用，避免阻塞主线程。
     */
    fun createAllRobotActions(scope: CoroutineScope): List<Action> {
        val actions = mutableListOf<Action>()
        actions.addAll(createHeadActions(scope))
        actions.addAll(createMotionActions(scope))
        Log.i(TAG, "All robot actions have been created by the factory.")
        return actions
    }
    // ▼▼▼【最终修正 1/2：添加健壮的数字获取函数】▼▼▼
    /**
     * 从Bundle中安全地获取一个数字并转换为Float。
     * AgentOS可能传来Integer、Double或Float，此函数都能兼容。
     */
    private fun getNumber(params: Bundle?, key: String, defaultValue: Float): Float {
        // 如果 params 为 null，直接返回默认值
        if (params == null) return defaultValue

        val value = params.get(key)
        return when (value) {
            is Float -> value
            is Double -> value.toFloat()
            is Int -> value.toFloat()
            is String -> value.toFloatOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
// ▲▲▲【最终修正 1/2：添加健壮的数字获取函数】▲▲▲


    // --- 兵工厂的具体生产线 ---

    // ▼▼▼【最终修正 2/3：修复了参数获取的“头部动作生产线”】▼▼▼
    private fun createHeadActions(scope: CoroutineScope): List<Action> {

        // 1. 移动头部
        val moveHeadAction = Action(ACTION_PREFIX + "HEAD_MOVE")
        moveHeadAction.executor = createExecutor(scope) { action, params ->
            // 【关键修复】在正确的作用域内，使用 getNumber 获取参数
            val vAngle = getNumber(params, "vAngle", 0f)
            val hAngle = getNumber(params, "hAngle", 0f)

            Log.i(TAG, "Executing HEAD_MOVE: vAngle=$vAngle, hAngle=$hAngle")

            RobotApi.getInstance().moveHead(
                reqId++, "relative", "relative",
                hAngle.toInt(),  // 根据API，需要Int
                vAngle.toInt(),  // 根据API，需要Int
                createUniversalListener(action)
            )
        }
        // 2. 头部回正
        val resetHeadAction = Action(ACTION_PREFIX + "HEAD_RESET")
        resetHeadAction.executor = createExecutor(scope) { action, _ ->
            Log.i(TAG, "Executing HEAD_RESET")
            RobotApi.getInstance().resetHead(reqId++, createUniversalListener(action))
        }

        return listOf(moveHeadAction, resetHeadAction)
    }

    // ▼▼▼【最终加固 2/2：统一输弹系统】▼▼▼
    private fun createMotionActions(scope: CoroutineScope): List<Action> {
        // 3. 前进
        val goForwardAction = Action(ACTION_PREFIX + "MOTION_GO_FORWARD")
        goForwardAction.executor = createExecutor(scope) { action, params ->
            // 【加固】统一使用我们更健壮的 getNumber 函数！
            val distance = getNumber(params, "distance", 0.3f)
            Log.i(TAG, "Executing MOTION_GO_FORWARD: distance=$distance")
            RobotApi.getInstance()
                .goForward(reqId++, 0.2f, distance, true, createUniversalListener(action))
        }

        // 4. 后退
        val goBackwardAction = Action(ACTION_PREFIX + "MOTION_GO_BACKWARD")
        goBackwardAction.executor = createExecutor(scope) { action, params ->
            // 【加固】统一使用我们更健壮的 getNumber 函数！
            val distance = getNumber(params, "distance", 0.3f)
            Log.i(TAG, "Executing MOTION_GO_BACKWARD: distance=$distance")
            RobotApi.getInstance()
                .goBackward(reqId++, 0.2f, distance, createUniversalListener(action))
        }

        // 5. 左转
        val turnLeftAction = Action(ACTION_PREFIX + "MOTION_TURN_LEFT")
        turnLeftAction.executor = createExecutor(scope) { action, params ->
            // 【加固】统一使用我们更健壮的 getNumber 函数！
            val angle = getNumber(params, "angle", 90f)
            Log.i(TAG, "Executing MOTION_TURN_LEFT: angle=$angle")
            RobotApi.getInstance().turnLeft(reqId++, 20f, angle, createUniversalListener(action))
        }

        // 6. 右转
        val turnRightAction = Action(ACTION_PREFIX + "MOTION_TURN_RIGHT")
        turnRightAction.executor = createExecutor(scope) { action, params ->
            // 【加固】统一使用我们更健壮的 getNumber 函数！
            val angle = getNumber(params, "angle", 90f)
            Log.i(TAG, "Executing MOTION_TURN_RIGHT: angle=$angle")
            RobotApi.getInstance().turnRight(reqId++, 20f, angle, createUniversalListener(action))
        }

        // 7. 停止移动
        val stopMoveAction = Action(ACTION_PREFIX + "MOTION_STOP_MOVE")
        stopMoveAction.executor = createExecutor(scope) { action, _ ->
            Log.i(TAG, "Executing MOTION_STOP_MOVE")
            RobotApi.getInstance().stopMove(reqId++, createUniversalListener(action))
        }

        return listOf(
            goForwardAction,
            goBackwardAction,
            turnLeftAction,
            turnRightAction,
            stopMoveAction
        )
    }
    // ▲▲▲【最终加固 2/2：统一输弹系统】▲▲▲


    /**
     * 【核心实现3/3：执行器模板】
     * 这是一个辅助函数，用于创建一个标准的ActionExecutor。
     * 它能确保所有的RobotAPI调用都在指定的协程作用域(scope)中执行，
     * 从而保证了API调用不会阻塞主线程。
     */

    // ▼▼▼【最终修正：解决ActionExecutor构造错误】▼▼▼
    // 放弃SAM语法糖，回归最明确、最稳定的“匿名对象”写法，以彻底解决编译器错误。
    private fun createExecutor(
        scope: CoroutineScope,
        // 【优化】让block挂起，代码更优雅
        block: suspend (action: Action, params: Bundle?) -> Unit
    ): ActionExecutor {
        // 使用 object : ActionExecutor 来明确地创建一个实现了该接口的匿名对象实例。
        return object : ActionExecutor {
            // 重写接口中定义的 onExecute 方法
            override fun onExecute(action: Action, params: Bundle?): Boolean {
                // 在协程中执行我们的挂起函数block
                scope.launch {
                    block(action, params)
                }
                // 立即返回true，表示接受了任务。真正的结果将在回调中异步通知。
                return true
            }
        }
    }
}
// ▲▲▲【最终修正：解决ActionExecutor构造错误】▲▲▲


