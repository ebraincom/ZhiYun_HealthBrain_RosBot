// ✅✅✅【【【【 这是 V1.0 当天提醒 Receiver：遵从最高指示，100%精确复刻最终版！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.todayreminder // ✅ 1. 包名已更新

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ainirobot.agent.AgentCore

// ✅ 2. 类名已更新
class TodayReminderAlarmReceiver : BroadcastReceiver() {

    // ✅ 3. 日志TAG和常量已更新
    companion object {
        const val TAG = "TodayReminder_Receiver_V1.0"
        const val REMINDER_PREFS_NAME = "TodayReminderCounts" // ✅ 计数器专用的 SharedPreferences 名称
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null, cannot process alarm.")
            return
        }

        val reminderId = intent.getStringExtra("REMINDER_ID")
        if (reminderId.isNullOrEmpty()) {
            Log.e(TAG, "V1.0: Reminder ID 为空，任务异常终止！")
            return
        }

        // ✅✅✅ 【【【 1. 【统一的计数系统】: 完全复刻！！！ 】】】】 ✅✅✅
        val prefs = context.getSharedPreferences(REMINDER_PREFS_NAME, Context.MODE_PRIVATE)
        val currentCount = prefs.getInt(reminderId, 0) + 1

        Log.i(TAG, "V1.0: '当天提醒'闹钟[$reminderId]触发，这是第 $currentCount 次。")

        // ✅✅✅ 【【【 2. 第一次触发的判断逻辑: 完全复刻！！！ 】】】】 ✅✅✅
        if (currentCount == 1) {
            Log.i(TAG, "V1.0: 这是第一次触发，发送'当天提醒'状态更新广播...")
            // ✅ 4. 广播的Action名称已更新
            val updateIntent = Intent("com.zhiyun.agentrobot.ACTION_UPDATE_TODAY_STATUS").apply {
                setPackage(context.packageName)
                putExtra("REMINDER_ID", reminderId)
            }
            context.sendBroadcast(updateIntent)
        }

        // ✅✅✅ 【【【 3. 播报语音 (核心任务): 完全复刻！！！ 】】】】 ✅✅✅
        // ✅ 5. 播报内容已适配为 TodayReminder 的字段
        val content = intent.getStringExtra("REMINDER_CONTENT") ?: "您之前设置的当天提醒"
        val ttsMessage = "第${currentCount}次提醒！叮咚！今天的提醒时间到了！请记得处理事项，$content。"

        // ✅✅✅ 【【【【 终极修正: 100%复刻！直接使用 AgentCore.tts()，这才是最正确的“照猫画虎”！！！ 】】】】 ✅✅✅
        AgentCore.tts(ttsMessage)
        Log.i(TAG, "V1.0: TTS播报指令已发送: $ttsMessage")

        // ✅✅✅ 【【【 4. 更新持久化计数值: 完全复刻！！！ 】】】】 ✅✅✅
        prefs.edit().putInt(reminderId, currentCount).apply()

        // ✅✅✅ 【【【 5. 【业务逻辑适配】: 仅修改此部分逻辑！！！ 】】】】 ✅✅✅
        // 根据您的业务逻辑，"当天提醒"是一次性的，不涉及多次“接力”提醒。
        // 因此，我们直接进入“任务完成”的逻辑。
        Log.i(TAG, "V1.0: >>> '当天提醒'任务已完成，无需接力。正在清理计数器...<<<")
        prefs.edit().remove(reminderId).apply()
    }
}


