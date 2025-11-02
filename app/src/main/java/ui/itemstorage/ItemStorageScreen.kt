package com.zhiyun.agentrobot.ui.itemstorage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiyun.agentrobot.R
import com.zhiyun.agentrobot.data.UserProfile
import com.zhiyun.agentrobot.ui.common.AppScaffold
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.lazy.grid.GridCells // ✅【【【关键导入1/3：导入GridCells！！！】】】
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // ✅【【【关键导入2/3：导入LazyVerticalGrid！！！】】】
import androidx.compose.foundation.lazy.grid.items // ✅【【【关键导入3/3：导入Grid专用的items！！！】】】
import androidx.compose.foundation.layout.ExperimentalLayoutApi // ✅【【【关键导入1/2：导入FlowRow所需的API！！！】】】
import androidx.compose.foundation.layout.FlowRow             // ✅【【【关键导入2/2：导入FlowRow！！！】】】


// ------------------------------------
// 主屏幕 Composable
// ------------------------------------

@Composable
fun ItemStorageScreen(
    userProfile: UserProfile, // 从AppScaffold透传下来
    items: List<String>,      // ✅【【【关键修正：添加items参数！！！】】】
    onBack: () -> Unit,
    // 其他未来可能需要的ViewModel和事件
) {
    // 1. 使用我们强大的通用脚手架搭建页面
    AppScaffold(
        userProfile = userProfile,
        onGuideClick = onBack, // 暂时将导览按钮作为返回按钮
        // TODO: 将来这里的onGuideClick需要替换为真正的返回按钮逻辑
        content = {
            // 2. 在内容区构建我们的核心UI
            StorageContent(items = items)
        }
    )
}

// ------------------------------------
// 核心内容区
// ------------------------------------
@OptIn(ExperimentalLayoutApi::class) // ✅【【【关键修正1/3：启用FlowRow的实验性API！！！】】】
@Composable
private fun StorageContent(
    items: List<String>
) {
    // ✅【【【关键修正2/3：使用 FlowRow 替换 LazyVerticalGrid ！！！】】】
    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 167.dp)
            .padding(horizontal = 74.dp), // 为整个布局区设置水平边距
        horizontalArrangement = Arrangement.spacedBy(24.dp), // 设置卡片之间的水平间距
        verticalArrangement = Arrangement.spacedBy(24.dp)   // 设置卡片之间的垂直间距
    ) {
        // ✅【【【关键修正3/3：在FlowRow中直接放置所有卡片！！！】】】
        // 第一个：固定的交互卡片
        InteractionCard(
            onStartClick = { /* TODO: 实现语音交互逻辑 */ }
        )

        // 后续：遍历数据显示记忆卡片
        items.forEach { itemText ->
            val currentTime = remember {
                SimpleDateFormat("yyyy/M/d HH:mm", Locale.getDefault()).format(Date())
            }
            MemoryCard(
                item = StorageItem(id = UUID.randomUUID().toString(), content = itemText, dateTime = "$currentTime 创建")
            )
        }
    }
}

// ------------------------------------
// 两种核心卡片 Composable
// ------------------------------------

/**
 * 绿色的固定交互卡片
 */
@Composable
private fun InteractionCard(
    modifier: Modifier = Modifier,
    onStartClick: () -> Unit
) {
    Card(
        modifier = modifier
            .width(566.dp)
            .height(324.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F8F3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_storage_placeholder), // TODO: 替换为正确的图标
                contentDescription = "存储物品位置",
                modifier = Modifier.size(100.dp) // 示例尺寸
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "请告诉我您存放的物品位置",
                fontSize = 24.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            // 语音点击按钮
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .width(175.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp), // 高度的一半，形成完美半圆形
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_voice_mic), // 您已提供的图片
                    contentDescription = "开始交流",
                    tint = Color(0xFF67DBC2),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

/**
 * 白色的动态记忆卡片
 */
@Composable
private fun MemoryCard(
    modifier: Modifier = Modifier,
    item: StorageItem
) {
    Card(
        modifier = modifier
            .width(566.dp)
            .height(324.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)) {
            // 顶部：时间和编辑按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.dateTime,
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                Text(
                    text = "编辑",
                    fontSize = 18.sp,
                    color = Color(0xFF4A90E2),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* TODO: 实现编辑逻辑 */ }
                )
            }
            // 中间：主要内容，使用Box和weight实现居中
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = item.content,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    lineHeight = 40.sp
                )
            }
        }
    }
}


// ------------------------------------
// 预览函数
// ------------------------------------
@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun ItemStorageScreenPreview() {
    ZhiyunAgentRobotTheme {
        ItemStorageScreen(
            userProfile = UserProfile(name = "王阿姨",avatarUrl = null),
            items = listOf("笔记本电脑 在 桌子上", "车钥匙 在 门背后的挂钩上"), // ✅ 为预览提供示例数据，修复编译错误
            onBack = {}
        )
    }
}
