// ✅✅✅【【【【 这是 V2.0 升级行动的第三站：Screen 终极改造版！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.medicinereminder

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
 * 服药管理页面的主屏幕 Composable (此部分无需修改)
 */
@Composable
fun MedicineReminderScreen(
    userProfile: UserProfile,
    reminders: List<MedicineReminderItem>, // ✅ 自动接收V2.0版的数据列表
    onBack: () -> Unit,
    onMedicineReminderClick: () -> Unit
) {
    AppScaffold(
        userProfile = userProfile,
        onGuideClick = onBack,
        content = {
            MedicineReminderContent(
                reminders = reminders,
                onMedicineReminderClick = onMedicineReminderClick
            )
        }
    )
}

/**
 * 核心内容区 (此部分无需修改)
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
        // 固定的交互卡片 (无需修改)
        InteractionCard(onMedicineReminderClick = onMedicineReminderClick)

        // 动态的提醒内容卡片 (将使用V2.0版的新卡片)
        reminders.forEach { reminderItem ->
            ReminderCard(item = reminderItem)
        }
    }
}

/**
 * 黄色的交互卡片 (此部分无需修改)
 */
@Composable
private fun InteractionCard(onMedicineReminderClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(566.dp)
            .height(324.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "新增用药事项", fontSize = 28.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onMedicineReminderClick,
                modifier = Modifier
                    .width(175.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9E07A)),
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


// ✅✅✅ 【【【【 关键重构点 1：ReminderCard 已升级到 V2.0 ！！！ 】】】】 ✅✅✅
/**
 * 白色的提醒内容卡片 V2.0
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
            // 卡片顶部：创建时间和状态 (使用V2.0字段)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.creationTime, fontSize = 18.sp, color = Color.Gray) // ✅ V2.0: creationTime
                Text(
                    text = item.reminderStatus, // ✅ V2.0: reminderStatus
                    fontSize = 18.sp,
                    color = if (item.reminderStatus == "待提醒") Color(0xFFF5A623) else Color.Gray, // ✅ V2.0: reminderStatus
                    fontWeight = FontWeight.Bold
                )
            }

            // 卡片中部：提醒内容和图标 (使用V2.0字段)
            Row(Modifier
                .fillMaxWidth()
                .weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    ReminderInfoLine("药物：", item.drugName) // (无变化)
                    Spacer(Modifier.height(8.dp))
                    ReminderInfoLine("服用说明：", item.dosageInstruction) // ✅ V2.0: dosageInstruction
                    Spacer(Modifier.height(8.dp))
                    // ✅ V2.0: 将旧的 reminderTimes 拆分为两个独立的行
                    if (item.reminderFrequency != null) { // 只有在频率不为空时才显示
                        ReminderInfoLine("提醒频率：", item.reminderFrequency)
                        Spacer(Modifier.height(8.dp))
                    }
                    ReminderInfoLine("提醒时间：", item.reminderTimePoints) // ✅ V2.0: reminderTimePoints
                    Spacer(Modifier.height(8.dp))
                    ReminderInfoLine("服药停止：", item.stopCondition ?: "未指定") // ✅ V2.0: stopCondition
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_medicine_placeholder),
                    contentDescription = "药物图标",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(start = 16.dp)
                )
            }

            // 卡片底部：操作按钮 (无需修改)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { /*TODO*/ }) { Text("修改用药事项", color = Color(0xFF4A90E2), fontSize = 18.sp) }
                TextButton(onClick = { /*TODO*/ }) { Text("删除用药", color = Color.Red, fontSize = 18.sp) }
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

// ✅✅✅ 【【【【 关键重构点 2：Preview 已升级到 V2.0 ！！！ 】】】】 ✅✅✅
@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun MedicineReminderScreenPreview() {
    // 使用 V2.0 结构创建假数据
    val sampleReminders = listOf(
        MedicineReminderItem(
            creationTime = "2025/11/03 18:30 创建",
            drugName = "阿莫西林 (消炎)",
            dosageInstruction = "每次服用1粒",
            reminderFrequency = "每日", // ✅ V2.0: 新增
            reminderTimePoints = "12:00, 19:00", // ✅ V2.0: 修正
            stopCondition = "2025/12/30", // ✅ V2.0: 修正
            reminderStatus = "今日已提醒" // ✅ V2.0: 修正
        ),
        MedicineReminderItem(
            creationTime = "2025/11/03 17:50 创建",
            drugName = "胃康安",
            dosageInstruction = "每次服用5粒",
            reminderFrequency = "每日", // ✅ V2.0: 新增
            reminderTimePoints = "7:00, 12:00, 19:00", // ✅ V2.0: 修正
            stopCondition = "长期服用", // ✅ V2.0: 修正
            reminderStatus = "待提醒" // ✅ V2.0: 修正
        ),
        MedicineReminderItem(
            creationTime = "2025/11/03 09:00 创建",
            drugName = "拜阿司匹林",
            dosageInstruction = "每日1片",
            reminderFrequency = null, // ✅ V2.0: 可以为空
            reminderTimePoints = "睡前", // ✅ V2.0: 修正
            stopCondition = "长期服用", // ✅ V2.0: 修正
            reminderStatus = "待提醒" // ✅ V2.0: 修正
        )
    )

    ZhiyunAgentRobotTheme {
        MedicineReminderScreen(
            userProfile = UserProfile(name = "王阿姨", avatarUrl = null),
            reminders = sampleReminders,
            onBack = { },
            onMedicineReminderClick = { }
        )
    }
}
