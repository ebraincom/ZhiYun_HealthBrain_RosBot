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

/**
 * 【v1.1·标准化改造版】
 * 彻底改造摄像头的调用方式，遵循最新的CameraEngine标准！
 */

class GuideActivity : ComponentActivity() {
    private val TAG = "GuideActivity_V1.1" // 【升级】最终语音胜利版TAG

    // 权限请求器
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.i(TAG, "PERMISSION GRANTED after request. Proceeding to take photo.")
                startTakingPhoto()
            } else {
                Log.e(TAG, "PERMISSION DENIED. Cannot take photo.")
                Toast.makeText(this, "没有相机权限，无法拍照", Toast.LENGTH_LONG).show()
            }
        }

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
            onUserProfileClick = { Toast.makeText(context, "点击了用户头像", Toast.LENGTH_SHORT).show() },
            onMoreConsultClick = { showRoleSelectionDialog = true },
            onGuideClick = { Toast.makeText(context, "当前已在导览页面", Toast.LENGTH_SHORT).show() },
            onOneTouchSosClick = { Toast.makeText(context, "点击了一键呼叫", Toast.LENGTH_SHORT).show() },
            content = { innerPadding ->
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)) {
                        GuideContentScreen(
                            modifier = Modifier.fillMaxSize(),
                            items = guideUiItems,
                            selectedItem = currentSelectedItem,
                            onItemSelected = { selectedItem ->
                                currentSelectedItem = selectedItem
                            },
                            // 【职责2：动作回调】只负责响应拍照按钮的点击
                            onPhotoClick = {
                                Log.d(
                                    TAG,
                                    "onPhotoClick explicitly triggered. Calling handleExpressionPhotoClick."
                                )
                                handleExpressionPhotoClick()
                            }
                        )
                    }
                }
            }
        )
    }

    private fun handleExpressionPhotoClick() {
        Log.i(TAG, "handleExpressionPhotoClick: Checking permissions...")
        when {
            checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                Log.i(TAG, "CAMERA permission is already granted. Starting photo process directly.")
                startTakingPhoto()
            }
            else -> {
                Log.i(TAG, "CAMERA permission not granted. Requesting...")
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startTakingPhoto() {
        // ▼▼▼【核心修正1：战前语音动员，保持不变】▼▼▼
        val welcomeText = "请您面对摄像头，并保持姿势，以便我们获取您的头像"
        // (application as? MyApplication)?.safeTts(welcomeText) // 假设您有这个方法
        Log.i(TAG, "TTS requested: '$welcomeText'")
        Toast.makeText(this, "请看摄像头...", Toast.LENGTH_SHORT).show()

        Log.i(TAG, "All pre-conditions met. Commanding CameraEngine to start...")

        // ▼▼▼【核心修正2：使用帝国标准调用CameraEngine】▼▼▼
        // 不再使用.instance，并且适配新的takePicture接口
        CameraEngine.takePicture(this) { success, message, bitmap ->
            // 回调默认就在UI线程，无需runOnUiThread
            if (success && bitmap != null) {
                // ▼▼▼【核心修正3：胜利语音宣告，保持不变】▼▼▼
                val successText = "拍照成功，稍后获得表情包"
                // (application as? MyApplication)?.safeTts(successText)
                Log.i(TAG, "TTS requested: '$successText'")

                // ▼▼▼【核心修正4：使用正确的Toast语法】▼▼▼
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                Log.i(TAG, "VICTORY CONFIRMED! Message: $message, Bitmap acquired.")

                // 在这里，我们可以将获取到的bitmap用于生成表情包
                // 比如： handleEmoticonCreation(bitmap)

                // 使用完毕后，记得回收bitmap，防止内存泄漏
                bitmap.recycle()

            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                Log.e(TAG, "MISSION FAILED! Message: $message")
            }
        }
    }
}
