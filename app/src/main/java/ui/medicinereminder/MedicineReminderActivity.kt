// ✅【【【这是完整的 MedicineReminderActivity.kt 文件！！！】】】
package com.zhiyun.agentrobot.ui.medicinereminder // ✅ 确保包名正确！

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.launch
// ✅ 1. 关键修改点：导入设置闹钟所必需的3个类
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import java.util.Calendar
// ✅ 导入结束

class MedicineReminderActivity : ComponentActivity() {

    private val TAG = "MedicineReminder_ACT"
    private val viewModel: MedicineReminderViewModel by viewModels()
    private lateinit var pageAgent: PageAgent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, ">>> [Activity] onCreate: Starting ATOMIC PageAgent configuration...")

        try {
            // 严格遵循《PageAgent终极开发纲要》进行原子化配置
            pageAgent = PageAgent(this)
                .blockAllActions()
                .setObjective("这个页面的目标是帮助用户记录和管理服药提醒。")
                .registerAction(
                    Action(
                        name = "com.zhiyun.agentrobot.CREATE_DRUG_REMINDER",
                        displayName = "新增用药事项",
                        desc = "当用户想要创建一个新的服药提醒时，调用此Action。需要包含药物名称、服用说明、提醒时间和停止日期。",
                        parameters = listOf(
                            Parameter("drug_name", ParameterType.STRING, "要服用的药物名称", true),
                            Parameter("dosage_instruction", ParameterType.STRING, "服用说明，例如每次几片", true),
                            Parameter("reminder_times", ParameterType.STRING, "提醒的频率和具体时间，例如每日早8点晚8点", true),
                            Parameter("stop_date", ParameterType.STRING, "服药到哪一天截止，如果没说可以为空", false)
                        ),
                        executor = object : ActionExecutor {
                            override fun onExecute(action: Action, params: Bundle?): Boolean {
                                Log.i(TAG, ">>> ACTION TRIGGERED: CREATE_DRUG_REMINDER is running! <<<")
                                // 严格遵循“黄金法则”
                                AOCoroutineScope.launch {
                                    val drugName = params?.getString("drug_name") ?: "未知药物"
                                    val dosage = params?.getString("dosage_instruction") ?: "遵医嘱"
                                    val times = params?.getString("reminder_times") ?: "未指定时间"
                                    val stopDate = params?.getString("stop_date") ?: "长期"

                                    // 1. 更新ViewModel (保持不变)
                                    viewModel.addReminder(drugName, dosage, times, stopDate)

                                    // 2. TTS反馈 (保持不变)
                                    AgentCore.tts("好的，我记住了，$times 需要服用 $drugName,$dosage。")

                                    // 3. 通知系统并引导 (保持不变)
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

            Log.i(TAG, ">>> V-I-C-T-O-R-Y!!!: ATOMIC PageAgent configuration complete!")

        } catch (e: Exception) {
            Log.e(TAG, "!!!!!! CRITICAL FAILURE during ATOMIC configuration!", e)
        }

        setContent {
            ZhiyunAgentRobotTheme {
                // 将ViewModel的StateFlow连接到UI
                val reminders by viewModel.reminders.collectAsState()
                // ✅ 2. 关键修改点：添加一个LaunchedEffect来监听来自ViewModel的“设置闹钟”信号
                // 这个逻辑与您的UI代码并行，互不干扰
                // 现在我们从管道里收到的item，就是那个完整的、与UI一致的 MedicineReminderItem 类型！
                LaunchedEffect(Unit) {
                    viewModel.alarmEventFlow.collect { item ->
                        Log.i(TAG, ">>> [Activity] 从统一数据流中接收到闹钟指令！")
                        setMedicineAlarm(item)
                    }
                }
                MedicineReminderScreen(
                    userProfile = UserProfile(name = "总司令", avatarUrl = null),
                    reminders = reminders,
                    onBack = { finish() },
                    onMedicineReminderClick = {
                        // TODO: 实现点击麦克风按钮后的逻辑，例如开始语音识别
                        Log.d(TAG, "InteractionCard Mic button clicked!")
                    }
                )
            }
        }
        Log.d(TAG, "[Activity] onCreate finished.")
    }
    // ✅ 3. 关键修改点：在Activity类中，添加设置闹钟的执行函数 `setMedicineAlarm`
    /**
     * 设置闹钟的执行函数
     * @param item 包含提醒信息的数据项
     */
    private fun setMedicineAlarm(item: MedicineReminderItem) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MedicineAlarmReceiver::class.java).apply {
            putExtra("DRUG_NAME", item.drugName)
            putExtra("DOSAGE", item.dosageInstruction)
            action = "MEDICINE_REMINDER_ACTION_${item.id}"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 调用新添加的真实时间计算函数
        val triggerAtMillis = getNextTriggerTime(item.reminderTimes)

        if (triggerAtMillis == null) {
            Log.e(TAG, "无法解析提醒时间: ${item.reminderTimes}，闹钟设置失败！")
            // 此处保持与您代码风格一致的TTS反馈
            AgentCore.tts("抱歉，我没能听懂您说的提醒时间，请再说一次。")
            return
        }

        val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        Log.i(TAG, "【真实时间】闹钟将于: ${calendar.time} 触发，提醒: ${item.drugName}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        Log.i(TAG, ">>>【真实时间】闹钟设置成功！<<<")
        // 这里不再重复TTS，因为您的Action中已有TTS反馈
    }
    // ✅ setMedicineAlarm函数添加结束

    // ✅ 4. 关键修改点：添加计算下一个提醒时间的辅助函数 `getNextTriggerTime`
    /**
     * 一个简化的、但功能正确的函数，用于计算下一个提醒时间的毫秒时间戳
     */
    private fun getNextTriggerTime(reminderTimes: String): Long? {
        val hourPattern = "(\\d+)(点|时)".toRegex()
        val hours = hourPattern.findAll(reminderTimes).mapNotNull { it.groupValues[1].toIntOrNull() }.toList()

        if (hours.isEmpty()) return null

        val now = Calendar.getInstance()
        var nextTriggerTime: Calendar? = null

        for (hour in hours) {
            val candidateTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (candidateTime.after(now)) {
                if (nextTriggerTime == null || candidateTime.before(nextTriggerTime)) {
                    nextTriggerTime = candidateTime
                }
            }
        }

        if (nextTriggerTime == null) {
            val earliestHour = hours.minOrNull() ?: return null
            nextTriggerTime = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, earliestHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
        return nextTriggerTime.timeInMillis
    }
    // ✅ getNextTriggerTime函数添加结束


    override fun onStart() {
        super.onStart()
        Log.i(TAG, ">>> [Activity] onStart: Page becomes VISIBLE. Performing AgentCore tasks...")
        AgentCore.clearContext()
        AgentCore.tts("这里是服药管理页，您可以让我帮您记录需要提醒的用药事项。")
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
