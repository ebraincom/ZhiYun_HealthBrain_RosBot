// ▼▼▼ 【V12-全面战争修正版】代码 - 修正所有命名，区分两种回调！ ▼▼▼
package com.zhiyun.agentrobot.fragment

import android.os.Bundle
import android.util.Log
import com.ainirobot.agent.PageAgent
import com.ainirobot.agent.action.Action
import com.ainirobot.agent.action.ActionExecutor
import com.ainirobot.agent.base.ActionResult
import com.ainirobot.agent.base.ActionStatus
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.CommandListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class RobotControlFragment(
    private val pageAgent: PageAgent,
    private val coroutineScope: CoroutineScope
) {
    private val TAG = "RobotControlFragment"
    private val ACTION_PREFIX = "com.zhiyun.agentrobot.action."
    private val reqId = 0 // 通用请求ID

    /**
     * 注册所有机器人动作（Action）
     */
    fun setupRobotActions() {
        Log.d(TAG, "开始以【全面战争】标准，注册所有已知动作...")

        // --- 注册头部云台动作 ---
        registerHeadActions()

        // --- 注册基础移动动作 ---
        registerMotionActions()

        Log.i(TAG, "所有动作已完成注册，系统进入完全战备状态！")
    }

    // =================================================================
    // 模块一：头部云台动作
    // =================================================================
    private fun registerHeadActions() {
        // 1. 移动头部
        val moveHeadAction = Action(ACTION_PREFIX + "HEAD_MOVE") // ★ 命名正确
        moveHeadAction.executor = object : ActionExecutor {
            override fun onExecute(action: Action, params: Bundle?): Boolean {
                coroutineScope.launch {
                    val vAngleFloat = params?.getFloat("vAngle", 0f) ?: 0f
                    val hAngleFloat = params?.getFloat("hAngle", 0f) ?: 0f
                    val mode = params?.getString("mode", "relative") ?: "relative"

                    val vAngle: Int = vAngleFloat.toInt()
                    val hAngle: Int = hAngleFloat.toInt() // 允许从Objective获取hAngle

                    Log.i(TAG, "执行 HEAD_MOVE: vAngle=$vAngle, hAngle=$hAngle, mode=$mode")
                    RobotApi.getInstance().moveHead(reqId, mode, mode, hAngle, vAngle, createHeadListener(action))
                }
                return true
            }
        }
        pageAgent.registerAction(moveHeadAction)

        // 2. 头部回正
        val resetHeadAction = Action(ACTION_PREFIX + "HEAD_RESET") // ★ 命名正确
        resetHeadAction.executor = object : ActionExecutor {
            override fun onExecute(action: Action, params: Bundle?): Boolean {
                coroutineScope.launch {
                    Log.i(TAG, "执行 HEAD_RESET")
                    RobotApi.getInstance().resetHead(reqId, createHeadListener(action))
                }
                return true
            }
        }
        pageAgent.registerAction(resetHeadAction)
    }

    // =================================================================
    // 模块二：基础移动动作 (崩溃的根源)
    // =================================================================
    private fun registerMotionActions() {
        // 3. 前进
        val goForwardAction = Action(ACTION_PREFIX + "MOTION_GO_FORWARD") // ★★★ 致命错误修正 ★★★
        goForwardAction.executor = createMotionExecutor { action, params ->
            val distance = params?.getFloat("distance", 0f) ?: 0f
            val avoid = params?.getBoolean("avoid", true) ?: true
            Log.i(TAG, "执行 MOTION_GO_FORWARD: distance=$distance, avoid=$avoid")
            RobotApi.getInstance().goForward(reqId, 0.2f, distance, avoid, createMotionListener(action))
        }
        pageAgent.registerAction(goForwardAction)

        // 4. 后退
        val goBackwardAction = Action(ACTION_PREFIX + "MOTION_GO_BACKWARD") // ★★★ 致命错误修正 ★★★
        goBackwardAction.executor = createMotionExecutor { action, params ->
            val distance = params?.getFloat("distance", 0f) ?: 0f
            Log.i(TAG, "执行 MOTION_GO_BACKWARD: distance=$distance")
            RobotApi.getInstance().goBackward(reqId, 0.2f, distance, createMotionListener(action))
        }
        pageAgent.registerAction(goBackwardAction)

        // 5. 左转
        val turnLeftAction = Action(ACTION_PREFIX + "MOTION_TURN_LEFT") // ★★★ 致命错误修正 ★★★
        turnLeftAction.executor = createMotionExecutor { action, params ->
            val angle = params?.getFloat("angle", 0f) ?: 0f
            Log.i(TAG, "执行 MOTION_TURN_LEFT: angle=$angle")
            RobotApi.getInstance().turnLeft(reqId, 20f, angle, createMotionListener(action))
        }
        pageAgent.registerAction(turnLeftAction)

        // 6. 右转
        val turnRightAction = Action(ACTION_PREFIX + "MOTION_TURN_RIGHT") // ★★★ 致命错误修正 ★★★
        turnRightAction.executor = createMotionExecutor { action, params ->
            val angle = params?.getFloat("angle", 0f) ?: 0f
            Log.i(TAG, "执行 MOTION_TURN_RIGHT: angle=$angle")
            RobotApi.getInstance().turnRight(reqId, 20f, angle, createMotionListener(action))
        }
        pageAgent.registerAction(turnRightAction)

        // 7. 停止移动
        val stopMoveAction = Action(ACTION_PREFIX + "MOTION_STOP_MOVE") // ★★★ 致命错误修正 ★★★
        stopMoveAction.executor = createMotionExecutor { action, _ ->
            Log.i(TAG, "执行 MOTION_STOP_MOVE")
            RobotApi.getInstance().stopMove(reqId, createMotionListener(action))
        }
        pageAgent.registerAction(stopMoveAction)
    }

    // =================================================================
    // 模块三：回调处理器 (区分对待)
    // =================================================================

    /**
     *  回调处理器1：用于处理 moveHead/resetHead 的复杂JSON回调
     */
    private fun createHeadListener(action: Action): CommandListener {
        return object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                try {
                    val status = JSONObject(message).getString("status")
                    val success = "ok".equals(status, ignoreCase = true)
                    val resultBundle = Bundle().apply { putString("resultMessage", message) }
                    action.notify(ActionResult(if (success) ActionStatus.SUCCEEDED else ActionStatus.FAILED, resultBundle))
                } catch (e: Exception) {
                    // ... 错误处理 ...
                    action.notify(ActionResult(ActionStatus.FAILED))
                }
            }
        }
    }

    /**
     *  回调处理器2：用于处理 goForward/turnLeft 等基础移动的简单 "succeed" 字符串回调
     */
    private fun createMotionListener(action: Action): CommandListener { // ★★★ 核心新增 ★★★
        return object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                val success = "succeed".equals(message, ignoreCase = true)
                Log.d(action.name, "基础移动回调: result=$result, message='$message', success=$success")
                action.notify(ActionResult(if (success) ActionStatus.SUCCEEDED else ActionStatus.FAILED))
            }
        }
    }

    /**
     *  辅助函数：为基础移动动作创建一个统一的执行器模板
     */
    private fun createMotionExecutor(block: (Action, Bundle?) -> Unit): ActionExecutor {
        return object : ActionExecutor {
            override fun onExecute(action: Action, params: Bundle?): Boolean {
                coroutineScope.launch { block(action, params) }
                return true
            }
        }
    }

    private fun bundleToString(bundle: Bundle?): String {
        // ... (内容无变化) ...
        if (bundle == null) return "null"
        if (bundle.isEmpty) return "empty"
        return bundle.keySet().joinToString(", ") { key -> "$key=${bundle.get(key)}" }
    }
}
// ▲▲▲ 【V12-全面战争修正版】代码 ▲▲▲
