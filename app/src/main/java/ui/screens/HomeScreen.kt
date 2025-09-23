package com.zhiyun.agentrobot.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
            import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiyun.agentrobot.R // 确保 R 文件被正确导入
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme // 确保主题被正确导入

// 模拟数据 - 后续可以从 ViewModel 获取
data class UserProfile(
    val name: String = "王阿姨", // 默认值，可以从实际数据源获取
    val avatarResId: Int = R.drawable.user_avatar // 对应您的 user_avatar.png
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userProfile: UserProfile = UserProfile(), // 传入用户信息
    onOneTouchSosClick: () -> Unit = {},
    onGuideClick: () -> Unit = {},
    onShoppingCartClick: () -> Unit = {},
    onMemoClick: () -> Unit = {},
    onStorageClick: () -> Unit = {},
    onMemoryShowcaseClick: () -> Unit = {},
    onReminderListClick: () -> Unit = {},
    onPlanReminderClick: () -> Unit = {},
    onMedicineReminderClick: () -> Unit = {},
    onTodayReminderClick: () -> Unit = {},
    onAiBrainClick: () -> Unit = {},
    onRepeatReminderClick: () -> Unit = {},
    onDoctorClick: () -> Unit = {},
) {
    // 主题颜色，方便后续统一调整 (可以从 MaterialTheme.colorScheme 获取更佳)
    val lightBlueBackground = Color(0xFFE3F2FD) // 示例浅蓝色背景，请根据您的UI图调整
    val cardBackgroundColor = Color.White

    Scaffold(
        topBar = {
            AppTopBar(
                userProfile = userProfile,
                onOneTouchSosClick = onOneTouchSosClick,
                onGuideClick = onGuideClick
            )
        },
        bottomBar = {
            AppBottomBar(
                robotMessage = "你可以对我说，小智帮我做点什么...",
                time = "15:36", // 后续应动态获取
                weatherInfo = "晴转多云 25-32度" // 后续应动态获取
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(lightBlueBackground) // 设置主内容区的背景色
                .padding(horizontal = 24.dp, vertical = 16.dp), // 主内容区的外边距
            horizontalArrangement = Arrangement.spacedBy(24.dp) // 左右两大块之间的间距
        ) {
            // 左侧 "智芸记录" 区域
            ZhiyunRecordSection(
                modifier = Modifier
                    .weight(1f) // 占据可用空间的一半
                    .fillMaxHeight(),
                backgroundColor = cardBackgroundColor,
                onShoppingCartClick = onShoppingCartClick,
                onMemoClick = onMemoClick,
                onStorageClick = onStorageClick,
                onMemoryShowcaseClick = onMemoryShowcaseClick
            )

            // 右侧 "智芸助手" 区域
            ZhiyunAssistantSection(
                modifier = Modifier
                    .weight(1f) // 占据可用空间的一半
                    .fillMaxHeight(),
                backgroundColor = cardBackgroundColor,
                onReminderListClick = onReminderListClick,
                onPlanReminderClick = onPlanReminderClick,
                onMedicineReminderClick = onMedicineReminderClick,
                onTodayReminderClick = onTodayReminderClick,
                onAiBrainClick = onAiBrainClick,
                onRepeatReminderClick = onRepeatReminderClick,
                onDoctorClick = onDoctorClick
            )
        }
    }
}

@Composable
fun AppTopBar(
    userProfile: UserProfile,
    onOneTouchSosClick: () -> Unit,
    onGuideClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface( // 使用 Surface 可以方便设置背景色和阴影等
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp), // 根据您的UI图调整高度
        color = MaterialTheme.colorScheme.surfaceVariant, // 顶部栏背景色，请根据UI调整
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧用户区域
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = userProfile.avatarResId),
                    contentDescription = stringResource(R.string.user_avatar_content_description),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                // 状态指示灯 (一个简单的圆形)
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Green, CircleShape) // 假设在线是绿色
                )
                Spacer(Modifier.width(16.dp))
                Button( // 一键呼叫按钮
                    onClick = onOneTouchSosClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726)), // 橙色，根据UI调整
                    shape = RoundedCornerShape(50) // 圆角按钮
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.one_touch_sos),
                        contentDescription = stringResource(R.string.one_touch_sos_button),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.one_touch_sos_button))
                }
            }

            // 右侧导览按钮
            Button(
                onClick = onGuideClick,
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_guide_scan),
                    contentDescription = stringResource(R.string.guide_button),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.guide_button))
            }
        }
    }
}

@Composable
fun AppBottomBar(
    robotMessage: String,
    time: String,
    weatherInfo: String,
    modifier: Modifier = Modifier
) {
    val bottomBarHeight = 80.dp // 底部栏高度
    val robotImageSize = 90.dp // 机器人图片大小，比底部栏略高
    val robotOverlap = (robotImageSize - bottomBarHeight) / 1.5f // 机器人超出底部栏的高度，调整这个值以获得想要的浮动效果

    Box(modifier = modifier.fillMaxWidth()) { // 使用 Box 来叠加机器人
        Surface( // 底部栏主体
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomBarHeight),
            color = MaterialTheme.colorScheme.surfaceVariant, // 底部栏背景色
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 机器人占位空间 (实际机器人在 Box 的上一层)
                Spacer(Modifier.width(robotImageSize + 8.dp)) // 预留机器人和一些间距的空间

                Text(
                    text = robotMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(time, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_weather_cloud), // 天气图标
                        contentDescription = stringResource(R.string.weather_icon_content_description),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(weatherInfo, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // 小机器人图片，利用 offset 实现浮动效果
        Image(
            painter = painterResource(id = R.drawable.ic_robot_zhiyun),
            contentDescription = stringResource(R.string.zhiyun_robot_content_description),
            modifier = Modifier
                .size(robotImageSize)
                .align(Alignment.BottomStart) // 对齐到 Box 的左下角
                .offset(x = 16.dp, y = -robotOverlap) //向上和向右偏移
        )
    }
}


@Composable
fun ZhiyunRecordSection(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onShoppingCartClick: () -> Unit,
    onMemoClick: () -> Unit,
    onStorageClick: () -> Unit,
    onMemoryShowcaseClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp), // 卡片圆角
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.zhiyun_record_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 功能按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround // 或 SpaceBetween
            ) {
                FeatureButton(
                    text = stringResource(R.string.shopping_list_button),
                    iconResId = R.drawable.ic_shopping_cart_placeholder,
                    onClick = onShoppingCartClick,
                    modifier = Modifier.weight(1f),
                    buttonColor = Color(0xFFFFB74D) // 橙色示例，请根据UI调整
                )
                Spacer(Modifier.width(8.dp))
                FeatureButton(
                    text = stringResource(R.string.daily_memo_button),
                    iconResId = R.drawable.ic_memo_placeholder,
                    onClick = onMemoClick,
                    modifier = Modifier.weight(1f),
                    buttonColor = Color(0xFF64B5F6) // 蓝色示例
                )
                Spacer(Modifier.width(8.dp))
                FeatureButton(
                    text = stringResource(R.string.item_storage_button),
                    iconResId = R.drawable.ic_storage_placeholder,
                    onClick = onStorageClick,
                    modifier = Modifier.weight(1f),
                    buttonColor = Color(0xFF81C784) // 绿色示例
                )
            }

            Spacer(Modifier.height(16.dp))

            // 智芸回忆录区域
            Card( // 可以用一个内部Card来做展示，或者直接Image+Text
                onClick = onMemoryShowcaseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // 占据剩余空间
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) { // 使用 Box 来叠加文字（如果需要）
                    Image(
                        painter = painterResource(id = R.drawable.img_memory_showcase),
                        contentDescription = stringResource(R.string.memory_showcase_content_description),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // 或 Fit，根据图片和容器调整
                    )
                    // 可以在这里用Column叠加文字描述，如 "照片+动态生成文字"
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(Color.Black.copy(alpha = 0.5f)) // 半透明背景
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            "智芸回忆录",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "无需动手，只需畅言，AI自动为您生成图文并茂的美好回忆", // 来自UI图
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZhiyunAssistantSection(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onReminderListClick: () -> Unit,
    onPlanReminderClick: () -> Unit,
    onMedicineReminderClick: () -> Unit,
    onTodayReminderClick: () -> Unit,
    onAiBrainClick: () -> Unit,
    onRepeatReminderClick: () -> Unit,
    onDoctorClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.zhiyun_assistant_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // 左侧提醒列表大图
                Card(
                    onClick = onReminderListClick,
                    modifier = Modifier
                        .weight(1f) // 占据一部分空间
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_reminder_list_showcase),
                            contentDescription = stringResource(R.string.reminder_list_showcase_content_description),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(8.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                "提醒列表",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "智芸提醒，不错过每个重要时刻", // 来自UI图，可修改
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 右侧六个功能按钮 (每列三个)
                Column(
                    modifier = Modifier
                        .weight(0.8f) // 占据相对较小空间，可调整
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceAround // 或 SpaceEvenly
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                        FeatureButton(
                            text = stringResource(R.string.plan_reminder_button),
                            iconResId = R.drawable.ic_plan_placeholder,
                            onClick = onPlanReminderClick,
                            modifier = Modifier.weight(1f),
                            buttonColor = Color(0xFF90CAF9) // 浅蓝色
                        )
                        FeatureButton(
                            text = stringResource(R.string.medicine_reminder_button),
                            iconResId = R.drawable.ic_medicine_placeholder,
                            onClick = onMedicineReminderClick,
                            modifier = Modifier.weight(1f),
                            buttonColor = Color(0xFFFFE082) // 浅黄色
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                        FeatureButton(
                            text = stringResource(R.string.today_reminder_button),
                            iconResId = R.drawable.ic_today_reminder_placeholder,
                            onClick = onTodayReminderClick,
                            modifier = Modifier.weight(1f),
                            buttonColor = Color(0xFF90CAF9)
                        )
                        FeatureButton(
                            text = stringResource(R.string.ai_brain_button),
                            iconResId = R.drawable.ic_ai_brain_placeholder,
                            onClick = onAiBrainClick,
                            modifier = Modifier.weight(1f),
                            buttonColor = Color(0xFFFFE082)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                        FeatureButton(
                            text = stringResource(R.string.repeat_reminder_button),
                            iconResId = R.drawable.ic_repeat_placeholder,
                            onClick = onRepeatReminderClick,
                            modifier = Modifier.weight(1f),
                            buttonColor = Color(0xFF90CAF9)
                        )
                        FeatureButton(
                            text = stringResource(R.string.doctor_button),
                            iconResId = R.drawable.ic_doctor_placeholder,
                            onClick = onDoctorClick,
                            modifier = Modifier.weight(1f),
                            buttonColor = Color(0xFFFFE082)
                        )
                    }
                }
            }
        }
    }
}

// 通用功能按钮 Composable
@Composable
fun FeatureButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconSize: Int = 32 // dp
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(IntrinsicSize.Min), // 让按钮高度适应内容
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = contentColor),
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp) // 调整内边距
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = text, // 使用按钮文字作为辅助描述
                modifier = Modifier.size(iconSize.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(text = text, fontSize = 12.sp, maxLines = 1) // 调整字体大小
        }
    }
}


// --- Previews ---
@Preview(showBackground = true, name = "HomeScreen Landscape Preview", widthDp = 1920, heightDp = 1080)
@Composable
fun HomeScreenLandscapePreview() {
    ZhiyunAgentRobotTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, name = "AppTopBar Preview")
@Composable
fun AppTopBarPreview() {
    ZhiyunAgentRobotTheme {
        AppTopBar(userProfile = UserProfile(), onOneTouchSosClick = {}, onGuideClick = {})
    }
}

@Preview(showBackground = true, name = "AppBottomBar Preview")
@Composable
fun AppBottomBarPreview() {
    ZhiyunAgentRobotTheme {
        AppBottomBar(robotMessage = "你好，我是小智！", time = "10:00", weatherInfo = "晴 28°C")
    }
}

@Preview(showBackground = true, name = "ZhiyunRecordSection Preview", widthDp = 900, heightDp = 1000)
@Composable
fun ZhiyunRecordSectionPreview() {
    ZhiyunAgentRobotTheme {
        Box(modifier= Modifier
            .padding(16.dp)
            .background(Color.LightGray)) {
            ZhiyunRecordSection(
                modifier = Modifier.fillMaxWidth(0.5f), // 模拟占据一半宽度
                backgroundColor = Color.White,
                onShoppingCartClick = {},
                onMemoClick = {},
                onStorageClick = {},
                onMemoryShowcaseClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "ZhiyunAssistantSection Preview", widthDp = 900, heightDp = 1000)
@Composable
fun ZhiyunAssistantSectionPreview() {
    ZhiyunAgentRobotTheme {
        Box(modifier= Modifier
            .padding(16.dp)
            .background(Color.LightGray)) {
            ZhiyunAssistantSection(
                modifier = Modifier.fillMaxWidth(0.5f), // 模拟占据一半宽度
                backgroundColor = Color.White,
                onReminderListClick = {},
                onPlanReminderClick = {},
                onMedicineReminderClick = {},
                onTodayReminderClick = {},
                onAiBrainClick = {},
                onRepeatReminderClick = {},
                onDoctorClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "FeatureButton Preview")
@Composable
fun FeatureButtonPreview() {
    ZhiyunAgentRobotTheme {
        FeatureButton(
            text = "示例按钮",
            iconResId = R.drawable.ic_shopping_cart_placeholder, // 使用一个存在的图标
            onClick = {}
        )
    }
}
