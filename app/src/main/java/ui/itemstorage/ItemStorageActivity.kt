// ✅【【【这是强化了UI反馈和主动引导的、最终的 ItemStorageActivity.kt 文件！！！】】】
package com.zhiyun.agentrobot.ui.itemstorage

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState // ✅ UI反馈的关键导入
import androidx.compose.runtime.getValue      // ✅ UI反馈的关键导入

import com.ainirobot.agent.AgentCore
import com.ainirobot.agent.PageAgent
import com.ainirobot.agent.action.Action
import com.ainirobot.agent.action.ActionExecutor
import com.ainirobot.agent.action.Actions
import com.ainirobot.agent.base.Parameter
import com.ainirobot.agent.base.ParameterType
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import com.zhiyun.agentrobot.data.UserProfile
import com.ainirobot.agent.coroutine.AOCoroutineScope
import kotlinx.coroutines.launch

class ItemStorageActivity : ComponentActivity() {

    private val TAG = "ItemStorage_FINAL_DEBUG"
    private val viewModel: ItemStorageViewModel by viewModels()
    private lateinit var pageAgent: PageAgent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, ">>> [Activity] onCreate: Starting ATOMIC PageAgent configuration...")

        try {
            pageAgent = PageAgent(this)
                .blockAllActions()
                .setObjective("这个页面的目标是记录用户存放的物品信息，并能回答用户的查询。")
                .registerAction(
                    Action(
                        name = "com.zhiyun.agentrobot.SAVE_ITEM_INFO",
                        displayName = "保存物品信息",
                        desc = "当用户想要记录某个物品存放在哪里时，调用此Action来保存信息。",
                        parameters = listOf(
                            Parameter("item_name", ParameterType.STRING, "要保存的物品名称", true),
                            Parameter("location", ParameterType.STRING, "物品存放的具体位置", true)
                        ),
                        executor = object : ActionExecutor {
                            override fun onExecute(action: Action, params: Bundle?): Boolean {
                                Log.i(TAG, ">>> ACTION TRIGGERED: onExecute for SAVE_ITEM_INFO is running! <<<")
                                AOCoroutineScope.launch {
                                    val itemName = params?.getString("item_name") ?: "未知物品"
                                    val location = params?.getString("location") ?: "未知位置"
                                    viewModel.addStorageItem("$itemName 在 $location")
                                    AgentCore.tts("好的，我记住了 $itemName 在 $location。")

                                    // ✅ 【【【主动引导的关键修正！！！】】】
                                    action.notify(isTriggerFollowUp = true)
                                    Log.i(TAG, ">>> notify(isTriggerFollowUp = true) called!")
                                }
                                return true
                            }
                        }
                    )
                )
                .registerAction(Actions.SAY)
                .registerAction(Actions.EXIT)

            Log.i(TAG, ">>> V-I-C-T-O-R-Y!!!: ATOMIC PageAgent configuration complete without crash!")

        } catch (e: Exception) {
            Log.e(TAG, "!!!!!! CRITICAL FAILURE during ATOMIC configuration in onCreate!", e)
        }

        setContent {
            ZhiyunAgentRobotTheme {
                val items by viewModel.storageItems.collectAsState()

                ItemStorageScreen(
                    userProfile = UserProfile(name = "总司令", avatarUrl = null),
                    items = items, // ✅ 现在这里不再报错！
                    onBack = { finish() }
                )
            }
        }
        Log.d(TAG, "[Activity] onCreate finished.")
    }

    // onStart() 和 onDestroy() 保持不变
    override fun onStart() {
        super.onStart()
        Log.i(TAG, ">>> [Activity] onStart: Page becomes VISIBLE. Performing AgentCore tasks...")
        AgentCore.clearContext()
        AgentCore.tts("这里是物品存放页。")
        Log.i(TAG, ">>> onStart: AgentCore tasks dispatched.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, ">>> [Activity] onDestroy: Cleaning up session.")
        if (::pageAgent.isInitialized) {
            pageAgent.destroy()
        }
    }
}
