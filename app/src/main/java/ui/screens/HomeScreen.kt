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
import com.zhiyun.agentrobot.ui.theme.AppTopBarSurfaceColor
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
import android.util.Log

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
) {
    Scaffold(
        topBar = {
            AppTopBar(
                userProfile = userProfile,
                onUserProfileClick = {
                    // 当用户区域被点击时执行的操作
                    // 例如：导航到用户详情页，或显示一个弹窗
                    Log.d("HomeScreen", "User profile area clicked!")
                    // viewModel.onUserProfileClicked() // 如果使用 ViewModel
                }, // ！！！添加这个参数！！！
                onOneTouchSosClick = {
                    // onOneTouchSosClick 的逻辑
                    Log.d("HomeScreen", "SOS clicked!")
                    // viewModel.onSosClicked()
                },
                onGuideClick = {
                    // onGuideClick 的逻辑
                    Log.d("HomeScreen", "Guide clicked!")
                    // viewModel.onGuideClicked()
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                robotMessage = stringResource(R.string.robot_greeting_message),
                time = "10:30 AM ", // Placeholder
                weatherInfo = "晴转多云 25-32度" // Placeholder
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MainContentAreaBg) // From Theme
                .padding(horizontal = 43.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.Start //使用 Start,然后用 Spacer 控制精确间距
        ) {
            ZhiyunRecordSection(
                modifier = Modifier
                    .width(772.dp)
                    .fillMaxHeight(),
                onShoppingCartClick = onShoppingCartClick,
                onMemoClick = onMemoClick,
                onStorageClick = onStorageClick,
                // onMemoryShowcaseClick = onMemoryShowcaseClick
            )
            // 添加精确的 Spacer
            Spacer(Modifier.width(122.dp))

            ZhiyunAssistantSection(
                modifier = Modifier
                    .width(867.dp)  // 根据设计稿，如果包含外层padding 876dp
                    .fillMaxHeight(),
                // onReminderListClick = onReminderListClick,
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
    onUserProfileClick: () -> Unit,
    onOneTouchSosClick: () -> Unit,
    onGuideClick: () -> Unit
) {
    // 整个TopBar的高度，设计图是113dp，需要确保内部元素能合理布局
    // 按钮图片高度是 63dp，用户信息区的复合按钮也尽量与之协调
    val buttonImageHeight = 63.dp // SOS 和 Guide 按钮的高度
    val userProfileButtonHeight = buttonImageHeight // 尝试让用户区按钮与图片按钮等高

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(113.dp), // 保持TopBar整体高度
        color = AppTopBarOverallSurfaceColor,
        shadowElevation = 2.dp
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
                                colors = listOf(UserProfileButtonStartColor, UserProfileButtonEndColor)
                            )
                        )
                        .clickable { onUserProfileClick() }
                        .padding(start = 8.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = userProfile.avatarResId),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            // 为了圆形效果，使用一个正方形尺寸，且小于等于按钮高度减去一些内部padding
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

            // == 最右侧“导入界面”图片按钮 ==
            Image(
                painter = painterResource(id = R.drawable.ic_guide_scan), // 使用正确的资源名
                contentDescription = "导入界面",
                modifier = Modifier
                    .width(175.dp)
                    .height(buttonImageHeight) // 63.dp
                    .clickable { onGuideClick() },
                contentScale = ContentScale.FillBounds // 或者 Fit
            )
        }
    }
}

@Composable
fun AppBottomBar(
    modifier: Modifier = Modifier,
    robotMessage: String,
    time: String,
    weatherInfo: String,
    robotAvatarResId: Int = R.drawable.ic_robot_zhiyun,
    weatherIconResId: Int = R.drawable.ic_weather_cloud
) {
    // --- 渐变背景画刷 ---
    val bottomBarBrush = Brush.linearGradient(
        colors = listOf(AppBottomBarGradientStartColor, AppBottomBarGradientEndColor),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    // --- 从设计稿提取的尺寸和字体大小 (直接使用 px 值作为 dp/sp, 因为是 MDPI 160dpi) ---

    // 底部栏整体高度
    val bottomBarHeight = 137.dp // 设计稿: 137px

    // 机器人头像
    // 设计稿中机器人图片本身很大，这里需要一个适合底部栏的显示尺寸。
    // 设计稿中机器人头像区域的宽度是 267px，如果头像是贴左的，
    // 我们可以估算一个头像本身的尺寸，例如 80dp (px)。
    // ** 这个值仍需您根据视觉效果仔细调整，以使其在137dp高的底部栏中看起来协调 **
    val robotAvatarDisplaySize = 80.dp // !! 这是一个关键的调整点 !!

    // 机器人说话文本
    val robotMessageFontSize = 32.sp     // 设计稿: 32px (font-size)
    val robotMessageMaxHeight = 36.dp    // 设计稿文本容器高度: 36px
    val robotMessageContainerWidth = 699.dp // 设计稿文本容器宽度: 699px

    // 天气图标
    val weatherIconSize = 45.dp          // 设计稿: 45px

    // 间距 (直接使用设计稿 px 值作为 dp)
    val horizontalPaddingOverall = 24.dp   // 底部栏左右的整体内边距 (可根据设计稿调整，第一张图左右有边距)
    val spacingRobotToText = 51.dp         // 设计稿: 51px
    // 机器人说话文本区域(699dp)之后，到时间文本(95dp)开始，设计图标注50px间距
    // 我们用SpaceBetween，所以这个间距由布局自动处理一部分，但可以考虑调整weight或添加一个小的Spacer
    val spacingTextContainerToEnd = 50.dp  // 文字容器到时间区域的间距

    val spacingTimeInternal = 10.dp        // 时间和天气图标之间的间距 (估计值)
    val spacingWeatherIconToText = 8.dp    // 天气图标和天气文字之间的间距 (估计值)
    val timeTextWidth = 95.dp              // 设计稿时间文本宽度: 95px


    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bottomBarHeight)
            .background(bottomBarBrush)
            .padding(horizontal = horizontalPaddingOverall)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 左侧区域: 机器人头像 + 消息 ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                // modifier = Modifier.weight(1f) // 先去掉weight，让其自然包裹内容
            ) {
                Image(
                    painter = painterResource(id = robotAvatarResId),
                    contentDescription = "Robot Avatar",
                    modifier = Modifier.size(robotAvatarDisplaySize), // 应用机器人头像显示尺寸
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(spacingRobotToText))
                Text(
                    text = robotMessage,
                    color = AppBottomBarContentColor,
                    fontSize = robotMessageFontSize,
                    fontWeight = FontWeight.W400, // 设计稿 Cousine, weight 400
                    // fontFamily = FontFamily(Font(R.font.cousine_regular)), // !! 如果集成了Cousine字体 !!
                    modifier = Modifier
                        .widthIn(max = robotMessageContainerWidth) // 限制文本区域的最大宽度
                        .heightIn(max = robotMessageMaxHeight), // 限制文本区域的最大高度
                    maxLines = 2 // 允许机器人消息最多显示两行
                )
            }
            // Spacer(Modifier.weight(1f)) // 如果左侧内容和右侧内容需要尽可能分开，可以加一个权重spacer

            // --- 右侧区域: 时间 + 天气图标 + 天气信息 ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = time,
                    color = AppBottomBarContentColor,
                    fontSize = 36.sp, // 根据视觉效果调整，通常时间字体较大
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(timeTextWidth) // 应用设计稿时间文本宽度
                )
                Spacer(Modifier.width(spacingTimeInternal))
                Image(
                    painter = painterResource(id = weatherIconResId),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(weatherIconSize)
                )
                Spacer(Modifier.width(spacingWeatherIconToText))
                Text(
                    text = weatherInfo,
                    color = AppBottomBarContentColor,
                    fontSize = 14.sp, // 根据视觉效果调整
                    lineHeight = 18.sp,
                )
            }
        }
    }
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
                    modifier = Modifier.fillMaxWidth().height(149.dp),
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
                    modifier = Modifier.fillMaxWidth().height(149.dp),
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
                    modifier = Modifier.fillMaxWidth().height(149.dp),
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
        // 直接调用 HomeScreen。
        // HomeScreen 内部的 Scaffold 会负责创建 AppTopBar 和 AppBottomBar。
        HomeScreen(
            // 如果您的 HomeScreen Composable 函数需要任何参数来进行预览，
            // 请在此处提供这些参数。
            // 例如，如果 HomeScreen 需要一个 UserProfile 和一些点击回调：
            // userProfile = UserProfile(name = "横屏预览用户", avatarResId = R.drawable.user_avatar),
            // onOneTouchSosClick = { Log.d("Preview", "SOS clicked in HomeScreen Preview") },
            // onGuideClick = { Log.d("Preview","Guide clicked in HomeScreen Preview") },
            // onShoppingCartClick = { Log.d("Preview","ShoppingCart clicked") },
            // onMemoClick = { Log.d("Preview","Memo clicked") },
            // onStorageClick = { Log.d("Preview","Storage clicked") },
            // onPlanReminderClick = { Log.d("Preview","PlanReminder clicked") },
            // onMedicineReminderClick = { Log.d("Preview","MedicineReminder clicked") },
            // onTodayReminderClick = { Log.d("Preview","TodayReminder clicked") },
            // onAiBrainClick = { Log.d("Preview","AiBrain clicked") },
            // onRepeatReminderClick = { Log.d("Preview","RepeatReminder clicked") },
            // onDoctorClick = { Log.d("Preview","Doctor clicked") }
            // 确保上面示例中的参数名和类型与您实际的 HomeScreen 函数定义一致。
            // 如果 HomeScreen 不需要任何参数，则直接调用 HomeScreen() 即可。
        )
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
        onGuideClick = { Log.d("Preview", "Guide clicked in preview") }
    )
    // }
}

