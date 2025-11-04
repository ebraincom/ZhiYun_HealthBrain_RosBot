// ✅✅✅【【【【 这是 V1.0 当天提醒 Screen：遵从最高指示，100%精确复刻最终版！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.todayreminder // ✅ 1. 包名已更新

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
import com.zhiyun.agentrobot.ui.planreminder.PlanReminderItem

/**
 * 当天提醒页面的主屏幕 Composable
 */
// ✅ 2. Composable函数名已更新
@Composable
fun TodayReminderScreen(
    userProfile: UserProfile,
    reminders: List<TodayReminderItem>, // ✅ 3. reminders列表的数据类型已更新
    onBack: () -> Unit,
    onTodayReminderClick: () -> Unit // ✅ 4. 回调函数名已更新
) {
    AppScaffold(
        userProfile = userProfile,
        onGuideClick = onBack, // 返回按钮复用onGuideClick
        content = {
            // ✅ 5. 调用新的Content Composable
            TodayReminderContent(
                reminders = reminders,
                onTodayReminderClick = onTodayReminderClick
            )
        }
    )
}

/**
 * 核心内容区
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodayReminderContent( // ✅ 6. Composable函数名已更新
    reminders: List<TodayReminderItem>, // ✅ 7. 参数类型已更新
    onTodayReminderClick: () -> Unit // ✅ 8. 参数名已更新
) {
    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 167.dp, start = 74.dp, end = 74.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ✅ 9. 调用新的交互卡片
        InteractionCard(onTodayReminderClick = onTodayReminderClick)

        // 动态的提醒内容卡片
        reminders.forEach { reminderItem ->
            ReminderCard(item = reminderItem) // ✅ 10. 参数类型自动匹配 TodayReminderItem
        }
    }
}

/**
 * 黄色的交互卡片
 */
@Composable
private fun InteractionCard(onTodayReminderClick: () -> Unit) { // ✅ 11. 参数名已更新
    Card(
        modifier = Modifier
            .width(566.dp)
            .height(324.dp),
        shape = RoundedCornerShape(16.dp),
        // ✅ 12. 颜色暂用计划提醒的淡蓝色，您可以替换为当天提醒的主题色
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "新增当天提醒", fontSize = 28.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold) // ✅ 13. 文本已更新
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onTodayReminderClick,
                modifier = Modifier
                    .width(175.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                // ✅ 14. 颜色暂用计划提醒的主题蓝，您可以替换
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
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
 * 白色的提醒内容卡片
 */
@Composable
private fun ReminderCard(item: TodayReminderItem) { // ✅ 15. 参数类型已更新
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
                    // ✅ 16. 【业务适配】数据字段已更新为当天提醒的 content, details
                    ReminderInfoLine("提醒事项：", item.content)
                    Spacer(Modifier.height(8.dp))
                    if(item.details.isNotBlank()) { // 只有在有补充说明时才显示
                        ReminderInfoLine("补充说明：", item.details)
                        Spacer(Modifier.height(8.dp))
                    }
                }
                // ✅ 17. 图标已更新为当天提醒的图标
                Image(
                    painter = painterResource(id = R.drawable.ic_today_reminder_placeholder),
                    contentDescription = "当天提醒图标",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(start = 16.dp)
                )
            }

            // 卡片底部：操作按钮
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { /*TODO*/ }) { Text("修改提醒", color = Color(0xFF4A90E2), fontSize = 18.sp) } // ✅ 18. 文本已更新
                TextButton(onClick = { /*TODO*/ }) { Text("删除提醒", color = Color.Red, fontSize = 18.sp) }
            }
        }
    }
}

// Info Line 组件无需修改，保持复用
@Composable
private fun ReminderInfoLine(label: String, value: String) {
    Row {
        Text(text = label, fontSize = 20.sp, color = Color.Gray)
        Text(text = value, fontSize = 20.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun TodayReminderScreenPreview() { // ✅ 19. Preview函数名已更新
    val sampleReminders = listOf(
        TodayReminderItem(
            creationTime = "2025/11/05 10:00 创建",
            content = "下午三点开会",
            details = "准备好上周的报告",
            reminderStatus = "待提醒"
        ),
        TodayReminderItem(
            creationTime = "2025/11/05 09:00 创建",
            content = "取快递",
            details = "在3号楼下丰巢柜",
            reminderStatus = "已提醒"
        )
    )

    ZhiyunAgentRobotTheme {
        // ✅ 20. 调用新的Screen Composable
        TodayReminderScreen(
            userProfile = UserProfile(name = "总司令", avatarUrl = null),
            reminders = sampleReminders,
            onBack = { },
            onTodayReminderClick = { }
        )
    }
}
