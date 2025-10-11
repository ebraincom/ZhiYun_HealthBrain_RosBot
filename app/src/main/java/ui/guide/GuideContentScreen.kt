package com.zhiyun.agentrobot.ui.guide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiyun.agentrobot.R // 确保您的R文件路径正确
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme // 确保您的主题路径正确
// ... (所有必要的 imports, 尤其是 foundation.pager 和 kotlinx.coroutines.delay) ...
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.delay

import androidx.compose.runtime.LaunchedEffect  // <-- LaunchedEffect 的身份证明

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GuideContentScreen(
    modifier: Modifier = Modifier,
    items: List<GuidePageUiItem>,
    selectedItem: GuidePageUiItem,
    onItemSelected: (GuidePageUiItem) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    // --- 交互逻辑 (保持不变) ---
    LaunchedEffect(selectedItem) {
        val selectedIndex = items.indexOf(selectedItem)
        if (selectedIndex != -1) { pagerState.animateScrollToPage(selectedIndex) }
    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) { onItemSelected(items[pagerState.currentPage]) }
    }
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(5000L)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    // --- 【最终布局】: 遵循“协作范式”的三层嵌套结构 ---
    // 第一层: 根容器，用于定位“真·工作区背景”
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 第二层: “真·工作区背景”，尺寸为您计算的 1920x830
        Box(
            modifier = Modifier
                .width(1920.dp) // 工作区背景宽度拉通
                .height(830.dp)  // 1080 - 113(头) - 137(底) = 830
                //.align(Alignment.TopCenter) // 顶部对齐
                //.offset(y = 113.dp) // 向下偏移头部高度
                //.background(Color.White.copy(alpha = 0.2f)) // 用半透明白色标识背景区，便于调试
        ) {

            // 第三层: “内容组合容器”，用于将所有内容组合并居中
            Box(
                modifier = Modifier
                    .width(1693.dp)  // 1286(左图) + 79(间距) + 328(右按钮) = 1693
                    .height(657.dp) // 内容的实际高度
                    .align(Alignment.Center) // 居中对齐
            ) {

                // --- 最终内容: 左侧大图区 ---
                Box(
                    modifier = Modifier
                        .width(1286.dp)
                        .height(657.dp)
                        .align(Alignment.CenterStart) // 在“内容组合容器”中居左
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    ) { pageIndex ->
                        Image(
                            painter = painterResource(id = items[pageIndex].bigImageResId),
                            contentDescription = items[pageIndex].name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Row( // 页码指示器
                        Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                            Box(modifier = Modifier.padding(horizontal = 4.dp).clip(CircleShape).background(color).size(12.dp))
                        }
                    }
                }

                // --- 最终内容: 右侧按钮区 ---
                Column(
                    modifier = Modifier
                        .width(328.dp)
                        .height(657.dp) //【关键】使其与左侧大图容器等高
                        .align(Alignment.CenterEnd), // 在“内容组合容器”中居右
                    verticalArrangement = Arrangement.SpaceBetween, //【关键】因为有了固定高度，所以能完美均分
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items.forEach { item ->
                        GuideButton(
                            text = item.name,
                            iconResId = item.iconResId,
                            isSelected = (item.name == selectedItem.name),
                            onClick = { onItemSelected(item) }
                        )
                    }
                }
            }
        }
    }
}

// ... GuideButton Composable 保持不变...

// 抽离出的可复用按钮组件
@Composable
private fun GuideButton(
    text: String,
    iconResId: Int,
    isSelected: Boolean, // 1. 【补上】是否选中的布尔值参数
    onClick: () -> Unit      // 2. 【补上】点击事件的回调函数参数
) {
    // 3. 【补上】根据 isSelected 状态决定是否有边框
    val border = if (isSelected) BorderStroke(4.dp, Color(0xFF4C87FF)) else null

    Button(
        onClick = onClick, // 4. 【补上】将 onClick 回调传递给真正的 Button 组件
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        border = border, // 5. 【补上】应用边框效果
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = listOf(Color(0xFFE3EEFF), Color(0xFFC9D8FF)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = text,
                    modifier = Modifier.size(65.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text,
                    color = Color.DarkGray,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 预览函数，方便您在Android Studio中直接查看UI效果，无需运行到真机
@Preview(widthDp = 1920, heightDp = 1080)
@Composable
fun GuideContentScreenPreview() {
    ZhiyunAgentRobotTheme {
        // 为了在预览中看到效果，我们把它包在一个Box里，并给一个背景色
        Box(modifier = Modifier.background(Color(color = 0xFFF0F4FF))) {
            // 为预览函数提供临时的、“写死的”参数
            GuideContentScreen(
                items = guideUiItems, // 1. 【补上】使用我们定义好的假数据列表
                selectedItem = guideUiItems.first(), // 2. 【补上】默认选中第一项
                onItemSelected = { } // 3. 【补上】提供一个空的点击回调，因为在预览中点击无效
            )
        }
    }
}



