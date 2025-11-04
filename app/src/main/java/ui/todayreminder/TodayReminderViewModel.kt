// ✅✅✅【【【【 这是 V1.0 当天提醒 ViewModel：大脑构建，完美复刻版！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.todayreminder // ✅ 1. 包名已更新

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ✅ 2. 数据类名称已更新
data class TodayReminderItem(
    val id: String = UUID.randomUUID().toString(),
    val creationTime: String,
    val content: String,
    val details: String,
    var reminderStatus: String = "待提醒"
)

// ✅ 3. ViewModel类名及常量已更新
class TodayReminderViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "TodayReminder_VM_V1.0"
        private const val PREFS_NAME = "TodayReminderPrefs"
        private const val REMINDERS_KEY = "today_reminders"
    }

    private val _reminders = MutableStateFlow<List<TodayReminderItem>>(emptyList())
    val reminders: StateFlow<List<TodayReminderItem>> = _reminders.asStateFlow()

    private val sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        Log.i(TAG, "ViewModel V1.0 初始化！开始从 SharedPreferences 恢复'当天提醒'数据...")
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            val json = sharedPreferences.getString(REMINDERS_KEY, null)
            if (json != null) {
                val type = object : TypeToken<List<TodayReminderItem>>() {}.type
                val loadedList: List<TodayReminderItem> = gson.fromJson(json, type)
                _reminders.value = loadedList
                Log.i(TAG, "V1.0: 成功从 SharedPreferences 恢复了 ${loadedList.size} 个'当天提醒'项。")
            } else {
                Log.i(TAG, "V1.0: SharedPreferences 中没有找到'当天提醒'数据。")
            }
        }
    }

    private fun saveReminders() {
        viewModelScope.launch {
            val json = gson.toJson(_reminders.value)
            sharedPreferences.edit().putString(REMINDERS_KEY, json).apply()
            Log.i(TAG, "V1.0: 已成功将 ${_reminders.value.size} 个'当天提醒'项写入 SharedPreferences。")
        }
    }

    // ✅ 4. 方法参数及内部逻辑已适配为 TodayReminderItem
    fun addReminder(content: String, details: String) {
        Log.d(TAG, "V1.0: 接收到Activity指令，准备新增'当天提醒'...")
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val creationTime = sdf.format(Date())

        val newItem = TodayReminderItem(
            creationTime = creationTime,
            content = content,
            details = details
        )
        _reminders.update { currentList ->
            listOf(newItem) + currentList
        }
        saveReminders()
    }

    fun markAsReminded(reminderId: String) {
        Log.i(TAG, "V1.0: 接收到状态更新指令，正在将提醒[$reminderId]标记为'已提醒'...")
        _reminders.update { currentList ->
            val updatedList = currentList.map {
                if (it.id == reminderId) {
                    it.copy(reminderStatus = "已提醒")
                } else {
                    it
                }
            }
            // 将已提醒的项移动到列表末尾 (可选，但能优化UI体验)
            updatedList.sortedBy { it.reminderStatus != "待提醒" }
        }
        saveReminders()
        Log.i(TAG, "V1.0: 状态更新并持久化成功！")
    }
}
