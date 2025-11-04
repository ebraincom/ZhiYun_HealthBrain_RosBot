package com.zhiyun.agentrobot.ui.planreminder // ✅ 1. 包名已更新

import java.util.UUID

/** * “计划提醒”事项的数据类，这是我们新页面的数据模型
 * @param content 提醒的具体内容，例如 "去医院复查"
 * @param details 对提醒的补充说明，例如 "带好病历和身份证"
 * @param reminderTimePoints 提醒的时间点描述，例如 "明天上午9点"
 * @param reminderStatus 提醒状态 ("待提醒", "已提醒")
 * @param id 唯一标识符
 * @param createdAt 创建时间的时间戳
 */
// ✅ 2. 类名和参数已更新为通用“计划提醒”场景
data class PlanReminderItem(
    val content: String,
    val details: String,
    val reminderTimePoints: String,
    val reminderStatus: String = "待提醒",
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis()
)


