// ✅✅✅【【【【 这是 V1.0 当天提醒 Activity：遵从最高指示，100%精确复刻最终版！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.todayreminder // ✅ 1. 包名已更新

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.ainirobot.agent.AgentCore
import com.ainirobot.agent.PageAgent
import com.ainirobot.agent.action.Action
import com.ainirobot.agent.action.ActionExecutor
import com.ainirobot.agent.action.Actions
import com.ainirobot.agent.base.Parameter
import com.ainirobot.agent.base.ParameterType
import com.ainirobot.agent.coroutine.AOCoroutineScope
import com.zhiyun.agentrobot.data.UserProfile
import com.zhiyun.agentrobot.ui.theme.ZhiyunAgentRobotTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

// ✅ 2. 类名已更新
class TodayReminderActivity : ComponentActivity() {

    private val TAG = "TodayReminder_ACT_V1.0" // ✅ 3. 日志TAG已更新

    // ✅ 4. ViewModel类型已更新为TodayReminderViewModel
    private val viewModel: TodayReminderViewModel by viewModels()

    private lateinit var pageAgent: PageAgent

    // ✅ 5. 广播接收器监听的Action名称已更新
    private val statusUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.zhiyun.agentrobot.ACTION_UPDATE_TODAY_STATUS") {
                val reminderId = intent.getStringExtra("REMINDER_ID")
                if (reminderId != null) {
                    Log.d(TAG, "V1.0: Activity收到状态更新广播，通知ViewModel...")
                    viewModel.markAsReminded(reminderId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, ">>> [Activity] V1.0 onCreate: Starting...")

        pageAgent = PageAgent(this)
            .blockAllActions()
            .setObjective("这个页面的目标是帮助用户记录和管理今天之内的提醒事项。") // ✅ 6. Objective描述已更新
            .registerAction(
                Action(
                    name = "com.zhiyun.agentrobot.CREATE_TODAY_REMINDER", // ✅ 7. Action名称已更新
                    displayName = "新增当天提醒", // ✅ 8. Action显示名称已更新
                    desc = "当用户想要创建一个仅限今天的提醒事项时，调用此Action。例如'下午三点开会'或'记得取快递'。", // ✅ 9. Action描述已更新
                    parameters = listOf(
                        // ✅ 10. 【业务适配】参数简化为当天提醒所需的字段
                        Parameter("reminder_content", ParameterType.STRING, "提醒的具体内容是什么", true),
                        Parameter("reminder_details", ParameterType.STRING, "对提醒内容的补充说明，可为空", false)
                    ),
                    executor = object : ActionExecutor {
                        override fun onExecute(action: Action, params: Bundle?): Boolean {
                            // ✅ 11. 参数解析已更新
                            val content = params?.getString("reminder_content") ?: "未指定事项"
                            val details = params?.getString("reminder_details") ?: ""

                            lifecycleScope.launch {
                                Log.d(TAG, "V1.0: Now on MAIN thread, calling viewModel.addReminder...")
                                // ✅ 12. 调用ViewModel的addReminder方法
                                viewModel.addReminder(content, details)

                                withContext(Dispatchers.IO) {
                                    // ✅ 13. TTS播报内容已更新
                                    val ttsMessage = "好的，今天的提醒事项，$content，我记下了。"
                                    AgentCore.tts(ttsMessage)
                                    action.notify(isTriggerFollowUp = true)
                                    Log.d(TAG, "V1.0: TTS and Notify completed on IO thread.")
                                }
                            }
                            Log.d(TAG, "V1.0: onExecute immediately returns true.")
                            return true
                        }
                    }
                )
            )
            .registerAction(Actions.SAY)
            .registerAction(Actions.EXIT)
        Log.i(TAG, ">>> V1.0: PageAgent configuration complete!")


        setContent {
            ZhiyunAgentRobotTheme {
                val reminders by viewModel.reminders.collectAsState()

                // 【业务适配】当天提醒的闹钟设置逻辑被简化，无需单独的Flow

                // ✅ 14. 调用新的Screen Composable，并传入正确的参数
                TodayReminderScreen(
                    userProfile = UserProfile(name = "总司令", avatarUrl = null),
                    reminders = reminders,
                    onBack = { finish() },
                    onTodayReminderClick = {
                        // TODO: 触发语音交互，例如提示用户“请说您要提醒的事项”
                        AOCoroutineScope.launch { AgentCore.tts("请说您要提醒的事项。") }
                    }
                )
            }
        }
        Log.d(TAG, "[Activity] V1.0 onCreate finished.")
        // ✅ 15. 注册广播的Action名称已更新
        val filter = IntentFilter("com.zhiyun.agentrobot.ACTION_UPDATE_TODAY_STATUS")
        registerReceiver(statusUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        Log.d(TAG, "V1.0: 状态更新广播接收器已安全注册！")
    }

    // 【业务适配】当天提醒的闹钟设置逻辑被简化，不需要复杂的setPlanAlarm和时间解析引擎
    // 因为业务逻辑是“只记录当天的提醒事项”，不涉及具体时间点的闹钟触发

    override fun onStart() {
        super.onStart()
        Log.i(TAG, ">>> [Activity] onStart: Page becomes VISIBLE.")
        AgentCore.clearContext()
        // ✅ 16. TTS欢迎语已更新
        AOCoroutineScope.launch { AgentCore.tts("这里是当天提醒页，您可以让我记下今天需要处理的事情。") }
        Log.i(TAG, ">>> onStart: AgentCore tasks dispatched.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, ">>> [Activity] onDestroy: Cleaning up session.")
        if (::pageAgent.isInitialized) {
            pageAgent.destroy()
        }
        unregisterReceiver(statusUpdateReceiver)
        Log.d(TAG, "V1.0: 状态更新广播接收器已注销。")
    }
}
