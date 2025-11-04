// ✅✅✅【【【【 这是 V12.0 终极版 Receiver：统一思想，废除双计数！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.medicinereminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ainirobot.agent.AgentCore
import java.util.Calendar



class MedicineAlarmReceiver : BroadcastReceiver() {

    private val TAG = "MedicineAlarmReceiver_V12.0"
    // ✅ 将SharedPreferences文件名作为常量，避免硬编码
    companion object {
        const val REMINDER_PREFS_NAME = "MedicineReminderCounts"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null, cannot process alarm.")
            return
        }

        val reminderId = intent.getStringExtra("REMINDER_ID")
        if (reminderId.isNullOrEmpty()) {
            Log.e(TAG, "V12.0: Reminder ID 为空，任务异常终止！")
            return
        }

        // 1. 【统一的计数系统】: 完全依赖 SharedPreferences
        val prefs = context.getSharedPreferences(REMINDER_PREFS_NAME, Context.MODE_PRIVATE)
        val currentCount = prefs.getInt(reminderId, 0) + 1 // 读取旧值，加1

        Log.i(TAG, "V12.0: 闹钟[$reminderId]触发，这是第 $currentCount 次。")
        // 🎯🎯🎯 V13.1 现代化广播: 如果是第一次触发，就发送一个限定范围的广播 🎯🎯🎯
        // 【【【【 这是您需要添加的全新代码块 】】】】更新卡片状态
        if (currentCount == 1) {
            Log.i(TAG, "V13.1: 这是第一次触发，发送状态更新广播...")
            val updateIntent = Intent("com.zhiyun.agentrobot.ACTION_UPDATE_STATUS").apply {
                // 【关键】将广播限定在自己的应用内，这是替代LocalBroadcastManager的最佳实践之一
                setPackage(context.packageName)
                putExtra("REMINDER_ID", reminderId)
            }
            context.sendBroadcast(updateIntent)
        }

        // 2. 播报语音 (核心任务)
        val drugName = intent.getStringExtra("DRUG_NAME") ?: "您之前设置的药物"
        val dosage = intent.getStringExtra("DOSAGE") ?: "请遵医嘱"
        val ttsMessage = "第${currentCount}次提醒！叮咚！请您注意，现在是用药时间，请记得服用 $drugName,$dosage。"
        AgentCore.tts(ttsMessage)
        Log.i(TAG, "V12.0: TTS播报指令已发送: $ttsMessage")

        // 3. 更新持久化计数值
        prefs.edit().putInt(reminderId, currentCount).apply()

        // 4. 【统一的接力逻辑】: 检查是否需要“自我接力”
        val totalCount = intent.getIntExtra("TOTAL_COUNT", 1)
        if (currentCount < totalCount) {
            Log.i(TAG, "V12.0: 任务未完成，准备设置第 ${currentCount + 1} 次提醒...")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intervalMillis = intent.getLongExtra("INTERVAL_MILLIS", 0L)

            // 【关键】创建一个【干净】的接力Intent，只包含必要信息
            val nextIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                // 【关键】严格、明确地重新设置与第一次完全相同的action！
                action = "MEDICINE_REMINDER_ACTION_$reminderId"

                // 复制所有下一次触发时依然需要的信息
                putExtra("REMINDER_ID", reminderId)
                putExtra("DRUG_NAME", drugName)
                putExtra("DOSAGE", dosage)
                putExtra("TOTAL_COUNT", totalCount) // 终点判断依然需要
                putExtra("INTERVAL_MILLIS", intervalMillis) // 间隔计算依然需要
            }

            // 使用相同的requestCode和action创建PendingIntent
            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextTriggerTime = System.currentTimeMillis() + intervalMillis
            val calendar = Calendar.getInstance().apply { timeInMillis = nextTriggerTime }

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, nextPendingIntent)
            Log.i(TAG, "V12.0: >>> [第${currentCount + 1}次] 提醒已成功设置在: ${calendar.time} <<<")

        } else {
            // 5. 任务完成，清理计数器
            Log.i(TAG, "V12.0: >>> 提醒次数已达 $totalCount 次，任务彻底结束。正在清理计数器...<<<")
            prefs.edit().remove(reminderId).apply()
        }
    }
}
