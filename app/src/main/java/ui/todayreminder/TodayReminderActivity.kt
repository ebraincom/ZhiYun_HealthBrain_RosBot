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
import java.util.regex.Pattern


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
                    desc = "当用户想要创建一个今天的提醒事项时，调用此Action。例如'下午三点开会'或'记得取快递'。", // ✅ 9. Action描述已更新
                    parameters = listOf(
                        // ✅ 10. 【业务适配】参数简化为当天提醒所需的字段
                        Parameter(
                            "reminder_content",
                            ParameterType.STRING,
                            "提醒的具体内容是什么",
                            true
                        ),
                        Parameter(
                            "reminder_details",
                            ParameterType.STRING,
                            "对提醒内容的补充说明，可为空",
                            false
                        ),
                        Parameter(
                            "reminder_time_points",
                            ParameterType.STRING,
                            "提醒的具体时间点，例如'早上8点'、'17:50'、'晚上'",
                            true
                        ),
                        Parameter(
                            "stop_condition",
                            ParameterType.STRING,
                            "提醒的停止条件，例如'24点'或'晚上12点'，可为空",
                            false
                        )
                    ),
                    executor = object : ActionExecutor {
                        override fun onExecute(action: Action, params: Bundle?): Boolean {
                            // ✅ 11. 参数解析已更新
                            val content = params?.getString("reminder_content") ?: "未指定事项"
                            val details = params?.getString("reminder_details") ?: ""
                            val timePoints =
                                params?.getString("reminder_time_points") ?: "未指定时间"
                            // val stopCondition = params?.getString("stop_condition")


                            lifecycleScope.launch {
                                Log.d(
                                    TAG,
                                    "V1.0: Now on MAIN thread, calling viewModel.addReminder..."
                                )
                                // ✅ 调用ViewModel的addReminder方法
                                viewModel.addReminder(content, details, timePoints, null)

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
            .registerAction(
                Action(
                    name = "com.zhiyun.agentrobot.DELETE_COMPLETED_TODAY_REMINDERS",
                    displayName = "删除所有已完成的当天提醒",
                    desc = "当用户想要清除所有已经提醒过的当天事项时调用此Action。例如：'删除所有已提醒的事项'、'把提醒过的都删了'。",
                    // 这个Action不需要额外参数
                    parameters = emptyList(),
                    executor = object : ActionExecutor {
                        override fun onExecute(action: Action, params: Bundle?): Boolean {
                            // 严格遵从您的项目规范，使用lifecycleScope
                            lifecycleScope.launch {
                                Log.d(TAG, "V2.2: 接到语音批量删除指令...")
                                // 命令ViewModel执行批量删除逻辑
                                val deletedCount = viewModel.deleteAllCompleted()

                                withContext(Dispatchers.IO) {
                                    val ttsMessage = when {
                                        deletedCount > 0 -> "好的，已经帮您清除了${deletedCount}条已完成的提醒。"
                                        else -> "好的，目前没有已完成的提醒可以清除。"
                                    }
                                    AgentCore.tts(ttsMessage)
                                    // 严格遵从您的项目规范，调用notify并设置isTriggerFollowUp
                                    action.notify(isTriggerFollowUp = true)
                                }
                            }
                            return true
                        }
                    }
                )
            )
        Log.i(TAG, ">>> V1.0: PageAgent configuration complete!")

        setContent {
            ZhiyunAgentRobotTheme {
                val reminders by viewModel.reminders.collectAsState()
                LaunchedEffect(Unit) {
                    viewModel.alarmEventFlow.collect { item ->
                        setTodayAlarm(item) // ✅ 14. 调用新的设置闹钟方法
                    }
                }
                // ✅ 15. 调用新的Screen Composable，并传入正确的参数
                TodayReminderScreen(
                    userProfile = UserProfile(name = "总司令", avatarUrl = null),
                    reminders = reminders,
                    onBack = { finish() },
                    onTodayReminderClick = { /* TODO: 触发语音交互 */ },
                    onDeleteClick = { reminderId ->
                        Log.d(TAG, "V2.2: Activity收到手动删除指令，准备删除ID: $reminderId")
                        // 命令ViewModel按ID删除
                        viewModel.deleteReminderById(reminderId)
                    }
                )
            }
        }
        Log.d(TAG, "[Activity] V1.0 onCreate finished.")
        // ✅ 16. 注册广播的Action名称已更新
        val filter = IntentFilter("com.zhiyun.agentrobot.ACTION_UPDATE_TODAY_STATUS")
        registerReceiver(statusUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        Log.d(TAG, "V1.0: 状态更新广播接收器已安全注册！")
    }
    private fun setTodayAlarm(item: TodayReminderItem) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val totalCount = 3
        val intervalMillis = 3 * 60 * 1000L
        // ✅ 18. Intent指向新的TodayAlarmReceiver
        val intent = Intent(this, TodayReminderAlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", item.id)
            // ✅ 19. 传递的参数已更新为通用内容
            putExtra("REMINDER_CONTENT", item.content)
            putExtra("REMINDER_DETAILS", item.details)
            putExtra("TOTAL_COUNT", totalCount)
            putExtra("INTERVAL_MILLIS", intervalMillis)
            action = "TODAY_REMINDER_ACTION_${item.id}"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = getNextTriggerTime(item.reminderTimePoints)

        if (triggerAtMillis == null) {
            Log.e(TAG, "V1.0 无法解析提醒时间: ${item.reminderTimePoints}，闹钟设置失败！")
            AOCoroutineScope.launch { AgentCore.tts("抱歉，我没能听懂您说的提醒时间，请再说一次。") }
            return
        }
        val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        Log.i(TAG, "【真实时间】V1.0 [第1次] 闹钟将于: ${calendar.time} 触发，提醒: ${item.content}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
        Log.i(TAG, ">>>【真实时间】V1.0 [第1次] 闹钟设置成功！<<<")
    }

    // 时间解析引擎完全复用，无需修改
    private fun getNextTriggerTime(reminderTimes: String): Long? {
        Log.d(TAG, "V1.0 时间引擎开始解析: '$reminderTimes'")
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
        AOCoroutineScope.launch { AgentCore.tts("这里是当天提醒页，您可以让我帮您记录今天的各种待办事项。") }
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


