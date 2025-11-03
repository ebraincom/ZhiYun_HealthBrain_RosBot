// ✅【【【这是完整的 MedicineReminderScreen.kt 文件！！！】】】
package com.zhiyun.agentrobot.ui.medicinereminder // ✅ 确保包名正确！

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
 * 服药管理页面的主屏幕 Composable
 */
@Composable
fun MedicineReminderScreen(
    userProfile: UserProfile,
    reminders: List<MedicineReminderItem>, // 接收从ViewModel来的数据
    onBack: () -> Unit,
    onMedicineReminderClick: () -> Unit // 交互卡片按钮的点击事件
) {
    AppScaffold(
        userProfile = userProfile,
        onGuideClick = onBack, // 复用“导览界面”按钮作为返回
        content = {
            MedicineReminderContent(
                reminders = reminders,
                onMedicineReminderClick = onMedicineReminderClick
            )
        }
    )
}

/**
 * 核心内容区：使用FlowRow排列卡片
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicineReminderContent(
    reminders: List<MedicineReminderItem>,
    onMedicineReminderClick: () -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 167.dp, start = 74.dp, end = 74.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. 固定的交互卡片
        InteractionCard(onMedicineReminderClick = onMedicineReminderClick)

        // 2. 动态的提醒内容卡片
        reminders.forEach { reminderItem ->
            ReminderCard(item = reminderItem)
        }
    }
}

/**
 * 黄色的交互卡片
 */
@Composable
private fun InteractionCard(onMedicineReminderClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(566.dp)
            .height(324.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)), // 设计图的淡黄色
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "新增用药事项", fontSize = 28.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            // 语音点击按钮
            Button(
                onClick = onMedicineReminderClick,
                modifier = Modifier
                    .width(175.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9E07A)), // 设计图的深黄色
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
private fun ReminderCard(item: MedicineReminderItem) {
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
                    text = item.status,
                    fontSize = 18.sp,
                    color = if (item.status == "待提醒") Color(0xFFF5A623) else Color.Gray, // 状态颜色区分
                    fontWeight = FontWeight.Bold
                )
            }

            // 卡片中部：提醒内容和图标
            Row(Modifier
                .fillMaxWidth()
                .weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    ReminderInfoLine("药物：", item.drugName)
                    Spacer(Modifier.height(8.dp))
                    ReminderInfoLine("服用说明：", item.dosageInstruction)
                    Spacer(Modifier.height(8.dp))
                    ReminderInfoLine("提醒次数：", item.reminderTimes)
                    Spacer(Modifier.height(8.dp))
                    ReminderInfoLine("服药停止日期：", item.stopDate)
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_medicine_placeholder),
                    contentDescription = "药物图标",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(start = 16.dp)
                )
            }

            // 卡片底部：操作按钮
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { /*TODO*/ }) { Text("修改用药事项", color = Color(0xFF4A90E2), fontSize = 18.sp) }
                TextButton(onClick = { /*TODO*/ }) { Text("删除用药", color = Color.Red, fontSize = 18.sp) }
            }
        }
    }
}

@Composable
private fun ReminderInfoLine(label: String, value: String) {
    Row {
        Text(text = label, fontSize = 20.sp, color = Color.Gray)
        Text(text = value, fontSize = 20.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
    }
}
// 2. 将此@Preview代码块粘贴到文件的最底部
@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun MedicineReminderScreenPreview() {
    // 3. 创建一个临时的、用于预览的假数据列表
    val sampleReminders = listOf(
        MedicineReminderItem(
            drugName = "阿莫西林 (消炎)",
            dosageInstruction = "每次服用1粒",
            reminderTimes = "每日12:00/19:00提醒",
            stopDate = "2025/6/30",
            status = "今日已提醒",
            creationTime = "2025/7/29 13:00 创建"
        ),
        MedicineReminderItem(
            drugName = "胃康安",
            dosageInstruction = "每次服用5粒",
            reminderTimes = "每日7:00/12:00/19:00提醒",
            stopDate = "2025/8/30",
            status = "待提醒",
            creationTime = "2025/7/29 18:00 创建"
        ),
        MedicineReminderItem(
            drugName = "拜阿司匹林",
            dosageInstruction = "每日1片",
            reminderTimes = "每日睡前提醒",
            stopDate = "长期服用",
            status = "待提醒",
            creationTime = "2025/7/28 09:00 创建"
        )
    )

    // 4. 在你的主题(Theme)中调用主屏幕Composable
    ZhiyunAgentRobotTheme {
        MedicineReminderScreen(
            userProfile = UserProfile(name = "王阿姨", avatarUrl = null),
            reminders = sampleReminders, // 将假数据传递给UI
            onBack = { }, // 预览中为空实现
            onMedicineReminderClick = { } // 预览中为空实现
        )
    }
}

