// 文件路径: com/zhiyun/agentrobot/utils/RequestIdGenerator.kt

package com.zhiyun.agentrobot.utils

import java.util.concurrent.atomic.AtomicInteger

/**
 * 为RobotAPI调用提供全局唯一的、线程安全的请求ID。
 */
object RequestIdGenerator {
    private val id = AtomicInteger(0)

    fun next(): Int = id.incrementAndGet()
}
