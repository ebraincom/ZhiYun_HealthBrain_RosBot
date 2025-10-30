package com.zhiyun.agentrobot.event
sealed class RobotMotionEvent {

    /**
     * 对应 RobotAPI.md 的 "头部云台运动".
     * @param vAngle 上下运动的角度, 范围: 0 ~ 90.
     * @param hAngle 左右转动角度, 范围: -120 ~ 120 (在某些机型上无效).
     * @param mode 运动模式, "relative" 或 "absolute".
     */
    data class MoveHead(val vAngle: Int, val hAngle: Int = 0, val mode: String = "relative") : RobotMotionEvent()

    /**
     * 对应 RobotAPI.md 的 "恢复云台初始角度".
     */
    object ResetHead : RobotMotionEvent()

    /**
     * 对应 RobotAPI.md 的 "直线运动" (前进/后退).
     * 使用正负 speed 来区分前进和后退.
     * @param speed 运动速度 (m/s), 正数前进, 负数后退. 范围 0 ~ 1.0.
     * @param distance 运动距离 (m), 可选.
     */
    data class Go(val speed: Float, val distance: Float? = null) : RobotMotionEvent()

    /**
     * 对应 RobotAPI.md 的 "旋转运动" (左转/右转).
     * 使用正负 angle 来区分方向.
     * @param angle 旋转角度 (度), 正数右转, 负数左转.
     * @param speed 旋转速度 (度/s), 范围 0 ~ 50.
     */
    data class Turn(val angle: Float, val speed: Float) : RobotMotionEvent()

    /**
     * 对应 RobotAPI.md 的 "停止".
     * 用于停止前进、后退及旋转.
     */
    object StopMove : RobotMotionEvent()
}