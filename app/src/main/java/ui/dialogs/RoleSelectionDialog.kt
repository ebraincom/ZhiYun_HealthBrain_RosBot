package com.zhiyun.agentrobot.ui.dialogs // 确认包名是新建的 dialogs 包

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zhiyun.agentrobot.data.Role

/**
 * 角色选择对话框
 * @param onDismissRequest 当用户请求关闭对话框时调用（例如点击对话框外部或按返回键）
 * @param onRoleSelected 当用户点击并选择一个角色时调用
 */
@Composable
fun RoleSelectionDialog(
    onDismissRequest: () -> Unit,
    onRoleSelected: (Role) -> Unit,
    rolesToDisplay: List<Role>
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .widthIn(max = 450.dp) // 限制对话框最大宽度，使其在屏幕上更美观
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface // 使用主题的表面颜色作为背景
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "选择咨询角色",
                    style = MaterialTheme.typography.headlineSmall, // 使用主题定义的标题样式
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 使用 LazyColumn 高效地显示角色列表
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp) // 设置每个选项之间的垂直间距
                ) {
                    items(rolesToDisplay) { role ->
                        RoleItem(role = role, onRoleClick = {
                            onRoleSelected(role) // 当角色被点击时，调用回调函数
                        })
                    }
                }
            }
        }
    }
}

/**
 * 对话框中的单个角色选项
 * @param role 要显示的角色数据
 * @param onRoleClick 当此选项被点击时的回调
 */
@Composable
private fun RoleItem(role: Role, onRoleClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRoleClick), // 使整个卡片可点击
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // 添加轻微的阴影效果
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // 使用比背景稍亮的颜色
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = role.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = role.persona.substringBefore("。") + "...", // 只显示角色的第一句话作为简介
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 2 // 最多显示两行
            )
        }
    }
}

