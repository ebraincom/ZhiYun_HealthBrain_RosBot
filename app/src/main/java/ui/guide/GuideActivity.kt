// 文件路径: app/src/main/java/.../ui/guide/GuideActivity.kt
package com.zhiyun.agentrobot.ui.guide

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.ainirobot.agent.AgentCore
import com.ainirobot.agent.PageAgent
import com.zhiyun.agentrobot.MyApplication
import com.zhiyun.agentrobot.data.UserProfile
import com.zhiyun.agentrobot.data.roleAssistant
import com.zhiyun.agentrobot.data.roleData
import com.zhiyun.agentrobot.data.roleDoctor
import com.zhiyun.agentrobot.ui.common.AppScaffold
import com.zhiyun.agentrobot.ui.dialogs.RoleSelectionDialog
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import com.zhiyun.agentrobot.util.CameraEngine
import com.ainirobot.agent.base.ActionStatus // 导入正确的类
import com.ainirobot.agent.OnActionStatusChangedListener // 导入状态监听接口


// 最终的、肃清了所有错误的版本
class GuideActivity : ComponentActivity() {

    private lateinit var pageAgent: PageAgent
    private val TAG = "GuideActivity_Final"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDependencies()
        setContent {
            ZhiyunAgentRobotTheme {
                GuidePageFinal()
            }
        }
    }

    private fun initDependencies() {
        pageAgent = PageAgent(this).apply {
            setObjective("这是一个纯粹的UI展示与操作页面，请保持绝对静默，不要处理任何语音输入，也不要进行任何TTS播报。")
            // ▼▼▼【最终决战第二处：使用正确的接口实现方式设置监听器】▼▼▼
            setOnActionStatusChangedListener(object : OnActionStatusChangedListener {
                // 核心修正：所有参数都使用可空类型(String?)，与编译器提示完全一致
                override fun onStatusChanged(actionName: String?, status: String?, message: String?): Boolean {
                    // 只关心拍照Action的结果
                    if (actionName == "orion.vision.TAKE_PHOTO") {
                        // 回调在子线程，必须切回主线程更新UI
                        runOnUiThread {
                            when (status) {
                                ActionStatus.SUCCEEDED.name -> {
                                    Log.i(TAG, "VICTORY! 拍照Action成功! Message: $message")
                                    Toast.makeText(this@GuideActivity, "拍照成功！$message", Toast.LENGTH_LONG).show()
                                    AgentCore.tts("拍照成功！")
                                }
                                ActionStatus.FAILED.name -> {
                                    Log.e(TAG, "DEFEAT! 拍照Action失败. Message: $message")
                                    Toast.makeText(this@GuideActivity, "拍照失败: $message", Toast.LENGTH_LONG).show()
                                    AgentCore.tts("拍照失败，$message")
                                }
                                else -> {
                                    Log.d(TAG, "拍照Action状态更新: Status: $status, Message: $message")
                                }
                            }
                        }
                    }
                    return false // 返回false，不消费事件，继续传递给其他监听器
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        // ▼▼▼【最终修正第一处：删除不存在的API调用】▼▼▼
        // pageAgent.reportPageStarted() // 此API不存在，AgentOS会自动处理
        AgentCore.isDisablePlan = true
    }

    override fun onPause() {
        super.onPause()
        // ▼▼▼【最终修正第二处：删除不存在的API调用】▼▼▼
        // pageAgent.reportPageEnded() // 此API不存在，AgentOS会自动处理
    }

    // 您的UI函数 GuidePageFinal，100%原封不动地保留
    @Composable
    private fun GuidePageFinal() {
        val context = LocalContext.current
        var showRoleSelectionDialog by remember { mutableStateOf(false) }
        var currentSelectedItem by remember { mutableStateOf(guideUiItems.first()) }
        val currentUserProfile = UserProfile(name = "王阿姨")

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
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        GuideContentScreen(
                            modifier = Modifier.fillMaxSize(),
                            items = guideUiItems,
                            selectedItem = currentSelectedItem,
                            onItemSelected = { selectedItem ->
                                currentSelectedItem = selectedItem
                                if (selectedItem.name == "表情包合影") {
                                    handleExpressionPhotoClick()
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
                }
            }
        )
    }

    // 您的拍照处理方法 handleExpressionPhotoClick，100%原封不动地保留
    private fun handleExpressionPhotoClick() {
        val commands = listOf(
            "拍照",
            "拍张照",
            "给我拍张照",
            "执行拍照功能",
            "调用系统相机",
            "take photo" // 加入英文指令作为对照
        )
        val randomCommand = commands.random()

        Log.i(TAG, "ACTION: 发送指令 '$randomCommand'，请求大模型规划拍照Action")
        Toast.makeText(this, "正在请求系统拍照 (指令: $randomCommand)...", Toast.LENGTH_SHORT).show()

        AgentCore.query(randomCommand)
    }





    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "Activity is being destroyed.")
    }
}
