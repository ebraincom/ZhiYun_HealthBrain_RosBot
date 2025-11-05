// ✅✅✅【【【【 这是 V1.0 当天提醒 ViewModel：大脑构建，完美复刻版！！！ 】】】】✅✅✅
package com.zhiyun.agentrobot.ui.todayreminder // ✅ 1. 包名已更新

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.parcelize.Parcelize
import android.os.Parcelable





// ✅ 3. ViewModel类名及常量已更新
class TodayReminderViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "TodayReminder_VM_V1.0"
        private const val PREFS_NAME = "today_reminder_prefs"
        private const val REMINDERS_KEY = "today_reminders_list_key"
    }
    private val gson = Gson()
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _reminders = MutableStateFlow<List<TodayReminderItem>>(emptyList())
    val reminders = _reminders.asStateFlow()
    // ✅ 6. Channel的事件类型也更新为新的 PlanReminderItem
    private val _alarmEventChannel = Channel< TodayReminderItem>()
    val alarmEventFlow = _alarmEventChannel.receiveAsFlow()


    init {
        Log.i(TAG, "ViewModel V1.0 初始化！开始从 SharedPreferences 恢复'当天提醒'数据...")
        loadRemindersFromPrefs()
    }
    private fun loadRemindersFromPrefs() {
        val json = prefs.getString(REMINDERS_KEY, null)
        if (json != null) {
            try {
                // ✅ 7. 反序列化的目标类型已更新
                val type = object : TypeToken<List<TodayReminderItem>>() {}.type
                val savedList: List<TodayReminderItem> = gson.fromJson(json, type)
                _reminders.value = savedList
                Log.i(TAG, "V1.3: 成功从 SharedPreferences 恢复了 ${savedList.size} 个'计划提醒'项。")
            } catch (e: Exception) {
                Log.e(TAG, "V1.3: 从 SharedPreferences 解析'计划提醒'数据失败！", e)
            }
        } else {
            Log.i(TAG, "V1.3: SharedPreferences 中没有找到'计划提醒'数据。")
        }
    }
    private fun saveRemindersToPrefs(list: List<TodayReminderItem>) {
        try {
            val json = gson.toJson(list)
            prefs.edit().putString(REMINDERS_KEY, json).apply()
            Log.i(TAG, "V1.0: 已成功将 ${list.size} 个'计划提醒'项写入 SharedPreferences。")
        } catch (e: Exception) {
            Log.e(TAG, "V1.0: 写入 SharedPreferences 失败！", e)
        }
    }


    // ✅ 4. 方法参数及内部逻辑已适配为 TodayReminderItem
    fun addReminder(
        content: String,
        details: String,
        timePoints: String,
        stopCondition: String?
    ) {
        viewModelScope.launch {
            Log.d(TAG, "V1.3: 接收到Activity指令...")
            val newItem = TodayReminderItem(
                creationTime = SimpleDateFormat("yyyy/MM/dd HH:mm 创建", Locale.getDefault()).format(Date()),
                content = content,
                details = details,
                reminderTimePoints = timePoints,
                stopCondition = stopCondition,
                reminderStatus = "待提醒"
            )

            val newList = listOf(newItem) + _reminders.value // 使用 listOf(newItem) + ...
            _reminders.value = newList
            saveRemindersToPrefs(newList)

            _alarmEventChannel.send(newItem)
        }
    }
    // ✅✅✅ 【【【【 1. 核心修正：新增“按ID删除”的核心方法！！！ 】】】】 ✅✅✅
    /**
     * 根据指定的ID删除一个提醒事项。
     * @param id 要删除的提醒事项的唯一ID。
     */
    fun deleteReminderById(id: String) {
        viewModelScope.launch {
            val currentList = _reminders.value
            // 过滤掉ID匹配的元素，生成新列表
            val newList = currentList.filterNot { it.id == id }

            // 确认有元素被删除后再更新，避免不必要的重组和保存
            if (newList.size < currentList.size) {
                _reminders.value = newList
                // 严格遵从您的实现，调用 saveRemindersToPrefs 保存
                saveRemindersToPrefs(newList)
                Log.d(TAG, "V2.2: 已成功通过ID删除提醒: $id")
            } else {
                Log.w(TAG, "V2.2: 尝试通过ID删除提醒失败，未找到ID: $id")
            }
        }
    }
    // ✅✅✅ 【【【【 2. 核心修正：新增“批量删除已完成”的核心方法！！！ 】】】】 ✅✅✅
    /**
     * 删除所有状态为 "已提醒" 的事项，并返回成功删除的数量。
     * 这个方法是suspend函数，因为它会在协程中被调用，并且返回一个结果。
     * @return 实际删除的提醒数量。
     */
    suspend fun deleteAllCompleted(): Int {
        val currentList = _reminders.value
        // 过滤掉状态为“已提醒”的元素，生成新列表
        val newList = currentList.filterNot { it.reminderStatus == "已提醒" }
        val deletedCount = currentList.size - newList.size

        if (deletedCount > 0) {
            _reminders.value = newList
            // 严格遵从您的实现，调用 saveRemindersToPrefs 保存
            saveRemindersToPrefs(newList)
            Log.d(TAG, "V2.2: 已成功批量删除 ${deletedCount} 条已完成的提醒。")
        } else {
            Log.d(TAG, "V2.2: 没有找到已完成的提醒可供批量删除。")
        }

        return deletedCount // 返回删除的数量，供Activity中的TTS播报使用
    }
    fun markAsReminded(reminderId: String) {
        viewModelScope.launch {
            val currentList = _reminders.value
            val targetIndex = currentList.indexOfFirst { it.id == reminderId }

            if (targetIndex != -1 && currentList[targetIndex].reminderStatus == "待提醒") {
                Log.i(TAG, "V1.0: 接收到状态更新指令，正在将提醒[$reminderId]标记为'已提醒'...")
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
@Parcelize // ✅ 1. 注解
data class TodayReminderItem(
    val id: String = UUID.randomUUID().toString(),
    val creationTime: String,
    val content: String,
    val details: String,
    val reminderTimePoints: String,
    val stopCondition: String?,
    val reminderStatus: String
) : Parcelable // ✅ 2. 继承Parcelable接口





