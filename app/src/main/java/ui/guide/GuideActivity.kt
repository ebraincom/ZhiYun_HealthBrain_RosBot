// =================================================================================
// 文件路径: app/src/main/java/com/zhiyun/agentrobot/ui/guide/GuideActivity.kt
// 【真正完整版 - 融合UI】 - 这个文件是为您量身定制的，请用它替换。
// =================================================================================
package com.zhiyun.agentrobot.ui.guide

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.zhiyun.agentrobot.MyApplication
import com.zhiyun.agentrobot.data.UserProfile
import com.zhiyun.agentrobot.data.roleAssistant
import com.zhiyun.agentrobot.data.roleData
import com.zhiyun.agentrobot.data.roleDoctor
import com.zhiyun.agentrobot.ui.common.AppScaffold
import com.zhiyun.agentrobot.ui.dialogs.RoleSelectionDialog
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import com.zhiyun.agentrobot.util.CameraEngine
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zhiyun.agentrobot.viewmodel.FaceExpressionViewModel
import android.content.Intent
import androidx.compose.ui.platform.LocalContext // 如果之前没有的话
import com.zhiyun.agentrobot.ui.aiphoto.AiphotoActivity
import android.content.Context



/**
 * 【v1.1·标准化改造版】
 * 彻底改造摄像头的调用方式，遵循最新的CameraEngine标准！
 */

class GuideActivity : ComponentActivity() {
    private val TAG = "GuideActivity_V2.0_Final" // 【升级】最终语音胜利版TAG

    // 【修改点 1-B：引入我们强大的新 ViewModel 指挥官！】
    // =================================================================================
    private val faceViewModel: FaceExpressionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: Activity is being created.")

        // ▼▼▼【核心修正1】: 从 onCreate 中彻底移除所有与 CameraEngine 回调相关的旧代码！▼▼▼
        // CameraEngine.instance.setOnPhotoTakenListener { ... }  <--- 这行以及其内容块必须删除！

        setContent {
            ZhiyunAgentRobotTheme {

                // 调用包含完整UI和新逻辑的组合函数
                GuidePageWithFinalLogic()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // onPause 时停止CameraEngine不再是最佳实践，因为拍照是异步的。
        // CameraEngine 会在拍照完成后自行停止。
        // CameraEngine.instance.stop() // <-- 建议移除此行，以防中断正在进行的拍照
    }

    @Composable
    private fun GuidePageWithFinalLogic() {
        val context = LocalContext.current
        var showRoleSelectionDialog by remember { mutableStateOf(false) }
        var currentSelectedItem by remember { mutableStateOf(guideUiItems.first()) }
        val currentUserProfile = UserProfile(name = "王阿姨")
        // 【修改点 2-A：建立观察哨，监听 ViewModel 的状态变化】
        // =================================================================================
        val statusText by faceViewModel.statusText.collectAsState()
        val capturedBitmap by faceViewModel.capturedFace.collectAsState()
        // 当状态文本包含“正在” 或 已经成功捕获到图片时，显示弹窗
        if (statusText.contains("正在") || capturedBitmap != null) {
            EmoticonCreationDialog(
                statusText = statusText,
                bitmap = capturedBitmap,
                onDismiss = {
                    // 如果需要允许用户手动关闭弹窗并重置状态，可以在这里调用ViewModel的重置方法
                    // 例如: faceViewModel.resetState()
                }
            )
        }
        // 【UI逻辑保持不变】
        if (showRoleSelectionDialog) {
            val rolesForSelection = listOfNotNull(roleAssistant, roleData, roleDoctor).filter {
                it.name == "智芸康养小助手" || it.name == "智芸数据"
            }
            RoleSelectionDialog(
                onDismissRequest = { showRoleSelectionDialog = false },
                onRoleSelected = { selectedRole ->
                    showRoleSelectionDialog = false
                    val app = context.applicationContext as? MyApplication
                    app?.appAgent?.let {
                        it.setPersona(selectedRole.persona)
                        it.setObjective(selectedRole.objective)
                    }
                    finish()
                },
                rolesToDisplay = rolesForSelection
            )
        }

        AppScaffold(
            userProfile = currentUserProfile,
            onUserProfileClick = {
                Toast.makeText(context, "点击了用户头像", Toast.LENGTH_SHORT).show()
            },
            onMoreConsultClick = { showRoleSelectionDialog = true },
            onGuideClick = {
                Toast.makeText(context, "当前已在导览页面", Toast.LENGTH_SHORT).show()
            },
            onOneTouchSosClick = {
                Toast.makeText(context, "点击了一键呼叫", Toast.LENGTH_SHORT).show()
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        GuideContentScreen(
                            modifier = Modifier.fillMaxSize(),
                            items = guideUiItems,
                            selectedItem = currentSelectedItem,
                            onItemSelected = { selectedItem ->
                                currentSelectedItem = selectedItem
                            },
                            // =================================================================================
                            // 【修改点 2-B：连接总攻指令！将按钮点击绑定到 ViewModel！】
                            // =================================================================================
                            onPhotoClick = {
                                Log.d(TAG, "“表情包合影”按钮点击，命令ViewModel开始总攻！")
                                faceViewModel.startFaceCaptureProcess()
                            }
                        )
                    }
                }
            }
        )
    }

    @Composable
    fun EmoticonCreationDialog(
        statusText: String,
        bitmap: android.graphics.Bitmap?,
        onDismiss: () -> Unit
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .size(300.dp)
                    .background(
                        Color.White,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (bitmap != null) {
                    // 如果成功捕获图片，就显示图片
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "捕获的人脸",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // 如果正在检测/拍照，就显示加载动画
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = statusText,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}