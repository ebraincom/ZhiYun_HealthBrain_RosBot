package com.zhiyun.agentrobot.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.zhiyun.agentrobot.R
import androidx.compose.material3.Surface
import com.zhiyun.agentrobot.data.UserProfile // 假设有一个通用的UserProfile数据类


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
   // title: String,
    // showBackButton: Boolean = false,
    // actions: @Composable RowScope.() -> Unit = {},
    // --- 【新增或替换的代码】 开始 ---
    userProfile: UserProfile,
    onUserProfileClick: () -> Unit = {},
    onOneTouchSosClick: () -> Unit = {},
    onMoreConsultClick: () -> Unit = {},
    onGuideClick: () -> Unit = {},
    // --- 【新增或替换的代码】 结束 ---
    robotMessage: String = "你可以对我说，小智帮我做点什么...",
    time: String = "10:30", // 默认值
    weatherInfo: String = "晴 25°", // 默认值
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFFF0F4FF), // 全局统一背景色
        topBar = {
            // 修改】调用由您设计的AppTopBar，并传入所有参数
            AppTopBar(
                userProfile = userProfile,
                onUserProfileClick = onUserProfileClick,
                onOneTouchSosClick = onOneTouchSosClick,
                onMoreConsultClick = onMoreConsultClick,
                onGuideClick = onGuideClick
            )
        },
        bottomBar = {
            // 【核心连接】：将数据传递给精装修好的AppBottomBar
            AppBottomBar(
                robotMessage = robotMessage,
                time = time,
                weatherInfo = weatherInfo
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

// 【核心修正 2/2】: 修正AppTopBar，使其能根据title显示不同样式
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    userProfile: UserProfile,
    onUserProfileClick: () -> Unit,
    onOneTouchSosClick: () -> Unit,
    onMoreConsultClick: () -> Unit,
    onGuideClick: () -> Unit
) {
    val buttonImageHeight = 63.dp
    val userProfileButtonHeight = buttonImageHeight
    val imageButtonWidth = 175.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(113.dp), // 保持TopBar整体高度
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // == 左侧分组 (用户头像 + “一键呼叫”图片按钮) ==
            Row(verticalAlignment = Alignment.CenterVertically) {
                // -- 用户信息按钮 --
                Row(
                    modifier = Modifier
                        .height(userProfileButtonHeight)
                        .clip(RoundedCornerShape(userProfileButtonHeight / 2))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFE3EEFF), Color(0xFFC9D8FF))
                            )
                        )
                        .clickable { onUserProfileClick() }
                        .padding(start = 8.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.user_avatar),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(userProfileButtonHeight - 16.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = userProfile.name,
                        color = Color.DarkGray, // 新代码
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Info",
                        tint =Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(16.dp)) // 用户按钮和SOS按钮之间的间距

                // -- “一键呼叫”图片按钮 --
                Image(
                    painter = painterResource(id = R.drawable.one_touch_sos),
                    contentDescription = "一键呼叫",
                    modifier = Modifier
                        .width(imageButtonWidth)
                        .height(buttonImageHeight)
                        .clickable { onOneTouchSosClick() },
                    contentScale = ContentScale.FillBounds
                )
            } // 左侧区域结束

            // == 右侧分组 (“更多咨询” + 导览界面) ==
            Row(verticalAlignment = Alignment.CenterVertically) {
                // -- “更多咨询” 图标按钮 --
                Image(
                    painter = painterResource(id = R.drawable.ic_for_more_inquiries),
                    contentDescription = "更多咨询",
                    modifier = Modifier
                        .width(imageButtonWidth)
                        .height(buttonImageHeight)
                        .clickable { onMoreConsultClick() },
                    contentScale = ContentScale.FillBounds
                )

                Spacer(Modifier.width(16.dp))

                // -- “导览界面”图片按钮 --
                Image(
                    painter = painterResource(id = R.drawable.ic_guide_scan),
                    contentDescription = "导览界面",
                    modifier = Modifier
                        .width(imageButtonWidth)
                        .height(buttonImageHeight)
                        .clickable { onGuideClick() },
                    contentScale = ContentScale.FillBounds
                )
            } // 右侧区域结束
        }
    }
}


// 【核心修正 1/2】:修正AppBottomBar，让机器人“露头”
@Composable
private fun AppBottomBar(
    robotMessage: String,
    time: String,
    weatherInfo: String
) {
    // 尺寸常量，确保统一
    val ROBOT_ASSISTANT_WIDTH = 89.dp
    val ROBOT_ASSISTANT_HEIGHT = 161.dp
    val BOTTOM_BAR_HEIGHT = 137.dp


    // 使用Box作为根布局，它允许子组件堆叠，这是实现悬浮效果的关键
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Box的高度应该由内部最高的元素决定，我们让背景Row有137.dp，机器人有161.dp，Box会自动适应161.dp
            .height(BOTTOM_BAR_HEIGHT)
    ) {
        // 1. 底部栏的蓝色渐变背景和右侧内容
        // 这个Row在Z轴上位于底层
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(137.dp) // 背景高度为137.dp
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF667EEA), Color(0xFFC7D2F2))
                    )
                )
                .padding(horizontal = 24.dp)
                .align(Alignment.BottomCenter), // 让这个背景Row对齐在Box的底部
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 为左侧悬浮的机器人头像留出完整的空间
            Spacer(modifier = Modifier.width(ROBOT_ASSISTANT_WIDTH + 16.dp))

            // 中间的提示文字
            Text(
                text = robotMessage,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            )

            // 右侧的时间和天气
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = time, color = Color.White, fontSize = 36.sp)
                Image(
                    painter = painterResource(id = R.drawable.ic_weather_cloud),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(45.dp)
                )
                Text(text = weatherInfo, color = Color.White, fontSize = 25.sp)
            }
        }

        // 2. 机器人助手头像
        // 这个Image在Z轴上位于顶层，并且对齐到Box的左下角，从而实现“露头”的悬浮效果
        Image(
            painter = painterResource(id = R.drawable.ic_robot_zhiyun),
            contentDescription = "Robot Assistant",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .align(Alignment.BottomStart) // 对齐到Box的左下角
                .width(ROBOT_ASSISTANT_WIDTH)
                .height(ROBOT_ASSISTANT_HEIGHT)
                .clickable { /* TODO: onRobotAvatarClick */ }
        )
    }
}
// -----------------------------以下为Preview函数----------------------------------------
// 预览 1: 专门用于预览“导览界面”的特殊样式
@Preview(name = "导览界面样式预览", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
fun AppScaffoldGuidePreview() {
    // 创建一个临时的、仅用于预览的 UserProfile 对象
    val previewUserProfile = UserProfile(name = "王阿姨")

    ZhiyunAgentRobotTheme {
        // 在这里，我们精确地模拟 GuideActivity/HomeScreen 中的调用方式
        AppScaffold(
            // 1. 传入预览用的 UserProfile 对象
            userProfile = previewUserProfile,
            // 2. 为所有点击事件提供空的 lambda，确保函数调用完整
            onUserProfileClick = {},
            onOneTouchSosClick = {},
            onMoreConsultClick = {},
            onGuideClick = {},
            // 3. 传入动态数据，让精美的底部栏显示出来
            robotMessage = "你可以对我说，小智帮我做点什么...",
            time = "10:30",
            weatherInfo = "晴 25°"
        ) { innerPadding ->
            // 在主工作区放一个占位符，表示这里是主内容区
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "这里是导览界面的主内容区")
            }
        }
    }
}

// 预览 2: 专门用于预览“通用页面”的样式 (同样需要修正)
// 注意：由于我们的AppTopBar已经固定为导览页的样式，这个“通用页面预览”实际上也会显示相同的顶部栏。
// 这符合我们当前的设计，即AppScaffold现在是为导览页/主页这类顶级页面服务的。
@Preview(name = "通用页面样式预览", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
fun AppScaffoldGenericPreview() {
    // 同样需要一个临时的 UserProfile 对象
    val previewUserProfile = UserProfile(name = "李医生")

    ZhiyunAgentRobotTheme {
        AppScaffold(
            // 1. 传入预览用的 UserProfile 对象
            userProfile = previewUserProfile,
            // 2. 为所有点击事件提供空的 lambda
            onUserProfileClick = {},
            onOneTouchSosClick = {},
            onMoreConsultClick = {},
            onGuideClick = {},
            // 3. 传入底部栏数据
            robotMessage = "正在为您处理...",
            time = "14:55",
            weatherInfo = "多云 22°"
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "这里是另一个页面的主内容区")
            }
        }
    }
}

