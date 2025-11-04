// ✅✅✅【【【【 这是 V1.5 计划提醒 Activity：遵从最高指示，终极总装版！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.planreminder // ✅ 1. 包名无变化

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
import java.util.regex.Pattern

// ✅ 2. 类名无变化
class PlanReminderActivity : ComponentActivity() {

    private val TAG = "PlanReminder_ACT_V1.5" // ✅ 3. 日志TAG已更新

    // ✅ 4. ViewModel类型已更新为PlanReminderViewModel
    private val viewModel: PlanReminderViewModel by viewModels()

    private lateinit var pageAgent: PageAgent

    // ✅ 5. 广播接收器监听的Action名称已更新
    private val statusUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.zhiyun.agentrobot.ACTION_UPDATE_PLAN_STATUS") {
                val reminderId = intent.getStringExtra("REMINDER_ID")
                if (reminderId != null) {
                    Log.d(TAG, "V1.5: Activity收到状态更新广播，通知ViewModel...")
                    viewModel.markAsReminded(reminderId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, ">>> [Activity] V1.5 onCreate: Starting...")

        try {
            pageAgent = PageAgent(this)
                .blockAllActions()
                .setObjective("这个页面的目标是帮助用户记录和管理各种计划提醒事项。") // ✅ 6. Objective描述已更新
                .registerAction(
                    Action(
                        name = "com.zhiyun.agentrobot.CREATE_PLAN_REMINDER", // ✅ 7. Action名称已更新
                        displayName = "新增计划提醒", // ✅ 8. Action显示名称已更新
                        desc = "当用户想要创建一个新的计划提醒事项时，调用此Action。例如提醒复查、检查煤气、接送孩子等。", // ✅ 9. Action描述已更新
                        parameters = listOf(
                            // ✅ 10. 参数已更新为通用场景
                            Parameter("reminder_content", ParameterType.STRING, "提醒的具体内容是什么，例如'去医院复查'或'检查煤气'", true),
                            Parameter("reminder_details", ParameterType.STRING, "对提醒内容的补充说明，例如'带好病历'，可为空", false),
                            Parameter("reminder_time_points", ParameterType.STRING, "提醒的具体时间点，例如'早上8点'、'17:50'、'每天晚上'", true),
                            Parameter("stop_condition", ParameterType.STRING, "提醒的停止条件，例如'下周'或'月底'，可为空", false)
                        ),
                        executor = object : ActionExecutor {
                            override fun onExecute(action: Action, params: Bundle?): Boolean {
                                // ✅ 11. 参数解析已更新
                                val content = params?.getString("reminder_content") ?: "未指定事项"
                                val details = params?.getString("reminder_details") ?: ""
                                val timePoints = params?.getString("reminder_time_points") ?: "未指定时间"
                                val stopCondition = params?.getString("stop_condition")

                                lifecycleScope.launch {
                                    Log.d(TAG, "V1.5: Now on MAIN thread, calling viewModel.addReminder...")
                                    // ✅ 12. 调用ViewModel的方法，参数已完全匹配V1.4版ViewModel
                                    viewModel.addReminder(content, details, timePoints, stopCondition)

                                    withContext(Dispatchers.IO) {
                                        // ✅ 13. TTS播报内容已更新
                                        val ttsMessage = "好的，我记住了，$timePoints 要提醒您，$content。"
                                        AgentCore.tts(ttsMessage)
                                        action.notify(isTriggerFollowUp = true)
                                        Log.d(TAG, "V1.5: TTS and Notify completed on IO thread.")
                                    }
                                }
                                Log.d(TAG, "V1.5: onExecute immediately returns true.")
                                return true
                            }
                        }
                    )
                )
                .registerAction(Actions.SAY)
                .registerAction(Actions.EXIT)
            Log.i(TAG, ">>> V1.5: PageAgent configuration complete!")
        } catch (e: Exception) {
            Log.e(TAG, "!!!!!! CRITICAL FAILURE during PageAgent configuration!", e)
        }

        setContent {
            ZhiyunAgentRobotTheme {
                val reminders by viewModel.reminders.collectAsState()
                LaunchedEffect(Unit) {
                    viewModel.alarmEventFlow.collect { item ->
                        setPlanAlarm(item) // ✅ 14. 调用新的设置闹钟方法
                    }
                }
                // ✅ 15. 调用新的Screen Composable，并传入正确的参数
                PlanReminderScreen(
                    userProfile = UserProfile(name = "总司令", avatarUrl = null),
                    reminders = reminders,
                    onBack = { finish() },
                    onPlanReminderClick = { /* TODO: 触发语音交互 */ }
                )
            }
        }
        Log.d(TAG, "[Activity] V1.5 onCreate finished.")
        // ✅ 16. 注册广播的Action名称已更新
        val filter = IntentFilter("com.zhiyun.agentrobot.ACTION_UPDATE_PLAN_STATUS")
        registerReceiver(statusUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        Log.d(TAG, "V1.5: 状态更新广播接收器已安全注册！")
    }

    // ✅ 17. 方法名和参数类型已更新
    private fun setPlanAlarm(item: PlanReminderItem) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val totalCount = 3
        val intervalMillis = 3 * 60 * 1000L
        // ✅ 18. Intent指向新的PlanAlarmReceiver
        val intent = Intent(this, PlanAlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", item.id)
            // ✅ 19. 传递的参数已更新为通用内容
            putExtra("REMINDER_CONTENT", item.content)
            putExtra("REMINDER_DETAILS", item.details)
            putExtra("TOTAL_COUNT", totalCount)
            putExtra("INTERVAL_MILLIS", intervalMillis)
            // ✅ 20. Intent的Action已更新
            action = "PLAN_REMINDER_ACTION_${item.id}"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = getNextTriggerTime(item.reminderTimePoints)

        if (triggerAtMillis == null) {
            Log.e(TAG, "V1.5 无法解析提醒时间: ${item.reminderTimePoints}，闹钟设置失败！")
            AOCoroutineScope.launch { AgentCore.tts("抱歉，我没能听懂您说的提醒时间，请再说一次。") }
            return
        }
        val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        Log.i(TAG, "【真实时间】V1.5 [第1次] 闹钟将于: ${calendar.time} 触发，提醒: ${item.content}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
        Log.i(TAG, ">>>【真实时间】V1.5 [第1次] 闹钟设置成功！<<<")
    }

    // 时间解析引擎完全复用，无需修改
    private fun getNextTriggerTime(reminderTimes: String): Long? {
        Log.d(TAG, "V1.5 时间引擎开始解析: '$reminderTimes'")
        val hPattern = Pattern.compile("(下午|晚上|上午|早上)?(\\d{1,2})[点时](半)?")
        val hMatcher = hPattern.matcher(reminderTimes)
        if (hMatcher.find()) {
            val period = hMatcher.group(1)
            var hour = hMatcher.group(2)?.toIntOrNull()
            val isHalf = hMatcher.group(3) != null
            if (hour != null) {
                if ((period == "下午" || period == "晚上") && hour < 12) {
                    hour += 12
                }
                if (period == "晚上" && hour == 24) hour = 0
                val minute = if (isHalf) 30 else 0
                return calculateTriggerMillis(hour, minute)
            }
        }
        val hhmmPattern = Pattern.compile("(\\d{1,2}):(\\d{2})")
        val hhmmMatcher = hhmmPattern.matcher(reminderTimes)
        if (hhmmMatcher.find()) {
            val hour = hhmmMatcher.group(1)?.toIntOrNull()
            val minute = hhmmMatcher.group(2)?.toIntOrNull()
            if (hour != null && minute != null) {
                return calculateTriggerMillis(hour, minute)
            }
        }
        Log.e(TAG, "V1.5 所有解析规则均未命中，返回 null")
        return null
    }

    private fun calculateTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val candidateTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return if (candidateTime.after(now)) {
            candidateTime.timeInMillis
        } else {
            candidateTime.add(Calendar.DAY_OF_YEAR, 1)
            candidateTime.timeInMillis
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, ">>> [Activity] onStart: Page becomes VISIBLE.")
        AgentCore.clearContext()
        // ✅ 21. TTS欢迎语已更新
        AOCoroutineScope.launch { AgentCore.tts("这里是计划提醒页，您可以让我帮您记录各种待办事项。") }
        Log.i(TAG, ">>> onStart: AgentCore tasks dispatched.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, ">>> [Activity] onDestroy: Cleaning up session.")
        if (::pageAgent.isInitialized) {
            pageAgent.destroy()
        }
        unregisterReceiver(statusUpdateReceiver)
        Log.d(TAG, "V1.5: 状态更新广播接收器已注销。")
    }
}
