package com.zhiyun.agentrobot.ui.guide

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.zhiyun.agentrobot.ui.common.AppScaffold
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import com.zhiyun.agentrobot.data.UserProfile // 假设有一个通用的UserProfile数据类
import com.zhiyun.agentrobot.data.selectableRoles
import com.ainirobot.agent.PageAgent
import android.util.Log
import com.ainirobot.agent.AgentCore
// 【新增导入】: 在文件顶部的import区域，加入以下我们即将用到的组件
import com.zhiyun.agentrobot.ui.dialogs.RoleSelectionDialog
import com.zhiyun.agentrobot.ChatActivity // 【关键导入】
import android.content.Intent
import com.zhiyun.agentrobot.ui.guide.GuideContentScreen
import com.ainirobot.agent.OnTranscribeListener // 【新增导入】
import com.ainirobot.agent.action.Action // 【新增导入】
import com.ainirobot.agent.action.ActionExecutor // 【新增导入
import com.ainirobot.agent.action.Actions // 【新增导入】
import com.ainirobot.agent.base.Parameter // 【新增导入
import com.ainirobot.agent.base.ParameterType // 【新增导入】
import com.ainirobot.agent.base.Transcription // 【新增导入】
import com.zhiyun.agentrobot.MyApplication // 【新增导入】
import com.zhiyun.agentrobot.data.Role // 【新增导入】
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme

class GuideActivity : ComponentActivity() {
    private lateinit var pageAgent: PageAgent
    private val TAG = "GuideActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: Initializing as a 'Role Selection' page.")
        // 1. 初始化PageAgent (完全遵从官方做法)
        pageAgent = PageAgent(this)
        // pageAgent.blockAllActions()
        pageAgent.setObjective("我的首要目的是催促用户选择一个角色，进入体验")
        // 1. 注册核心的“选择角色”Action，赋予语音选角能力
        defineAndRegisterActions()


        // 2.【废除】不再在onCreate中尝试切换任何角色。
        //    此Activity的灵魂就是“引导选择”，而不是扮演某个角色。

        setContent {
            ZhiyunAgentRobotTheme {
                // 将pageAgent传递给UI层，尽管在这个新架构下，UI层几乎用不到它
                GuidePage()
            }
        }
    }
    // ▼▼▼【一：补全缺失的函数定义】
    private fun defineAndRegisterActions() {
        // 1. 定义语音选择角色的Action
        val selectRoleAction = Action(
            "com.zhiyun.action.SELECT_ROLE", // Action唯一ID
            "选择角色", // Action显示名
            "当用户说'选择某个角色'时，使用此工具来选择一个角色并进入对话", // 给大模型看的描述
            parameters = listOf(
                Parameter(
                    "role_name", // 参数名
                    ParameterType.STRING, // 参数类型
                    "用户想要选择的角色名称，必须是'智芸康养小助手', '医博士', '药博士'等中的一个", // 参数描述
                    true // 必须参数
                )
            ),
            executor = object : ActionExecutor {
                override fun onExecute(action: Action, params: Bundle?): Boolean {
                    // 【军规二】
                    val recognizedRoleName = params?.getString("role_name")?.trim() // 去除语音识别可能带来的空格
                    Log.i(TAG, "SELECT_ROLE Action triggered. Recognized name: '$recognizedRoleName'")

                    if (!recognizedRoleName.isNullOrEmpty()) {
                        // 【健壮性加固】使用包含(contains)而不是等于(==)来查找，提高模糊匹配成功率
                        val selectedRole = selectableRoles.find { it.name.contains(recognizedRoleName) }

                        if (selectedRole != null && (selectedRole.name == "智芸数据" || selectedRole.name == "医博士")) {
                            Log.i(TAG, "Permitted role '${selectedRole.name}' found! Injecting soul before launch...")

                            // ▼▼▼【核心：先注入灵魂，再启动肉体！】▼▼▼
                            val appAgent = (application as MyApplication).appAgent
                            appAgent.setPersona(selectedRole.persona)
                            appAgent.setObjective(selectedRole.objective)
                            Log.i(TAG, "SUCCESS: Soul of '${selectedRole.name}' has been injected into AppAgent.")

                            // 现在可以安全启动ChatActivity了
                            launchChatActivity(selectedRole)
                            // ▲▲▲【核心修正完毕】▲▲▲
                            // 启动聊天页后，立刻关闭当前的导览页，彻底杜绝白屏幽灵！
                            finish()
                            Log.i(TAG, "GuideActivity is finishing to prevent ghost screen.")

                        } else {
                            // 如果用户说了其他角色（比如智芸康养小助手），礼貌地拒绝
                            (application as MyApplication).safeTts("抱歉，在这个页面，我们只支持选择智芸数据或医博士。")
                            Log.w(TAG, "Role '$recognizedRoleName' is not a permitted choice on this page.")
                        }
                    } else {
                        (application as MyApplication).safeTts("抱歉，我没听清您想选择哪个角色。")
                    }
                    action.notify()
                    return true
                }
            }
        )
        pageAgent.registerAction(selectRoleAction)
        Log.i(TAG, "'com.zhiyun.action.SELECT_ROLE' Action has been registered.")

        // 2.【罪行二：使用100%正确的SAY Action引用】
        pageAgent.registerAction(Actions.SAY)
        Log.i(TAG, "The correct 'Actions.SAY' has been registered.")
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: Holding 'Role Selection' inauguration ceremony.")

        // 1. 清理战场，确保从一个干净的状态开始
        AgentCore.stopTTS()
        AgentCore.clearContext()

        // 2. 告知AgentCore当前页面的上下文是“角色列表”，以优化语音识别 (关键！)
        val roleInfoForNLU = "智芸数据\n医博士" // 只把允许选择的角色名传给NLU
        AgentCore.uploadInterfaceInfo(roleInfoForNLU)
        Log.d(TAG, "onStart: Uploaded PERMITTED role names to AgentCore for NLU optimization: [${roleInfoForNLU.replace("\n", ", ")}]")

        // 3. 设定为主动引导模式 (关键！)
        AgentCore.isDisablePlan = false
        Log.i(TAG, "onStart: Interaction mode set to ACTIVE (isDisablePlan=false).")

        // 4. 发表引导语 (如果需要的话，现在可以取消注释)
        // AgentCore.tts("请选择您要体验的角色。")
        val ttsMessage = "在这里，您可以选择体验我们的特色角色：智芸数据或医博士。请对我说，选择智芸数据。"
        (application as MyApplication).safeTts(ttsMessage)
        Log.i(TAG, "Exclusive role selection TTS guide sent: $ttsMessage")
        // ▲▲▲【修复完毕】▲▲▲
    }
    // ▼▼▼【精确安装位置：就是这里！】▼▼▼
    /**
     * 统一的、安全的启动ChatActivity的方法
     */
    fun launchChatActivity(role: Role) {
        Log.i(TAG, "Launching ChatActivity for role: '${role.name}'")
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("role", role)
        }
        startActivity(intent)
    }
    // ▲▲▲【安装完毕】▲▲▲

} // <--- GuideActivity 类的结束花括号




// ---【第三部分：Composable 函数 GuidePage】---
@Composable
fun GuidePage(pageAgent: PageAgent? = null) {
    val context = LocalContext.current
    var showRoleSelectionDialog by remember { mutableStateOf(false) }
    var currentSelectedItem by remember { mutableStateOf(guideUiItems.first()) }


    // 【重要】: 检查对话框是否应该显示
    if (showRoleSelectionDialog) {
        RoleSelectionDialog(
            rolesToDisplay = selectableRoles,
            onDismissRequest = { showRoleSelectionDialog = false },
            onRoleSelected = { selectedRole ->
                // ▼▼▼【最终的、决定性的、100%基于官方圣经的核心修正！】▼▼▼
                showRoleSelectionDialog = false
                Log.i("GuidePage", "User selected '${selectedRole.name}'. Preparing to launch ChatActivity.")

                // 1. 创建一个指向我们新的、专门聊天的 ChatActivity 的 Intent
                val intent = Intent(context, ChatActivity::class.java)

                // 2. 将用户选择的 Role 对象 (必须是 Parcelable) 打包到 Intent 中
                //    官方圣经中使用的 key 是 "role"
                intent.putExtra("role", selectedRole)

                // 3. 启动新的 Activity，将控制权完全交出去
                context.startActivity(intent)
                Log.i("GuidePage", "startActivity(ChatActivity) called successfully.")
                // ▲▲▲【核心修正结束】▲▲▲
            }
        )
    }

    val currentUserProfile = UserProfile(name = "王阿姨")
    ZhiyunAgentRobotTheme {
        AppScaffold(
            userProfile = currentUserProfile,
            onUserProfileClick = {
                Toast.makeText(context, "点击了用户头像", Toast.LENGTH_SHORT).show()
            },
            onOneTouchSosClick = {
                Toast.makeText(context, "点击了一键呼叫", Toast.LENGTH_SHORT).show()
            },
            onMoreConsultClick ={
                Toast.makeText(context, "请点击屏幕中央的选项进行选择", Toast.LENGTH_SHORT).show() },
            // ... (所有 on...Click 回调保持不变) ...
            onGuideClick = {
                Toast.makeText(context, "当前已在导览页面", Toast.LENGTH_SHORT).show()
            },

            content = { innerPadding ->
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. 顶部空白，等于顶部栏的高度
                    Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

                    // 2. 中间内容区，占据所有剩余空间
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // <--- 这是实现垂直居中的核心！
                    ) {
                        // 将我们的内容屏幕放入这个绝对安全的Box中
                        GuideContentScreen(
                            // 它只需要撑满这个Box即可
                            modifier = Modifier.fillMaxSize(),
                            items = guideUiItems,
                            selectedItem = currentSelectedItem,
                            onItemSelected = { selectedItem ->
                                currentSelectedItem = selectedItem
                                Toast.makeText(
                                    context,
                                    "切换到: ${selectedItem.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                if (selectedItem.name == "表情包合影") {
                                    Toast.makeText(
                                        context,
                                        "进入表情包合影功能模块",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                    // 3. 底部空白，等于底部栏的高度
                    Spacer(
                        modifier = Modifier.height(innerPadding.calculateBottomPadding())
                    )
                }
            }
        )
    }
}
@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun GuidePagePreview() {
    ZhiyunAgentRobotTheme {
        GuidePage()
    }
}