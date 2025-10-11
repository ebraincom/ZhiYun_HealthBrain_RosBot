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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.get
import com.zhiyun.agentrobot.data.selectableRoles
import com.ainirobot.agent.PageAgent
import android.app.Activity // 【关键导入1】: 需要导入Activity
import android.util.Log
import androidx.compose.ui.semantics.text
import com.ainirobot.agent.OnTranscribeListener
import com.ainirobot.agent.base.Transcription
import com.ainirobot.agent.base.Parameter
import com.ainirobot.agent.base.ParameterType
import com.zhiyun.agentrobot.MyApplication
import com.zhiyun.agentrobot.data.Role
import com.ainirobot.agent.AgentCore
import com.ainirobot.agent.coroutine.AOCoroutineScope
import kotlinx.coroutines.launch

class GuideActivity : ComponentActivity() {
    private lateinit var pageAgent: PageAgent
    private val zhiyunDataRole: Role? by lazy {
        selectableRoles.find { it.name == "智芸数据" }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageAgent = PageAgent(this)
        zhiyunDataRole?.let { role ->
            (applicationContext as MyApplication).switchAgentRole(role)// 只设定，不清除上下文
            Log.i("GuideActivity_LifeCycle", "onCreate: switchAgentRole called for ${role.name}")
        }
        setContent {
            ZhiyunAgentRobotTheme {
                GuidePage(pageAgent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i("GuideActivity_LifeCycle", "onStart: Clearing context and activating new role...")
        AgentCore.clearContext() // 清除上下文
        AgentCore.stopTTS()      // 停止可能正在播放的上一页面的TTS
        // 主动让机器人说一句开场白，以激活并展现新角色
        AOCoroutineScope.launch {
            // 增加一个小的延迟，确保 Agent 状态已完全就绪
            kotlinx.coroutines.delay(200)
            val openingLine = "您好，我是智芸数据全能助手，很高兴为您服务。"
            AgentCore.tts(openingLine)
            Log.i("GuideActivity_LifeCycle", "onStart: Opening line TTS sent.")
        }
    }

    override fun onDestroy() {
        super.onDestroy() // 首先调用父类的onDestroy

        // 从 applicationContext 中获取 MyApplication 实例
        val myApp = applicationContext as MyApplication
        val defaultRole = selectableRoles.find { it.name == "智芸康养小助手" }
        // 【核心】: 调用我们完美的 switchAgentRole 方法，但这次传入的是全局默认角色！
        // 这样就完成了灵魂的“恢复”操作。
        // myApp.switchAgentRole(myApp.defaultRole)
        if (defaultRole != null) {
            myApp.switchAgentRole(defaultRole)
            Log.i(
                "GuideActivity_LifeCycle",
                "onDestroy: Agent role restored to default '智芸康养小助手'."
            )
        } else {
            Log.w(
                "GuideActivity_LifeCycle",
                "onDestroy: Could not find the default role '智芸康养小助手' to restore."
            )
        }
    }
}


// ---【第三部分：Composable 函数 GuidePage】---
@Composable
fun GuidePage(pageAgent: PageAgent? = null) {
    val context = LocalContext.current
    val myApp = context.applicationContext as MyApplication
    // 1. 获取PageAgent实例
    val pageAgent = remember { PageAgent(context as Activity) }

    // 2.【精确制导】: 从全局数据源`selectableRoles`中，唯一地、精确地找出“智芸数据”角色
    val zhiyunDataRole = remember { selectableRoles.find { it.name == "智芸数据" } }

    // 3. 状态持有: UI的选中项，初始为列表第一个
    var currentSelectedItem by remember { mutableStateOf(guideUiItems.first()) }

    // 4.【灵魂注入核心】: 使用LaunchedEffect，在页面首次加载时，为Agent注入“智芸数据”的灵魂
    //    这个Effect只会在页面首次组合时运行一次，因为它的key(Unit)永远不会改变。
    LaunchedEffect(pageAgent) { // key 从 Unit 改为 pageAgent
        pageAgent?.setOnTranscribeListener(object : OnTranscribeListener {
            override fun onASRResult(transcription: Transcription): Boolean {
                if (transcription.final) {
                    (context as? Activity)?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "听到您说: ${transcription.text}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                return false
            }

            override fun onTTSResult(transcription: Transcription): Boolean {
                return false
            }
        })
        Log.i("GuideActivity_Compose", "OnTranscribeListener has been set.")
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
            onMoreConsultClick = {
                Toast.makeText(context, "点击了更多咨询", Toast.LENGTH_SHORT).show()
            },
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