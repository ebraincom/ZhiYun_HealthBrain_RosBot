package com.zhiyun.agentrobot.ui.screens

// import androidx.compose.foundation.layout.height
// 为了测试图标问题
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiyun.agentrobot.R
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.zhiyun.agentrobot.ui.theme.AssistantButtonBlueBg
import com.zhiyun.agentrobot.ui.theme.AssistantButtonBlueContent
import com.zhiyun.agentrobot.ui.theme.AssistantButtonYellowBg
import com.zhiyun.agentrobot.ui.theme.AssistantButtonYellowContent
import com.zhiyun.agentrobot.ui.theme.MainContentAreaBg
import com.zhiyun.agentrobot.ui.theme.MemoButtonBg
import com.zhiyun.agentrobot.ui.theme.StorageButtonBg
import com.zhiyun.agentrobot.ui.theme.StorageButtonContentColor
import com.zhiyun.agentrobot.ui.theme.TextButtonWhite
import com.zhiyun.agentrobot.ui.theme.TextPrimaryColor
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import com.zhiyun.agentrobot.ui.theme.shoppingCartGradientBrush
import com.zhiyun.agentrobot.MyApplication
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.zhiyun.agentrobot.ui.dialogs.RoleSelectionDialog // <-- 1. 导入我们创建的对话框
import android.content.Intent
import com.zhiyun.agentrobot.ui.guide.GuideActivity
import kotlin.math.abs
// import androidx.compose.material3.Icon // 确保导入正确的Icon

// import coil.compose.AsyncImage
// import coil.request.ImageRequest
// import com.zhiyun.agentrobot.ui.theme.AssistantButtonYellowBg
// import com.zhiyun.agentrobot.ui.theme.AssistantButtonYellowConte
val AppBottomBarGradientStartColor = Color(0xFFC7D2F2)
val AppBottomBarGradientEndColor = Color(0xFF967EEA)
val AppBottomBarContentColor = Color.White

// UserProfile Data Class
data class UserProfile(
    val name: String = "王阿姨",
    val avatarResId: Int = R.drawable.user_avatar
)

// 定义颜色
val UserProfileButtonStartColor = Color(0xFF6285EE)
val UserProfileButtonEndColor = Color(0xFF90B0FA)
val UserProfileTextColor = Color.White

val AppTopBarOverallSurfaceColor = Color.White
val ROBOT_ASSISTANT_WIDTH = 116.dp
val ROBOT_ASSISTANT_HEIGHT = 228.dp

// You can place this at the top level of HomeScreen.kt or in a separate common UI file

enum class ButtonLayoutStyle {
    ICON_TOP_TEXT_BOTTOM,
    TEXT_LEFT_ICON_RIGHT,
    ICON_LEFT_TEXT_RIGHT // If needed
}

@Composable
fun FeatureButton(
    modifier: Modifier = Modifier,
    text: String,
    iconPainter: Painter, // Use Painter for more flexibility (e.g. from ImageVector or painterResource)
    iconDescription: String?,
    onClick: () -> Unit,
    layoutStyle: ButtonLayoutStyle,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    backgroundBrush: Brush? = null, // For gradients
    contentColor: Color = contentColorFor(backgroundColor), // Helper from Material
    disabledBackgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    cornerRadius: Dp = 16.dp, // Common default, can be overridden
    iconSize: Dp = 40.dp,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    internalPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp) // Padding inside the button
) {
    Surface(
        onClick = onClick,
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)), // Clip should be on the Surface
        shape = RoundedCornerShape(cornerRadius),
        color = if (backgroundBrush == null) backgroundColor else Color.Transparent, // If brush, Surface is transparent
        // and Box inside handles brush
        contentColor = contentColor // This will be inherited by Text and Icon by default
    ) {
        Box( // Use Box to handle potential gradient background
            modifier = Modifier
                .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier)
                .padding(internalPadding),
            contentAlignment = Alignment.Center
        ) {
            when (layoutStyle) {
                ButtonLayoutStyle.ICON_TOP_TEXT_BOTTOM -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = iconPainter,
                            contentDescription = iconDescription,
                            modifier = Modifier.size(iconSize)
                            // tint = contentColor is inherited
                        )
                        Spacer(Modifier.height(4.dp)) // Adjust spacing as needed
                        Text(
                            text = text,
                            fontSize = fontSize,
                            fontWeight = fontWeight
                            // color = contentColor is inherited
                        )
                    }
                }
                ButtonLayoutStyle.TEXT_LEFT_ICON_RIGHT -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween, // Or Start with a Spacer
                        modifier = Modifier.fillMaxWidth() // Usually for this style
                    ) {
                        Text(
                            text = text,
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp) // Allow text to take space
                        )
                        Image(
                            painter = iconPainter,
                            contentDescription = iconDescription,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
                // Add ICON_LEFT_TEXT_RIGHT if needed
                ButtonLayoutStyle.ICON_LEFT_TEXT_RIGHT -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = iconPainter,
                            contentDescription = iconDescription,
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = text,
                            fontSize = fontSize,
                            fontWeight = fontWeight
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun contentColorFor(backgroundColor: Color): Color {
    return if (backgroundColor.luminance() > 0.5) Color.Black else Color.White
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userProfile: UserProfile = UserProfile(),
    onOneTouchSosClick: () -> Unit = {},
    onGuideClick: () -> Unit = {},
    onShoppingCartClick: () -> Unit = {},
    onMemoClick: () -> Unit = {},
    onStorageClick: () -> Unit = {},
    // onMemoryShowcaseClick: () -> Unit = {},
    // onReminderListClick: () -> Unit = {}, //这是智芸提醒列表大图（老奶奶形象）无需点击效果
    onPlanReminderClick: () -> Unit = {},
    onMedicineReminderClick: () -> Unit = {},
    onTodayReminderClick: () -> Unit = {},
    onAiBrainClick: () -> Unit = {},
    onRepeatReminderClick: () -> Unit = {},
    onDoctorClick: () -> Unit = {},
    // 机器人点击回调
    onRobotAvatarClicked: () -> Unit = { Log.d("HomeScreen", "Default Robot Click Handler") },
    weatherDataState: State<String>

) {
    // 2. 添加一个状态来管理对话框的显示/隐藏
    var showRoleDialog by remember { mutableStateOf(false) }

    // 3. 获取当前上下文，用于之后获取 Application 实例
    val context = LocalContext.current

    // 4. 当 showRoleDialog 状态为 true 时，显示我们的对话框
    if (showRoleDialog) {
        RoleSelectionDialog(
            onDismissRequest = {
                showRoleDialog = false // 点击对话框外部或按返回键时，关闭对话框
            },
            onRoleSelected = { selectedRole ->
                // 5. 当一个角色被选中时的核心逻辑
                val application = context.applicationContext as MyApplication
                application.switchAgentRole(selectedRole) // 调用 Application 中的方法切换角色
                showRoleDialog = false // 完成后，关闭对话框
            }
        )
    }

    // --- 传递给 AppBottomBar 的占位符/实际数据 ---

    val currentRobotMessage = stringResource(R.string.robot_greeting_message) // 替换或从 ViewModel 获取
    // val currentTimeToDisplay = "10:30 AM" // 占位符时间或从 ViewModel 获取
    // 以下是新增天气获取信息
    val currentWeatherData by weatherDataState

    // --- VV 新增：实时时间状态管理 VV ---
    var currentTimeToDisplay by remember { mutableStateOf(getCurrentFormattedTime()) }
    LaunchedEffect(Unit) { // key1 为 Unit 表示这个 effect 只在 Composable 首次进入组合时运行，并在离开时取消
        while (true) {
            currentTimeToDisplay = getCurrentFormattedTime()
            // 如果您希望时间精确到秒并每秒更新，使用 1000L
            // 如果只需要分钟级别更新，可以使用 60000L (更节省资源)
            // 考虑到截图显示的是 "10:30 AM"，没有秒，每分钟更新一次可能就够了。
            // 但如果AM/PM的切换需要在正确的时间点发生，或者分钟的跳变需要及时，每秒更新然后格式化时去掉秒是更保险的做法。
            // 我们先用每秒更新，如果性能敏感再调整。
            delay(1000L)
        }
    }
    // -- AA 新增时间管理结束

    Box(modifier = modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                AppTopBar(
                    userProfile = userProfile,
                    onUserProfileClick = { Log.d("HomeScreen", "User profile area clicked!") },
                    onOneTouchSosClick = onOneTouchSosClick,
                    onMoreConsultClick = {
                        showRoleDialog = true
                        Log.d("HomeScreen", "More Consult Button Clicked! Opening role selection dialog...")
                    },
                    onGuideClick = {
                        Log.d("HomeScreen", "导览界面按钮被点击，准备跳转...")
                        // 创建 Intent
                        val intent = Intent(context, GuideActivity::class.java)
                        // 启动 Activity
                        context.startActivity(intent)
                    }
                )
            },
            // Scaffold 的 bottomBar 为空，因为我们手动在 Box 中放置 AppBottomBar
            bottomBar = { /* Explicitly empty */ },
            modifier = Modifier.fillMaxSize() // Scaffold 填满外部 Box
        ) { innerPadding -> // innerPadding 来自 Scaffold，主要用于避开 TopAppBar

            //主要内容区域
            Row(
                modifier = Modifier
                    .padding(innerPadding) //1. 应用 Scaffold 的内边距 (避开 TopAppBar)
                    .fillMaxSize()
                    .background(MainContentAreaBg) // From Theme
                    .padding(horizontal = 43.dp, vertical = 86.dp),
                horizontalArrangement = Arrangement.Start //使用 Start,然后用 Spacer 控制精确间距
            ) {
                ZhiyunRecordSection(
                    modifier = Modifier
                        .width(772.dp)
                        .fillMaxHeight(),
                    onShoppingCartClick = onShoppingCartClick,
                    onMemoClick = onMemoClick,
                    onStorageClick = onStorageClick
                )
                Spacer(Modifier.width(122.dp))
                ZhiyunAssistantSection(
                    modifier = Modifier
                        .width(867.dp)  // 根据设计稿，如果包含外层padding 876dp
                        .fillMaxHeight(),
                    // onReminderListClick = onReminderListClick, // 大图不需要点击效果
                    onPlanReminderClick = onPlanReminderClick,
                    onMedicineReminderClick = onMedicineReminderClick,
                    onTodayReminderClick = onTodayReminderClick,
                    onAiBrainClick = onAiBrainClick,
                    onRepeatReminderClick = onRepeatReminderClick,
                    onDoctorClick = onDoctorClick
                )
            }
        } // End of Scaffold
        // AppBottomBar 在 Box 中，Scaffold 之后，但在 RobotAssistantAvatar 之前
        // AppBottomBar
        AppBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            robotMessage = currentRobotMessage,
            time = currentTimeToDisplay, // 使用动态时间管理
            weatherInfo = currentWeatherData
        )
        // RobotAssistantAvatar 在 Box 中，最后声明，所以它在最上
        val ROBOT_PADDING_FROM_LEFT = 56.dp
        val ROBOT_PADDING_FROM_BOTTOM = 2.dp
        RobotAssistantAvatar(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = ROBOT_PADDING_FROM_LEFT,
                    bottom = ROBOT_PADDING_FROM_BOTTOM
                ),
            onClick = onRobotAvatarClicked
        )
    }
}

// --- VV 新增：获取并格式化当前时间的辅助函数 VV ---
// 这个函数可以放在 HomeScreen.kt 文件的底部，或者一个通用的工具文件中
private fun getCurrentFormattedTime(): String {
    // 根据您截图的 "10:30 AM" 格式
    // hh: 12小时制的小时 (01-12)
    // mm: 分钟
    // a: AM/PM 标记
    // Locale.getDefault() 确保 AM/PM 根据设备语言显示 (例如 英语是 AM/PM, 中文可能是 上午/下午)
    val formatter = DateTimeFormatter.ofPattern("HH:mm a", Locale.getDefault())
    return LocalDateTime.now().format(formatter)
}
// --- AA 新增结束 ---
@Composable
fun AppTopBar(
    userProfile: UserProfile,
    onUserProfileClick: () -> Unit,
    onOneTouchSosClick: () -> Unit,
    onMoreConsultClick: () -> Unit, // 新增的回调onMoreConsultClick: () -> Unit, // 新增的回调
    onGuideClick: () -> Unit
) {
    // 整个TopBar的高度，设计图是113dp，需要确保内部元素能合理布局
    // 按钮图片高度是 63dp，用户信息区的复合按钮也尽量与之协调
    Log.d("AppTopBar", "Rendering AppTopBar (M3 Version)...")
    val buttonImageHeight = 63.dp // SOS 和 Guide 按钮的高度
    val userProfileButtonHeight = buttonImageHeight // 尝试让用户区按钮与图片按钮等高
    val imageButtonWidth = 175.dp
    val moreInquiriesButtonWidth = 175.dp // 为"更多咨询"按钮设置一个明确的宽度
    val context = LocalContext.current




    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(113.dp), // 保持TopBar整体高度
        color = AppTopBarOverallSurfaceColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // == 左侧用户区 + “一键呼叫”图片按钮 ==
            Row(verticalAlignment = Alignment.CenterVertically) {
                // -- 用户信息按钮 --
                Row(
                    modifier = Modifier
                        .height(userProfileButtonHeight) // 使用与其他按钮协调的高度
                        .clip(RoundedCornerShape(userProfileButtonHeight / 2)) // 圆角半径为高度的一半，形成胶囊状
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    UserProfileButtonStartColor,
                                    UserProfileButtonEndColor
                                )
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
                            .size(userProfileButtonHeight - 16.dp) // 例如: 63dp - 16dp = 47dp, 请调整以获得最佳视觉
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop // 确保图片填满并正确裁剪
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = userProfile.name,
                        color = UserProfileTextColor,
                        fontSize = 18.sp, // 请根据设计稿微调字号
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Info, // 替换为您的实际小图标
                        contentDescription = "Info",
                        tint = UserProfileTextColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp) // 小图标尺寸
                    )
                }

                Spacer(Modifier.width(16.dp)) // 用户按钮和SOS按钮之间的间距

                // -- “一键呼叫”图片按钮 --
                Image(
                    painter = painterResource(id = R.drawable.one_touch_sos), // 使用正确的资源名
                    contentDescription = "一键呼叫",
                    modifier = Modifier
                        .width(175.dp)
                        .height(buttonImageHeight) // 63.dp
                        .clickable { onOneTouchSosClick() },
                    contentScale = ContentScale.FillBounds // 或者 Fit, 取决于图片是否需要严格保持宽高比
                )
            } // 左侧区域结束

            // == 右侧的按钮组，“更多咨询”+导览界面 ==
            Row(verticalAlignment = Alignment.CenterVertically) {
                Log.d("AppTopBar", "Rendering 'More Inquiries' button...") // 调试日志
                // -- “更多咨询” 图标按钮 -- (新增)
                Image(
                    painter = painterResource(id = R.drawable.ic_for_more_inquiries),
                    contentDescription = "更多咨询", // 用于无障碍
                    modifier = Modifier
                        .width(moreInquiriesButtonWidth)
                        .height(buttonImageHeight)
                        // .background(Color.Magenta)
                        .clickable { onMoreConsultClick() },
                    contentScale = ContentScale.FillBounds // 使用 Fit 以免图片变形，同时能看到背景色
                )

                Spacer(Modifier.width(16.dp)) // “更多咨询”和“导入界面”之间的间距, 根据视觉调整

                // -- “导入界面”图片按钮 --
                Image(
                    painter = painterResource(id = R.drawable.ic_guide_scan),
                    contentDescription = "导览界面",
                    modifier = Modifier
                        .width(175.dp) // 根据您的图片资源调整宽度
                        .height(buttonImageHeight)
                        .clickable { onGuideClick() },
                    contentScale = ContentScale.FillBounds // 或 FillWidth/FillHeight/Fit
                )

            } // 右侧区域结束
        }
    }
}

@Composable
fun AppBottomBar(
    modifier: Modifier = Modifier,
    robotMessage: String,
    weatherIconResId: Int = R.drawable.ic_weather_cloud,
    time: String,
    weatherInfo: String,
    gradientColorStart: Color = Color(0xFFC7D2F2),
    gradientColorEnd: Color = Color(0xFF667EEA),
    contentTextColor: Color = Color.White // 您原始的是 White，我们后面讨论改为 Black
) {
    // --- 内部使用的尺寸和计算值 ---

    val robotMessageFontSize = 32.sp
    val robotMessageMaxHeight = 70.dp
    val robotMessageContainerMaxWidth = 600.dp
    val bottomBarActualHeight = 137.dp

    val weatherIconSize = 45.dp

    val horizontalPaddingOverall = 24.dp
    val messageStartPadding = horizontalPaddingOverall + 120.dp // 示例：整体边距 + 一点额外空间
    // 机器人图标现在独立布局，文本区域需要从其右侧开始，或者有一个明确的起始边距
    // 我们让文本区域从 horizontalPaddingOverall + robotAvatarDisplayWidth + 适当间距 开始
    val bottomBarBrush = Brush.horizontalGradient(
        colors = listOf(gradientColorEnd, gradientColorStart)
    )
    // 1. 底部栏的背景和主要内容 (除了机器人图标)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(137.dp)
            .background(bottomBarBrush)
            .padding(horizontal = horizontalPaddingOverall),
            // .padding(start = messageStartPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = robotMessage,
            color = contentTextColor,
            fontSize = robotMessageFontSize,
            fontWeight = FontWeight.W400,
            modifier = Modifier
                .weight(1f) // 允许文本区域扩展
                .heightIn(max = robotMessageMaxHeight)
                .padding(start = 164.dp)
                .padding(end = 16.dp), // 文本和右侧时间天气区域的间距
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        // 右侧时间天气信息
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = time,
                color = contentTextColor,
                fontSize = 36.sp // 或者您期望的字体大小
            )
            Image(
                painter = painterResource(R.drawable.ic_weather_cloud),
                contentDescription = "Weather Icon",            // 之前是 iconDescription，已修正
                modifier = Modifier.size(weatherIconSize)
            )
            Text(
                text = weatherInfo,
                color = contentTextColor,
                fontSize = 25.sp // 或者您期望的字体大小
            )
        }
    }
}
// RobotAssistantAvatar.kt (或 HomeScreen.kt 内)
@Composable
fun RobotAssistantAvatar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit

) {
    Image(
        painter = painterResource(id = R.drawable.ic_robot_zhiyun), // <--- 更新的资源名称
        contentDescription = stringResource(R.string.robot_assistant_avatar_description), // 确保此字符串资源存在
        contentScale = ContentScale.FillBounds, // 或根据您的图片资源和期望调整，例如 ContentScale.FillHeight
        modifier = modifier
            .width(ROBOT_ASSISTANT_WIDTH)  // 89.dp
            .height(ROBOT_ASSISTANT_HEIGHT) // 161.dp
            .clickable(
                onClick = onClick,
                role = Role.Button,
                onClickLabel = stringResource(R.string.robot_avatar_click_label) // 确保此字符串资源存在
            )
    )
}




@Composable
fun ZhiyunRecordSection(
    modifier: Modifier = Modifier,
    onShoppingCartClick: () -> Unit,
    onMemoClick: () -> Unit,
    onStorageClick: () -> Unit,
    // MODIFICATION: 移除了 onMemoryShowcaseClick 参数，因为右侧大图是静态展示
) {
    // 调整: Card 的 shape应该是整个内容区的圆角，设计稿是 11.dp (不是20.dp)
    // Card 的 padding 应该在 Column 之外，或者 Column 不加 padding，让 Card 的 contentPadding 控制。
    // 根据我们最新的精确分析：
    // 标题“智芸记录”距离其下方内容区的顶部是 43dp。
    // 内容区本身是一个 Row (左按钮列 + 右大图)，高 492dp。
    // Card 的 containerColor 应该是透明的，如果背景由 HomeScreen 的 Row 提供，

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.zhiyun_record_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryColor, // From Theme
            modifier = Modifier.padding(bottom = 43.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxSize() //度由 HomeScreen 中调用时指定 (772.dp)
                .height(492.dp)
            // .clip(RoundedCornerShape(11.dp)) //但如果背景由HomeScreen的父Row提供，则这里不需要
        ) {
            // 左侧按钮的Column
            Column(
                modifier = Modifier
                    .width(349.dp)  //左侧按钮宽度
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {

                // 按钮也应该使用 FeatureButton统一
                // MODIFICATION: “购物清单”按钮统一使用 FeatureButton
                FeatureButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(149.dp),
                    text = stringResource(R.string.shopping_list_button),
                    iconPainter = painterResource(id = R.drawable.ic_shopping_cart_placeholder),
                    iconDescription = stringResource(R.string.shopping_list_button),
                    onClick = onShoppingCartClick,
                    layoutStyle = ButtonLayoutStyle.TEXT_LEFT_ICON_RIGHT,
                    // enabled = true, // 可以省略，因为它有默认值，或者显式提供
                    // backgroundColor = ..., // 如果这个按钮用 brush，backgroundColor 可以不提供或提供一个透明/默认色
                    backgroundBrush = shoppingCartGradientBrush, // 明确指定 backgroundBrush
                    contentColor = TextButtonWhite,
                    cornerRadius = 7.dp,
                    iconSize = 74.dp,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                // MODIFICATION: “日常便签”按钮调用 FeatureButton，确保参数完整
                FeatureButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(149.dp),
                    text = stringResource(R.string.daily_memo_button),
                    iconPainter = painterResource(id = R.drawable.ic_memo_placeholder),
                    iconDescription = stringResource(R.string.daily_memo_button),
                    onClick = onMemoClick,
                    layoutStyle = ButtonLayoutStyle.TEXT_LEFT_ICON_RIGHT,
                    // enabled = true,
                    backgroundColor = MemoButtonBg, // 明确指定 backgroundColor
                    // backgroundBrush = null, // 可以省略，因为它默认为 null
                    contentColor = StorageButtonContentColor,
                    cornerRadius = 7.dp,
                    iconSize = 74.dp,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                // MODIFICATION: “物品存放”按钮调用 FeatureButton，确保参数完整
                FeatureButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(149.dp),
                    text = stringResource(R.string.item_storage_button),
                    iconPainter = painterResource(id = R.drawable.ic_storage_placeholder),
                    iconDescription = stringResource(R.string.item_storage_button),
                    onClick = onStorageClick,
                    layoutStyle = ButtonLayoutStyle.TEXT_LEFT_ICON_RIGHT,
                    // enabled = true, // 可以省略
                    backgroundColor = StorageButtonBg, // 明确是 backgroundColor
                    // backgroundBrush = null, // 可以省略
                    contentColor = StorageButtonContentColor,
                    cornerRadius = 7.dp,
                    iconSize = 74.dp,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold

                )
            }
            // 调整: 左按钮列和右大图之间的 Spacer
            Spacer(Modifier.width(32.dp))

            // MODIFICATION: 右侧大图简化为静态 Image，移除了不必要的 Card 和文字叠加逻辑
            Image(
                painter = painterResource(id = R.drawable.img_memory_showcase), // 这是"智芸回忆录"带文字的大图
                contentDescription = stringResource(R.string.memory_showcase_content_description), // 用于可访问性
                modifier = Modifier
                    .width(391.dp) // 设计稿规范：右侧大图固定宽度
                    .height(492.dp), // 设计稿规范：右侧大图固定高度 (或 .fillMaxHeight() 保持与左列等高)
                // .clip(RoundedCornerShape(11.dp)) // MODIFICATION: 如果图片本身需要圆角，则添加
                contentScale = ContentScale.Crop
            )
        }
    }
}

// ------------------- ZhiyunAssistantSection 及内部组件 -------------------
// 主要是尺寸和间距要从 ZhiyunAssistantSection 传入设计稿的精确值。
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZhiyunAssistantSection(
    modifier: Modifier = Modifier,
    onPlanReminderClick: () -> Unit, onMedicineReminderClick: () -> Unit,
    onTodayReminderClick: () -> Unit, // 新增
    onAiBrainClick: () -> Unit,       // 新增
    onRepeatReminderClick: () -> Unit, // 新增
    onDoctorClick: () -> Unit          // 新增
) {
    // 建议: 根应该是 Column (标题 + 内容Row)
    Column(modifier = Modifier) {
        Text(
            text = stringResource(R.string.zhiyun_assistant_title), // AI建议: stringResource(R.string.zhiyun_assistant_title)
            style = MaterialTheme.typography.headlineSmall, // 建议: 核对设计稿字体
            fontWeight = FontWeight.Bold,
            color = TextPrimaryColor,
            // AI调整: 标题与下方内容区的间距，设计稿是 36dp
            modifier = Modifier.padding(bottom = 36.dp)
        )
        val overallContentHeight = 492.dp     // [S1] 内容区域总高度 (对应左侧图片高度)
        val leftImageWidth = 369.dp           // [S2] 左侧图片宽度
        val imageAndButtonsSpacing = 31.dp    // [S3] 左侧图片与右侧按钮区之间的间距
        val buttonColumnsSpacing = 31.dp      // [S4] 两列按钮之间的间距
        val buttonWidth = 218.dp              // [S5] 单个按钮宽度
        val buttonHeight = 148.dp             // [S6] 单个按钮高度
        val buttonVerticalSpacing = 24.dp     // [S7] 按钮之间的垂直间距
        // --- 样式参数 ---
        val cardCornerRadius = 11.dp          // [S8] 卡片和图片的圆角
        // val sectionOuterPadding = 35.dp       // [S9] 整个 Section 距离屏幕/父容器边缘的 padding (假设)
        //您可以根据实际 HomeScreen 的布局调整这个
        // 在您的原代码中是 sectionPadding = 8.dp，请根据设计稿调整
        // val titleBottomPadding = 20.dp        // [S10] "智芸助手"标题下方的间距 (假设)
        // val iconSizeForButtons = 60.dp        // [S11] 按钮内图标大小 (可微调)
        // val textSizeForButtons = 12.sp        // [S12] 按钮内文字大小
        // 从您的代码片段中获取这些值，如果它们定义在ZhiyunAssistantSection之外，请确保可以访问
        // 如果它们是固定值，也可以直接在这里定义或从Theme获取
        // val iconSizeForAssistant = 40.dp // 假设这是您定义的 Dp 值
        // val textSizeForAssistant = 12.sp // 假设这是您定义的 TextUnit 值
        Row(
            modifier = Modifier
                // AI建议: 这个Row不需要 weight(1f) 如果左图是固定宽度，它会自动占据剩余空间
                // 它的宽度是 2*buttonWidth + buttonColumnsSpacing = 2*218 + 31 = 467dp
                .fillMaxWidth()                 // 占据父 Column 的全部宽度
                .height(overallContentHeight)  // 固定内容区域高度
                .clip(RoundedCornerShape(11.dp)), // AI调整: 内容区圆角
            verticalAlignment = Alignment.CenterVertically,   // 使得 Text(robotMessage) 和 Row(timeAndWeather) 垂直居中
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_reminder_list_showcase),
                contentDescription = stringResource(R.string.reminder_list_showcase_content_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(leftImageWidth)
                    .fillMaxHeight()
            )
            // 2. 左侧大图和右侧按钮区之间的间距 (直接子元素)
            Spacer(Modifier.width(imageAndButtonsSpacing))
            // 3.右侧按钮曲区(ROW)(直接子元素）
            Row(
                modifier = Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(buttonColumnsSpacing)
            ) {
                val buttonFixedSizeModifier = Modifier
                    .width(218.dp) // buttonWidth
                    .height(148.dp) // buttonHeight

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(buttonVerticalSpacing)
                ) {
                    // 按钮1：计划提醒
                    FeatureButton(
                        modifier = buttonFixedSizeModifier,
                        text = stringResource(R.string.plan_reminder_button),
                        iconPainter = painterResource(R.drawable.ic_plan_placeholder),
                        iconDescription = stringResource(R.string.plan_reminder_button), // 添加描述
                        onClick = onPlanReminderClick,
                        backgroundColor = AssistantButtonBlueBg,
                        contentColor = AssistantButtonBlueContent,
                        layoutStyle = ButtonLayoutStyle.ICON_TOP_TEXT_BOTTOM, // <-- 新增：指定布局
                        iconSize = 60.dp,
                        fontSize = 12.sp
                    )
                    // 按钮3：当天提醒
                    FeatureButton(
                        modifier = buttonFixedSizeModifier,
                        text = stringResource(R.string.today_reminder_button),
                        iconPainter = painterResource(R.drawable.ic_today_reminder_placeholder),
                        iconDescription = stringResource(R.string.today_reminder_button),
                        onClick = onTodayReminderClick,
                        backgroundColor = AssistantButtonBlueBg, // 您可以根据需要调整颜色
                        contentColor = AssistantButtonBlueContent,
                        layoutStyle = ButtonLayoutStyle.ICON_TOP_TEXT_BOTTOM,
                        iconSize = 60.dp,
                        fontSize = 12.sp
                    )
                    // 按钮5：重复提醒
                    FeatureButton(
                        modifier = buttonFixedSizeModifier,
                        text = stringResource(R.string.repeat_reminder_button),
                        iconPainter = painterResource(R.drawable.ic_repeat_placeholder),
                        iconDescription = stringResource(R.string.repeat_reminder_button),
                        onClick = onRepeatReminderClick,
                        backgroundColor = AssistantButtonBlueBg, // 您可以根据需要调整颜色
                        contentColor = AssistantButtonBlueContent,
                        layoutStyle = ButtonLayoutStyle.ICON_TOP_TEXT_BOTTOM,
                        iconSize = 60.dp,
                        fontSize = 12.sp
                    )
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(buttonVerticalSpacing)
                ) {
                    // 按钮2：服药管理
                    FeatureButton(
                        modifier = buttonFixedSizeModifier,
                        text = stringResource(R.string.medicine_reminder_button),
                        iconPainter = painterResource(R.drawable.ic_medicine_placeholder),
                        iconDescription = stringResource(R.string.medicine_reminder_button),
                        onClick = onMedicineReminderClick,
                        backgroundColor = AssistantButtonYellowBg,
                        contentColor = AssistantButtonYellowContent,
                        layoutStyle = ButtonLayoutStyle.ICON_TOP_TEXT_BOTTOM,
                        iconSize = 60.dp,
                        fontSize = 12.sp
                    ) // 按钮4：AI康养大脑
                    FeatureButton(
                        modifier = buttonFixedSizeModifier,
                        text = stringResource(R.string.ai_brain_button),
                        iconPainter = painterResource(R.drawable.ic_ai_brain_placeholder),
                        iconDescription = stringResource(R.string.ai_brain_button),
                        onClick = onAiBrainClick,
                        backgroundColor = AssistantButtonYellowBg, // 您可以根据需要调整颜色
                        contentColor = AssistantButtonYellowContent,
                        layoutStyle = ButtonLayoutStyle.ICON_TOP_TEXT_BOTTOM,
                        iconSize = 60.dp,
                        fontSize = 12.sp
                    ) // 按钮6 :医博士
                    FeatureButton(
                        modifier = buttonFixedSizeModifier,
                        text = stringResource(R.string.doctor_button),
                        iconPainter = painterResource(R.drawable.ic_doctor_placeholder),
                        iconDescription = stringResource(R.string.doctor_button),
                        onClick = onDoctorClick,
                        backgroundColor = AssistantButtonYellowBg,
                        contentColor = AssistantButtonYellowContent,
                        layoutStyle = ButtonLayoutStyle.ICON_TOP_TEXT_BOTTOM,
                        iconSize = 60.dp,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}


// --- Preview Functions ---

@Preview(showBackground = true, name = "AppBottomBar Preview", widthDp = 1920)
@Preview(
    showBackground = true,
    name = "HomeScreen Landscape Preview",
    widthDp = 1920,
    heightDp = 1080
)
@Composable
fun HomeScreenLandscapePreview() {
    ZhiyunAgentRobotTheme {
        // ▼▼▼【核心修正】▼▼▼
        // 1. 在这里创建一个“假的”State，专门用于UI预览。
        //    它的值可以是一个写死的预览文本，例如 "晴 25℃"。
        //    这个 fakeWeatherState 只在这个预览函数内部有效。
        val fakeWeatherState = remember { mutableStateOf("晴 25℃") }

        // 2. 在调用 HomeScreen 时，把这个“假的”State作为“通行证”传递进去。
        HomeScreen(
            weatherDataState = fakeWeatherState, // <-- 使用我们创建的假数据

            // 保持您原来所有的 on...Click 回调为空实现，以确保预览能正常工作
            // onMemoryShowcaseClick = { Log.d("Preview", "SOS clicked in HomeScreen Preview") },
            onGuideClick = { Log.d("Preview","Guide clicked in HomeScreen Preview") },
            onShoppingCartClick = { Log.d("Preview","ShoppingCart clicked") },
            onMemoClick = { Log.d("Preview","Memo clicked") },
            onStorageClick = { Log.d("Preview","Storage clicked") },
            onPlanReminderClick = { Log.d("Preview","PlanReminder clicked") },
            onMedicineReminderClick = { Log.d("Preview","MedicineReminder clicked") },
            onTodayReminderClick = { Log.d("Preview","TodayReminder clicked") },
            onAiBrainClick = { Log.d("Preview","AiBrain clicked") },
            onRepeatReminderClick = { Log.d("Preview","RepeatReminder clicked") },
            onDoctorClick = { Log.d("Preview","Doctor clicked") },
            // 如果您的 HomeScreen 还有其他参数，也请一并补全
            onRobotAvatarClicked = { Log.d("Preview", "Robot Avatar Clicked") } // 举例：补全可能缺失的回调
        )
        // ▲▲▲【核心修正】▲▲▲
    }
}

@Preview(showBackground = true, name = "AppTopBar Preview", widthDp = 1920)
@Composable
fun AppTopBarPreview() {
    ZhiyunAgentRobotTheme {
        AppTopBar(
                userProfile = UserProfile(name = "横屏预览用户", avatarResId = R.drawable.user_avatar),
                onUserProfileClick = { Log.d("Preview", "SOS clicked") },
                onOneTouchSosClick = { Log.d("Preview", "SOS clicked") },
                onMoreConsultClick = { Log.d("Preview","MoreConsult clicked") },
                onGuideClick = { Log.d("Preview","Guide clicked") }
        )
    }
}
@Preview(showBackground = true, name = "AppBottomBar Preview", widthDp = 1920)
@Composable
fun AppBottomBarPreview() {
    ZhiyunAgentRobotTheme {
        AppBottomBar(
            robotMessage = stringResource(R.string.robot_greeting_message), // <-- AI建议：添加
            time = "10:30 AM",                                               // <-- AI建议：添加
            weatherInfo = "晴转多云 25-32度"
        )
    }
}
@Preview(
    showBackground = true,
    name = "ZhiyunRecordSection Preview",
    widthDp = 900,
    heightDp = 600
)
@Composable
fun ZhiyunRecordSectionPreview() {
    ZhiyunAgentRobotTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(MainContentAreaBg) // Ensure MainContentAreaBg is defined in your theme
                .fillMaxSize()
        ) {
            ZhiyunRecordSection(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(end = 8.dp),
                onShoppingCartClick = {},
                onMemoClick = {},
                onStorageClick = {},
                // onMemoryShowcaseClick = {}
            )
        }
    }
}
@Preview(
    showBackground = true,
    name = "ZhiyunAssistantSection Preview",
    widthDp = 900,
    heightDp = 600
)
@Composable
fun ZhiyunAssistantSectionPreview() {
    ZhiyunAgentRobotTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(MainContentAreaBg) // Ensure MainContentAreaBg is defined
                .fillMaxSize()
        ) {
            ZhiyunAssistantSection(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(start = 8.dp),
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
@Preview(showBackground = true, name = "FeatureButton - Shopping Gradient Preview")
@Composable
fun RecordFeatureButtonShoppingGradientPreview() {
    ZhiyunAgentRobotTheme {
        Box(
            modifier = Modifier
                .height(150.dp)
                .width(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush = shoppingCartGradientBrush) // Ensure shoppingCartGradientBrush is defined
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shopping_cart_placeholder),
                    contentDescription = "购物清单",
                    modifier = Modifier.size(40.dp),
                    tint = TextButtonWhite
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "购物清单",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = TextButtonWhite
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "FeatureButton - Assistant Preview (using your FeatureButton)")
@Composable
fun FeatureButtonAssistantPreview() {
    ZhiyunAgentRobotTheme {
        // This uses the FeatureButton defined in this file.
        // If you intend to preview FeatureButton, you should call that one.
        // If you want to preview Assistant
        FeatureButton(
            text = stringResource(R.string.plan_reminder_button),
            iconPainter = painterResource(R.drawable.ic_plan_placeholder),
            onClick = {},
            modifier = Modifier
                .height(100.dp)
                .width(150.dp),
            backgroundColor = AssistantButtonBlueBg,
            contentColor = AssistantButtonBlueContent,
            iconSize = 36.dp, // Ensure this matches FeatureButton's parameter
            fontSize = 14.sp,
            layoutStyle = ButtonLayoutStyle.ICON_TOP_TEXT_BOTTOM,
            iconDescription = stringResource(R.string.plan_reminder_button)
            // Other parameters are inherited from FeatureButton
        )
    }
}

@Preview(showBackground = true, widthDp = 1000, backgroundColor = 0xFFFFFFFF, heightDp = 120)
@Composable
fun AppTopBarSizedPreview() {
    // MaterialTheme { // 应用您的主题
    AppTopBar(
        userProfile = UserProfile(name = "李时珍", avatarResId = R.drawable.user_avatar),
        onUserProfileClick = { Log.d("Preview", "User profile clicked in preview") }, // ！！！添加这个参数！！！
        onOneTouchSosClick = { Log.d("Preview", "SOS clicked in preview") },
        onMoreConsultClick = { Log.d("Preview", "MoreConsult clicked in preview") },
        onGuideClick = { Log.d("Preview", "Guide clicked in preview") }
    )
    // }
}
