// ✅✅✅【【【【 这是 V11.0 终极版ViewModel：放弃幻想，回归 SharedPreferences！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.medicinereminder

import android.app.Application // 🎯 1. 导入Application
import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.AndroidViewModel // 🎯 2. 改为继承 AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson // 🎯 3. 导入Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// 🎯 4. 构造函数改为接收 Application
class MedicineReminderViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "MedicineReminder_VM_V11.0"
        private const val PREFS_NAME = "medicine_reminder_prefs"
        private const val REMINDERS_KEY = "reminders_list_key"
    }

    private val gson = Gson()
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 🎯 5. 数据源改为普通的 MutableStateFlow
    private val _reminders = MutableStateFlow<List<MedicineReminderItem>>(emptyList())
    val reminders = _reminders.asStateFlow()

    private val _alarmEventChannel = Channel<MedicineReminderItem>()
    val alarmEventFlow = _alarmEventChannel.receiveAsFlow()

    init {
        // 🎯 6. 在初始化时，从 SharedPreferences 中读取数据
        Log.i(TAG, "ViewModel V11.0 初始化！开始从 SharedPreferences 恢复数据...")
        loadRemindersFromPrefs()
    }

    private fun loadRemindersFromPrefs() {
        val json = prefs.getString(REMINDERS_KEY, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<MedicineReminderItem>>() {}.type
                val savedList: List<MedicineReminderItem> = gson.fromJson(json, type)
                _reminders.value = savedList
                Log.i(TAG, "V11.0: 成功从 SharedPreferences 恢复了 ${savedList.size} 个提醒项。")
            } catch (e: Exception) {
                Log.e(TAG, "V11.0: 从 SharedPreferences 解析数据失败！", e)
            }
        } else {
            Log.i(TAG, "V11.0: SharedPreferences 中没有找到提醒数据。")
        }
    }

    // 🎯 7. 在添加提醒后，将新列表写入 SharedPreferences
    private fun saveRemindersToPrefs(list: List<MedicineReminderItem>) {
        try {
            val json = gson.toJson(list)
            prefs.edit().putString(REMINDERS_KEY, json).apply()
            Log.i(TAG, "V11.0: 已成功将 ${list.size} 个提醒项写入 SharedPreferences。")
        } catch (e: Exception) {
            Log.e(TAG, "V11.0: 写入 SharedPreferences 失败！", e)
        }
    }

    fun addReminder(
        drugName: String,
        dosageInstruction: String,
        reminderFrequency: String?,
        reminderTimePoints: String,
        stopCondition: String?
    ) {
        viewModelScope.launch {
            Log.d(TAG, "V11.0: 接收到Activity指令...")
            val newItem = MedicineReminderItem(
                creationTime = SimpleDateFormat(
                    "yyyy/MM/dd HH:mm 创建",
                    Locale.getDefault()
                ).format(Date()),
                drugName = drugName,
                dosageInstruction = dosageInstruction,
                reminderFrequency = reminderFrequency,
                reminderTimePoints = reminderTimePoints,
                stopCondition = stopCondition,
                reminderStatus = "待提醒"
            )

            val newList = _reminders.value + newItem
            _reminders.value = newList // 更新UI
            saveRemindersToPrefs(newList) // 持久化到 SharedPreferences

            _alarmEventChannel.send(newItem)
        }
    }

    // 🎯🎯🎯 V13.0 新增功能 1 of 3: 添加一个用于更新状态的新方法 🎯🎯🎯
    fun markAsReminded(reminderId: String) {
        viewModelScope.launch {
            val currentList = _reminders.value
            // 找到需要更新的目标
            val targetIndex = currentList.indexOfFirst { it.id == reminderId }

            // 确保目标存在，并且状态是“待提醒”（避免重复执行）
            if (targetIndex != -1 && currentList[targetIndex].reminderStatus == "待提醒") {
                Log.i(TAG, "V13.0: 接收到状态更新指令，正在将提醒[$reminderId]标记为'已提醒'...")

                // 创建一个新的、更新了状态的Item
                val updatedItem = currentList[targetIndex].copy(reminderStatus = "已提醒")

                // 创建一个新的列表并将更新后的Item替换进去
                val newList = currentList.toMutableList().apply {
                    set(targetIndex, updatedItem)
                }

                _reminders.value = newList // 更新UI
                saveRemindersToPrefs(newList) // 将新状态持久化到 SharedPreferences
                Log.i(TAG, "V13.0: 状态更新并持久化成功！")
            } else {
                Log.d(TAG, "V13.0: 无需更新状态（可能ID未找到或状态已更新）。")
            }
        }
    }
}

@Parcelize
data class MedicineReminderItem(
    val id: String = UUID.randomUUID().toString(),
    val creationTime: String,
    val drugName: String,
    val dosageInstruction: String,
    val reminderFrequency: String?,
    val reminderTimePoints: String,
    val stopCondition: String?,
    val reminderStatus: String
) : Parcelable
