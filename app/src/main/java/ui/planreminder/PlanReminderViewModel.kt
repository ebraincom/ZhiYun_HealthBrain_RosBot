// ✅✅✅【【【【 这是 V1.3 计划提醒 ViewModel：遵从最高指示，拨乱反正版！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.planreminder // ✅ 1. 包名已更新

import android.app.Application
import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.parcelize.Parcelize


// ✅ 2. 类名已更新
class PlanReminderViewModel(application: Application) : AndroidViewModel(application) {

    // ✅ 3. 日志TAG和常量已更新
    companion object {
        const val TAG = "PlanReminder_VM_V1.3"
        // ✅ 4.【【【 核心修改 】】】为“计划提醒”分配一个全新的、独立的SharedPreferences文件名！
        private const val PREFS_NAME = "plan_reminder_prefs"
        private const val REMINDERS_KEY = "plan_reminders_list_key"
    }

    private val gson = Gson()
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ✅ 5. 数据源的数据类型已更新为新的 PlanReminderItem
    private val _reminders = MutableStateFlow<List<PlanReminderItem>>(emptyList())
    val reminders = _reminders.asStateFlow()

    // ✅ 6. Channel的事件类型也更新为新的 PlanReminderItem
    private val _alarmEventChannel = Channel<PlanReminderItem>()
    val alarmEventFlow = _alarmEventChannel.receiveAsFlow()

    init {
        Log.i(TAG, "ViewModel V1.3 初始化！开始从 SharedPreferences 恢复'计划提醒'数据...")
        loadRemindersFromPrefs()
    }

    private fun loadRemindersFromPrefs() {
        val json = prefs.getString(REMINDERS_KEY, null)
        if (json != null) {
            try {
                // ✅ 7. 反序列化的目标类型已更新
                val type = object : TypeToken<List<PlanReminderItem>>() {}.type
                val savedList: List<PlanReminderItem> = gson.fromJson(json, type)
                _reminders.value = savedList
                Log.i(TAG, "V1.3: 成功从 SharedPreferences 恢复了 ${savedList.size} 个'计划提醒'项。")
            } catch (e: Exception) {
                Log.e(TAG, "V1.3: 从 SharedPreferences 解析'计划提醒'数据失败！", e)
            }
        } else {
            Log.i(TAG, "V1.3: SharedPreferences 中没有找到'计划提醒'数据。")
        }
    }

    private fun saveRemindersToPrefs(list: List<PlanReminderItem>) {
        try {
            val json = gson.toJson(list)
            prefs.edit().putString(REMINDERS_KEY, json).apply()
            Log.i(TAG, "V1.3: 已成功将 ${list.size} 个'计划提醒'项写入 SharedPreferences。")
        } catch (e: Exception) {
            Log.e(TAG, "V1.3: 写入 SharedPreferences 失败！", e)
        }
    }

    /**
     * 添加一个新的计划提醒事项
     */
    // ✅ 8. addReminder方法的参数已更新为通用场景
    fun addReminder(
        content: String,
        details: String,
        reminderTimePoints: String,
        stopCondition: String?
    ) {
        viewModelScope.launch {
            Log.d(TAG, "V1.3: 接收到Activity指令...")
            // ✅ 9. 创建新的PlanReminderItem实例
            val newItem = PlanReminderItem(
                creationTime = SimpleDateFormat("yyyy/MM/dd HH:mm 创建", Locale.getDefault()).format(Date()),
                content = content,
                details = details,
                reminderTimePoints = reminderTimePoints,
                stopCondition = stopCondition,
                reminderStatus = "待提醒"
            )

            val newList = listOf(newItem) + _reminders.value
            _reminders.value = newList
            saveRemindersToPrefs(newList)

            _alarmEventChannel.send(newItem)
        }
    }

    /**
     * 将提醒标记为“已提醒” (完全复用逻辑)
     */
    fun markAsReminded(reminderId: String) {
        viewModelScope.launch {
            val currentList = _reminders.value
            val targetIndex = currentList.indexOfFirst { it.id == reminderId }

            if (targetIndex != -1 && currentList[targetIndex].reminderStatus == "待提醒") {
                Log.i(TAG, "V1.3: 接收到状态更新指令，正在将提醒[$reminderId]标记为'已提醒'...")
                val updatedItem = currentList[targetIndex].copy(reminderStatus = "已提醒")
                val newList = currentList.toMutableList().apply {
                    set(targetIndex, updatedItem)
                }
                _reminders.value = newList
                saveRemindersToPrefs(newList)
                Log.i(TAG, "V1.3: 状态更新并持久化成功！")
            } else {
                Log.d(TAG, "V1.3: 无需更新状态（可能ID未找到或状态已更新）。")
            }
        }
    }
}
@Parcelize
// ✅ 13. 类名和参数已更新为通用“计划提醒”场景, 并补全stopCondition
data class PlanReminderItem(
    val id: String = UUID.randomUUID().toString(),
    val creationTime: String,
    val content: String,
    val details: String,
    val reminderTimePoints: String,
    val stopCondition: String?, // ✅ 补全缺失的字段
    val reminderStatus: String
) : Parcelable

