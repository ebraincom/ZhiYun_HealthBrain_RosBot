// ✅✅✅【【【【 这是 V1.5 计划提醒 Screen：遵从最高指示，拨乱反正版！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.planreminder // ✅ 1. 包名已更新

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiyun.agentrobot.R
import com.zhiyun.agentrobot.data.UserProfile
import com.zhiyun.agentrobot.ui.common.AppScaffold
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * 计划提醒页面的主屏幕 Composable
 */
// ✅ 2. Composable函数名已更新
@Composable
fun PlanReminderScreen(
    userProfile: UserProfile,
    reminders: List<PlanReminderItem>, // ✅ 3. reminders列表的数据类型已更新
    onBack: () -> Unit,
    onPlanReminderClick: () -> Unit // ✅ 4. 回调函数名已更新
) {
    AppScaffold(
        userProfile = userProfile,
        onGuideClick = onBack,
        content = {
            // ✅ 5. 调用新的Content Composable
            PlanReminderContent(
                reminders = reminders,
                onPlanReminderClick = onPlanReminderClick
            )
        }
    )
}

/**
 * 核心内容区
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanReminderContent( // ✅ 6. Composable函数名已更新
    reminders: List<PlanReminderItem>, // ✅ 7. 参数类型已更新
    onPlanReminderClick: () -> Unit // ✅ 8. 参数名已更新
) {
    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 167.dp, start = 74.dp, end = 74.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ✅ 9. 调用新的交互卡片
        InteractionCard(onPlanReminderClick = onPlanReminderClick)

        // 动态的提醒内容卡片
        reminders.forEach { reminderItem ->
            ReminderCard(item = reminderItem) // ✅ 10. 参数类型自动匹配 PlanReminderItem
        }
    }
}

/**
 * 黄色的交互卡片
 */
@Composable
private fun InteractionCard(onPlanReminderClick: () -> Unit) { // ✅ 11. 参数名已更新
    Card(
        modifier = Modifier
            .width(566.dp)
            .height(324.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F7FF)), // ✅ 12. 颜色更新为计划提醒的淡蓝色
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "新增计划提醒", fontSize = 28.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold) // ✅ 13. 文本已更新
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onPlanReminderClick,
                modifier = Modifier
                    .width(175.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7AC8F9)), // ✅ 14. 颜色更新为计划提醒的主题蓝
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_voice_mic),
                    contentDescription = "开始语音提醒",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

/**
 * 白色的提醒内容卡片 V1.5
 */
@Composable
private fun ReminderCard(item: PlanReminderItem) { // ✅ 15. 参数类型已更新
    Card(
        modifier = Modifier
            .width(566.dp)
            .height(324.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)) {
            // 卡片顶部：创建时间和状态
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.creationTime, fontSize = 18.sp, color = Color.Gray)
                Text(
                    text = item.reminderStatus,
                    fontSize = 18.sp,
                    color = if (item.reminderStatus == "待提醒") Color(0xFFF5A623) else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            // 卡片中部：提醒内容和图标
            Row(Modifier
                .fillMaxWidth()
                .weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    // ✅ 16. 数据字段已更新为通用的 content, details
                    ReminderInfoLine("提醒事项：", item.content)
                    Spacer(Modifier.height(8.dp))
                    if(item.details.isNotBlank()) { // 只有在有补充说明时才显示
                        ReminderInfoLine("补充说明：", item.details)
                        Spacer(Modifier.height(8.dp))
                    }
                    ReminderInfoLine("提醒时间：", item.reminderTimePoints)
                    Spacer(Modifier.height(8.dp))
                    if (!item.stopCondition.isNullOrBlank()) {
                        ReminderInfoLine("停止条件：", item.stopCondition)
                        // 如果您希望在它后面也加一个间距，可以取消这行注释
                        Spacer(Modifier.height(8.dp))
                    }
                }
                // ✅ 17. 图标已更新为通用的计划提醒图标
                Image(
                    painter = painterResource(id = R.drawable.ic_plan_placeholder),
                    contentDescription = "计划图标",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(start = 16.dp)
                )
            }

            // 卡片底部：操作按钮
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { /*TODO*/ }) { Text("修改计划提醒", color = Color(0xFF4A90E2), fontSize = 18.sp) } // ✅ 18. 文本已更新
                TextButton(onClick = { /*TODO*/ }) { Text("删除提醒", color = Color.Red, fontSize = 18.sp) } // ✅ 19. 文本已更新
            }
        }
    }
}

// Info Line 组件无需修改
@Composable
private fun ReminderInfoLine(label: String, value: String) {
    Row {
        Text(text = label, fontSize = 20.sp, color = Color.Gray)
        Text(text = value, fontSize = 20.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
    }
}


@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun PlanReminderScreenPreview() { // ✅ 20. Preview函数名已更新
    // 使用 V1.5 结构创建假数据
    val sampleReminders = listOf(
        PlanReminderItem(
            creationTime = "2025/11/04 10:00 创建",
            content = "周六去医院复查",
            details = "带好病历记录与身份证",
            reminderTimePoints = "2025/11/08 当天提醒",
            stopCondition = null,
            reminderStatus = "特提醒"
        ),
        PlanReminderItem(
            creationTime = "2025/11/03 09:00 创建",
            content = "5点出门去接孙女",
            details = "",
            reminderTimePoints = "每日提醒",
            stopCondition = "长期",
            reminderStatus = "今日已提醒"
        ),
        PlanReminderItem(
            creationTime = "2025/11/02 13:00 创建",
            content = "检查燃气是否关闭",
            details = "",
            reminderTimePoints = "睡前提醒",
            stopCondition = null,
            reminderStatus = "待提醒"
        )
    )

    ZhiyunAgentRobotTheme {
        // ✅ 21. 调用新的Screen Composable
        PlanReminderScreen(
            userProfile = UserProfile(name = "总司令", avatarUrl = null),
            reminders = sampleReminders,
            onBack = { },
            onPlanReminderClick = { }
        )
    }
}
