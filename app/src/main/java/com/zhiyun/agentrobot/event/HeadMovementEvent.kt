// 文件路径: com/zhiyun/agentrobot/event/HeadMovementEvent.kt

package com.zhiyun.agentrobot.event

/**
 * 用于控制头部运动的EventBus事件。
 * 这是一个数据类，封装了所有可能的头部运动指令。
 */
data class HeadMovementEvent(val movement: HeadMovement)

/**
 * 定义一个枚举，清晰地表示所有支持的头部运动类型。
 */
enum class HeadMovement {
    UP,
    DOWN,
    RESET
}
