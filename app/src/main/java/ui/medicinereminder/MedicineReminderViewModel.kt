// ✅✅✅【【【【 这是MedicineReminderViewModel.kt的最终修正版，修复了addReminder错误！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.medicinereminder

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 后勤与数据中心：用药提醒的ViewModel
 * 职责：
 * 1. 维护和暴露用药提醒列表的UI状态(StateFlow)。
 * 2. 提供一个被Activity调用的业务处理函数(addReminder)。
 * 3. 在处理业务后，通过一次性事件管道(Channel)通知Activity执行非UI操作（如设置闹钟）。
 */
class MedicineReminderViewModel : ViewModel() {
    companion object {
        const val TAG = "MedicineReminder_VM"
    }

    private val _reminders = MutableStateFlow<List<MedicineReminderItem>>(emptyList())
    val reminders = _reminders.asStateFlow()

    private val _alarmEventChannel = Channel<MedicineReminderItem>()
    val alarmEventFlow = _alarmEventChannel.receiveAsFlow()


    /**
     * ✅ 关键修正点：函数名和参数列表已与Activity中的调用完全匹配！
     * 这个函数由Activity的Action Executor调用，负责所有的数据处理和信号发送
     */
    fun addReminder(drugName: String, dosage: String, reminderTimes: String, stopDate: String) {
        // 使用ViewModel自带的viewModelScope，确保协程生命周期安全
        viewModelScope.launch {
            Log.d(TAG, "接收到Activity的指令，开始创建提醒项...")
            Log.d(TAG, "接收到参数: 药物=$drugName, 剂量=$dosage, 频率=$reminderTimes, 停止日期=$stopDate")

            // 创建新的用药提醒数据项

            // ✅ 关键修正点：创建数据项时，使用与UI完全匹配的新版Data Class
            val newItem = MedicineReminderItem(
                drugName = drugName,
                dosageInstruction = dosage,      // ✅ 适配UI：使用 dosageInstruction
                reminderTimes = reminderTimes,
                stopDate = stopDate,
                status = "待提醒",              // ✅ 适配UI：添加默认的 status 字段
                creationTime = SimpleDateFormat("yyyy/MM/dd HH:mm 创建", Locale.getDefault()).format(Date()) // ✅ 适配UI：使用 creationTime 并格式化
            )
            // 更新UI状态，Compose界面会自动响应并显示新的卡片
            _reminders.value = _reminders.value + newItem
            Log.i(TAG, "UI状态已更新，新的提醒卡片已添加。")

            // 通过管道，向Activity发送“设置闹钟”的指令！
            _alarmEventChannel.send(newItem)
            Log.i(TAG, "已通过Channel向Activity发送'设置闹钟'指令。")
        }
    }
}
/**
 * 用药提醒的数据类
 * 定义了一个提醒事项所包含的所有信息
 */
data class MedicineReminderItem(
    val id: String = UUID.randomUUID().toString(),
    val drugName: String,
    val dosageInstruction: String, // ✅ 对应UI的 '服用说明'
    val reminderTimes: String,     // ✅ 对应UI的 '提醒次数'
    val stopDate: String,          // ✅ 对应UI的 '服药停止日期'
    val status: String,            // ✅ 对应UI的 '状态' (待提醒/已提醒)
    val creationTime: String       // ✅ 对应UI的 '创建时间'
)
/**
 * ✅ 为了保持架构清晰，我们创建一个专门用于传递给闹钟的、简化的数据类
 * 这样，UI的数据结构变化，不会直接影响到底层的闹钟逻辑
 */
data class MedicineReminderItemForAlarm(
    val id: String,
    val drugName: String,
    val dosage: String,
    val reminderTimes: String
)

