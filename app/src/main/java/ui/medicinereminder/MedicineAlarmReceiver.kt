// ✅✅✅【【【【 这是 MedicineAlarmReceiver.kt 的最终完整版代码！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.medicinereminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ainirobot.agent.AgentCore

/**
 * 信号接收塔：服药提醒专属的广播接收器
 * 职责：
 * 1. 作为一个独立组件，在AndroidManifest.xml中注册。
 * 2. 在后台或App关闭时，能被系统的AlarmManager唤醒。
 * 3. 接收到广播信号(Intent)后，解析出提醒内容。
 * 4. 调用AgentCore.tts()执行语音播报，完成提醒任务。
 */
class MedicineAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "MedicineAlarmReceiver"
    }

    /**
     * 当接收到广播信号时，此方法被Android系统自动调用。
     * 官方文档指出，onReceive的生命周期很短，应尽快完成任务。
     * TTS播报是一个快速的发起操作，符合此要求。
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, ">>>【服药专属】提醒信号已收到！黎明之钟敲响！<<<")

        // 从广播信号(Intent)中安全地解析出我们之前存入的药物名称和剂量
        val drugName = intent?.getStringExtra("DRUG_NAME") ?: "指定的药物"
        val dosage = intent?.getStringExtra("DOSAGE") ?: "指定的剂量"

        Log.d(TAG, "解析到提醒内容 - 药物: $drugName, 剂量: $dosage")

        // 直接执行最核心的提醒任务：TTS语音播报
        AgentCore.tts("叮咚！现在是用药时间，请服用 $drugName, $dosage。")

        Log.i(TAG, ">>>【服药专属】TTS播报指令已发出！<<<")

        // TODO: 未来在此处添加弹出“横幅通知”(Notification)的代码，以实现更强的提醒效果。
    }
}



