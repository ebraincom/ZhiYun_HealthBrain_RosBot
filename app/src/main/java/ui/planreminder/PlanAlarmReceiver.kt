// ✅✅✅【【【【 这是 V1.7 计划提醒 Receiver：遵从最高指示，拨乱反正版！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.planreminder // ✅ 1. 包名已更新

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ainirobot.agent.AgentCore
import java.util.Calendar

// ✅ 2. 类名已更新
class PlanAlarmReceiver : BroadcastReceiver() {

    // ✅ 3. 日志TAG和常量已更新
    companion object {
        const val TAG = "PlanAlarmReceiver_V1.7"
        const val REMINDER_PREFS_NAME = "PlanReminderCounts"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null, cannot process alarm.")
            return
        }

        val reminderId = intent.getStringExtra("REMINDER_ID")
        if (reminderId.isNullOrEmpty()) {
            Log.e(TAG, "V1.7: Reminder ID 为空，任务异常终止！")
            return
        }

        // 1. 【统一的计数系统】: 完全依赖 SharedPreferences
        val prefs = context.getSharedPreferences(REMINDER_PREFS_NAME, Context.MODE_PRIVATE)
        val currentCount = prefs.getInt(reminderId, 0) + 1

        Log.i(TAG, "V1.7: 闹钟[$reminderId]触发，这是第 $currentCount 次。")

        // 2. 如果是第一次触发，发送状态更新广播
        if (currentCount == 1) {
            Log.i(TAG, "V1.7: 这是第一次触发，发送'计划提醒'状态更新广播...")
            // ✅ 4. 广播的Action名称已更新
            val updateIntent = Intent("com.zhiyun.agentrobot.ACTION_UPDATE_PLAN_STATUS").apply {
                setPackage(context.packageName)
                putExtra("REMINDER_ID", reminderId)
            }
            context.sendBroadcast(updateIntent)
        }

        // 3. 播报语音 (核心任务)
        // ✅ 5. 播报内容已从“药品名”更新为通用的“提醒内容”
        val content = intent.getStringExtra("REMINDER_CONTENT") ?: "您之前设置的提醒事项"
        val ttsMessage = "第${currentCount}次提醒！叮咚！请您注意，现在有计划事项需要处理，$content。"

        // ✅✅✅ 【【【【 终极修正: 使用 AgentCore.tts()，这是官方推荐的、经过封装的、可以在任何地方调用的安全API！！！ 】】】】 ✅✅✅
        AgentCore.tts(ttsMessage)
        Log.i(TAG, "V1.7: TTS播报指令已发送: $ttsMessage")

        // 4. 更新持久化计数值
        prefs.edit().putInt(reminderId, currentCount).apply()

        // 5. 【统一的接力逻辑】: 检查是否需要“自我接力”
        val totalCount = intent.getIntExtra("TOTAL_COUNT", 3) // 默认3次
        if (currentCount < totalCount) {
            Log.i(TAG, "V1.7: 任务未完成，准备设置第 ${currentCount + 1} 次提醒...")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intervalMillis = intent.getLongExtra("INTERVAL_MILLIS", 0L)

            // ✅ 6. Intent指向新的PlanAlarmReceiver
            val nextIntent = Intent(context, PlanAlarmReceiver::class.java).apply {
                // ✅ 7. Intent的Action已更新
                action = "PLAN_REMINDER_ACTION_$reminderId"
                // 复制所有下一次触发时依然需要的信息
                putExtras(intent.extras!!)
            }

            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextTriggerTime = System.currentTimeMillis() + intervalMillis
            val calendar = Calendar.getInstance().apply { timeInMillis = nextTriggerTime }

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, nextPendingIntent)
            Log.i(TAG, "V1.7: >>> [第${currentCount + 1}次] 提醒已成功设置在: ${calendar.time} <<<")

        } else {
            // 6. 任务完成，清理计数器
            Log.i(TAG, "V1.7: >>> 提醒次数已达 $totalCount 次，任务彻底结束。正在清理计数器...<<<")
            prefs.edit().remove(reminderId).apply()
        }
    }
}
