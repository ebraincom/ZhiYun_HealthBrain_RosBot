// ✅✅✅【【【【 这是 终极修复版 Activity：使用官方标准创建方式！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.medicinereminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
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
import java.util.Calendar
import java.util.regex.Pattern
import androidx.lifecycle.lifecycleScope // 🎯🎯🎯 1. 导入与Activity生命周期绑定的协程作用域 🎯🎯🎯
import kotlinx.coroutines.Dispatchers // 🎯🎯🎯 2. 导入协程调度器 🎯🎯🎯
import kotlinx.coroutines.withContext     // 🎯🎯🎯 新增修改 2 of 3: 导入协程调度器以切换线程 🎯🎯🎯
import android.content.BroadcastReceiver
import android.content.IntentFilter



class MedicineReminderActivity : ComponentActivity() {

    private val TAG = "MedicineReminder_ACT_13.1"

    private val viewModel: MedicineReminderViewModel by viewModels()

    private lateinit var pageAgent: PageAgent
    // 🎯🎯🎯 V13.1 添加 2 of 5: 定义广播接收器，这是我们接收后台信号的“耳朵” 🎯🎯🎯
    private val statusUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 确认是我们想要接收的广播
            if (intent?.action == "com.zhiyun.agentrobot.ACTION_UPDATE_STATUS") {
                val reminderId = intent.getStringExtra("REMINDER_ID")
                if (reminderId != null) {
                    Log.d(TAG, "V13.1: Activity收到状态更新广播，通知ViewModel...")
                    // 指挥ViewModel执行状态更新
                    viewModel.markAsReminded(reminderId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, ">>> [Activity] V7.0 onCreate: Starting...")

        try {
            pageAgent = PageAgent(this)
                .blockAllActions()
                .setObjective("这个页面的目标是帮助用户记录和管理服药提醒。")
                .registerAction(
                    Action(
                        name = "com.zhiyun.agentrobot.CREATE_DRUG_REMINDER",
                        displayName = "新增用药事项",
                        desc = "当用户想要创建一个新的服药提醒时，调用此Action。对话中应尽可能提取用药的关键信息。",
                        parameters = listOf(
                            Parameter("drug_name", ParameterType.STRING, "要服用的药物名称，例如阿司匹林", true),
                            Parameter("dosage_instruction", ParameterType.STRING, "服用说明，例如每次几片、饭后服用", true),
                            Parameter(
                                "reminder_frequency",
                                ParameterType.STRING,
                                "提醒的频率，例如每日、每天、每周、隔天等。如果用户只说了具体时间，此参数可为空",
                                false
                            ),
                            Parameter(
                                "reminder_time_points",
                                ParameterType.STRING,
                                "提醒的具体时间点，格式应尽可能多样化，例如'早上8点'、'17:50'、'晚上9点半'、'8点和晚上7点'、'睡前'等",
                                true
                            ),
                            Parameter(
                                "stop_condition",
                                ParameterType.STRING,
                                "服药的停止条件，可以是一个具体的日期（如2025年10月1日），也可以是一个相对的日期或周期（如明天、下周、月底、长期服用、吃完这盒药为止）",
                                false
                            )
                        ),
                        // ✅✅✅ 【【【【 关键重构点 1：严格遵守官方文档的 Action 执行流程！！！ 】】】】 ✅✅✅
                        executor = object : ActionExecutor {
                            override fun onExecute(action: Action, params: Bundle?): Boolean {
                                // AgentOS在子线程调用此方法，我们在这里仅做最简单的参数解析
                                val drugName = params?.getString("drug_name") ?: "未知药物"
                                val dosage = params?.getString("dosage_instruction") ?: "遵医嘱"
                                val frequency = params?.getString("reminder_frequency")
                                val timePoints = params?.getString("reminder_time_points") ?: "未指定时间"
                                val stopCondition = params?.getString("stop_condition")

                                // 【关键】使用与Activity生命周期绑定的 lifecycleScope 启动一个新协程
                                // 这个协程默认就在主线程运行！
                                lifecycleScope.launch {
                                    // 在这里，我们已经回到了安全的主线程
                                    Log.d(TAG, "V13.1: Now on MAIN thread, calling viewModel.addReminder...")
                                    // 在主线程上安全地调用ViewModel，更新SavedStateHandle
                                    viewModel.addReminder(drugName, dosage, frequency, timePoints, stopCondition)

                                    // TTS和notify是耗时操作，把它们切换到IO线程，避免阻塞主线程
                                    withContext(Dispatchers.IO) {
                                        val ttsMessage = "好的，我记住了，$timePoints 需要服用 $drugName,$dosage。"
                                        AgentCore.tts(ttsMessage)
                                        // 【关键】在所有耗时操作的最后，调用notify！
                                        action.notify(isTriggerFollowUp = true)
                                        Log.d(TAG, "V13.1: TTS and Notify completed on IO thread.")
                                    }
                                }

                                Log.d(TAG, "V13.1: onExecute immediately returns true from AgentOS's worker thread.")
                                // 立即返回true，不阻塞AgentOS的线程，完全符合文档规范
                                return true
                            }
                        }
                    )
                )
                .registerAction(Actions.SAY)
                .registerAction(Actions.EXIT)
            Log.i(TAG, ">>> V-I-C-T-O-R-Y!!!:  ATOMIC PageAgent configuration complete!")
        } catch (e: Exception) {
            Log.e(TAG, "!!!!!! CRITICAL FAILURE during ATOMIC configuration!", e)
        }

        setContent {
            ZhiyunAgentRobotTheme {
                val reminders by viewModel.reminders.collectAsState()
                LaunchedEffect(Unit) {
                    viewModel.alarmEventFlow.collect { item ->
                        setMedicineAlarm(item)
                    }
                }
                MedicineReminderScreen(
                    userProfile = UserProfile(name = "总司令", avatarUrl = null),
                    reminders = reminders,
                    onBack = { finish() },
                    onMedicineReminderClick = { }
                )
            }
        }
        Log.d(TAG, "[Activity] V13.1 onCreate finished.")
        // 🎯🎯🎯 V13.1 添加 3 of 5: 在 onCreate 的末尾，注册我们的广播接收器，让它开始“监听” 🎯🎯🎯
        val filter = IntentFilter("com.zhiyun.agentrobot.ACTION_UPDATE_STATUS")
        registerReceiver(statusUpdateReceiver, filter,RECEIVER_NOT_EXPORTED)
        Log.d(TAG, "V13.1: 状态更新广播接收器已注册。")

    }

    private fun setMedicineAlarm(item: MedicineReminderItem) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // ✅ 核心升级点 1：定义重复间隔和名称
        val totalCount = 3
        val intervalMillis = 3 * 60 * 1000L // 3分钟
        // 2. 创建一个“信息丰富”的Intent
        val intent = Intent(this, MedicineAlarmReceiver::class.java).apply {
            // 关键信息：闹钟的唯一身份ID，用于计数和取消
            putExtra("REMINDER_ID", item.id)
            // 播报内容
            putExtra("DRUG_NAME", item.drugName)
            putExtra("DOSAGE", item.dosageInstruction)

            // 重复契约
            // putExtra("CURRENT_INDEX", 1) // 这是第一次，所以写死为 1
            putExtra("TOTAL_COUNT", totalCount)
            putExtra("INTERVAL_MILLIS", intervalMillis)

            // 使用唯一的Action，确保PendingIntent的唯一性
            action = "MEDICINE_REMINDER_ACTION_${item.id}"
        }


        // 3. 创建与之前完全一致的PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            item.id.hashCode(), // 使用item.id的hashCode作为requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // 4. 获取第一次的触发时间 (此逻辑不变)
        val triggerAtMillis = getNextTriggerTime(item.reminderTimePoints)

        if (triggerAtMillis == null) {
            Log.e(TAG, "V3.3 无法解析提醒时间: ${item.reminderTimePoints}，闹钟设置失败！")
            AOCoroutineScope.launch { AgentCore.tts("抱歉，我没能听懂您说的提醒时间，请再说一次。") }
            return
        }
        val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        Log.i(TAG, "【真实时间】V3.3 [第1次] 闹钟将于: ${calendar.time} 触发，提醒: ${item.drugName}")

        // 5. 使用最可靠的 setExactAndAllowWhileIdle 设置【第一次】闹钟
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
        Log.i(TAG, ">>>【真实时间】V3.3 [第1次] 闹钟设置成功！<<<")
    }

    // ✅✅✅ 【【【【 关键重构点 2：V2.2 时间解析引擎！！！ 】】】】 ✅✅✅
    private fun getNextTriggerTime(reminderTimes: String): Long? {
        Log.d(TAG, "V2.2 引擎开始解析时间字符串: '$reminderTimes'")
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
                Log.d(TAG, "规则命中: 解析出 时间段='$period', 小时=$hour, 是否半点=$isHalf -> 最终小时=$hour, 分钟=$minute")
                return calculateTriggerMillis(hour, minute)
            }
        }

        val hhmmPattern = Pattern.compile("(\\d{1,2}):(\\d{2})")
        val hhmmMatcher = hhmmPattern.matcher(reminderTimes)
        if (hhmmMatcher.find()) {
            val hour = hhmmMatcher.group(1)?.toIntOrNull()
            val minute = hhmmMatcher.group(2)?.toIntOrNull()
            if (hour != null && minute != null) {
                Log.d(TAG, "规则命中: 解析出 小时=$hour, 分钟=$minute")
                return calculateTriggerMillis(hour, minute)
            }
        }
        Log.e(TAG, "所有解析规则均未命中，返回 null")
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
        Log.i(TAG, ">>> [Activity] onStart: Page becomes VISIBLE. Performing AgentCore tasks...")
        AgentCore.clearContext()
        AOCoroutineScope.launch { AgentCore.tts("这里是服药管理页，您可以让我帮您记录需要提醒的用药事项。") }
        Log.i(TAG, ">>> onStart: AgentCore tasks dispatched.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, ">>> [Activity] onDestroy: Cleaning up session.")
        if (::pageAgent.isInitialized) {
            pageAgent.destroy()
        }
        // 🎯🎯🎯 V13.1 添加 5 of 5: 注销广播接收器，防止内存泄漏，这是非常重要的好习惯！ 🎯🎯🎯
        unregisterReceiver(statusUpdateReceiver)
        Log.d(TAG, "V13.1: 状态更新广播接收器已注销。")
    }
}
