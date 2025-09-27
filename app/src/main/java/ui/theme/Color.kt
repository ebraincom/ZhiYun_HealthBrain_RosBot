package com.zhiyun.agentrobot.ui.theme // 确保包名与您的项目一致

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Material Design 3 默认种子颜色 (通常由 'New Project' 模板提供)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// --- Colors specifically used in HomeScreen.kt and its components based on recent revisions ---

// Main layout and global elements
val MainContentAreaBg = Color(0xFFF0F4FF) // 主内容区背景色 (调整为更柔和的浅蓝紫色)
val AppTopBarSurfaceColor = Color(0xFFE5E9FF) // 顶部栏背景色
val AppBottomBarSurfaceColor = Color(0xFFC7D2F2) // 底部栏背景色

val TextPrimaryColor = Color(0xFF333333) // 主要文字颜色 (如用户名、时间、机器人消息)
val TextButtonWhite = Color.White        // 白色按钮文字
val IconTintPrimary = Color(0xFF333333)    // 主要图标颜色 (如天气图标)
val IconTintWhite = Color.White          // 白色图标 (如一键呼叫按钮图标)
val IconTintDarkBlue = Color(0xFF1A237E)  // 深蓝色图标 (如导览界面按钮图标)

// Specific Buttons in AppTopBar
val OneTouchSosButtonBg = Color(0xFFFF8400)      // 一键呼叫按钮背景
val GuideButtonBg = Color(0xFFDBE9FE)          // 导览界面按钮背景
val GuideButtonContentColor = Color(0xFF1A237E) // 导览界面按钮内容颜色

// Status Indicator
val StatusIndicatorGreen = Color(0xFF66BB6A)   // 在线状态指示灯

// ZhiyunRecordSection and ZhiyunAssistantSection Cards
val ZhiyunSectionCardBg = Color(0xFFDBE9FE) // 智芸记录和智芸助手卡片的浅紫色背景

// ZhiyunRecordSection Buttons (RecordFeatureButton)
val ShoppingCartButtonBg = Color(0xFFFFA43B)    // 购物清单按钮背景 (橙色)
val MemoButtonBg = Color(0xFF3784E5)            // 日常便签按钮背景 (蓝色)
val StorageButtonBg = Color(0xFF97F7E3)         // 物品存放按钮背景 (浅绿色)
val StorageButtonContentColor = Color(0xFF004D40) // 物品存放按钮文字/图标颜色 (深绿色)
// shoppingCartGradientBrush for ShoppingCartButton (if you implement gradient background)
val shoppingCartGradientBrush = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFED7AA), Color(0xFFFFA43B))
)
// You might want similar brushes for MemoButton and StorageButton if they are also gradients
// Example:
// val memoGradientBrush = Brush.horizontalGradient(colors = listOf(Color(0xFFBFDBFE), Color(0xFF3784E5)))
// val storageGradientBrush = Brush.horizontalGradient(colors = listOf(Color(0xFF97F7E3), Color(0xFFCBFBF1)))


// ZhiyunAssistantSection Buttons (FeatureButton)
// Group 1 (Plan, Today, Repeat reminders)
val AssistantButtonBlueBg = Color(0xFFDBE9FE)      // 背景色 (与卡片背景一致，或略作区分)
val AssistantButtonBlueContent = Color(0xFF0D47A1) // 内容颜色 (深蓝色)

// Group 2 (Medicine, AI Brain, Doctor)
val AssistantButtonYellowBg = Color(0xFFFFF9C4)        // 背景色 (淡黄色)
val AssistantButtonYellowContent = Color(0xFFE65100)  // 内容颜色 (深橙黄色)


// Image Overlay Text Colors (if not directly using Color.White or Color.Black in composables)
val ImageOverlayTextOnDarkBg = Color.White
val ImageOverlayTextOnLightBg = Color.Black

// Semi-transparent backgrounds for image overlays
val ImageOverlayDarkScrim = Color.Black.copy(alpha = 0.4f)
val ImageOverlayLightScrim = Color.White.copy(alpha = 0.85f)
val AssistantReminderListBg = Color(0xFFE0F7FA)
