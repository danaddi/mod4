package com.example.kt11

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val reminderManager = ReminderManager(application)

    private val _isEnabled = MutableLiveData<Boolean>()
    val isEnabled: LiveData<Boolean> = _isEnabled

    private val _nextReminderText = MutableLiveData<String>()
    val nextReminderText: LiveData<String> = _nextReminderText

    private val _statusText = MutableLiveData<String>()
    val statusText: LiveData<String> = _statusText

    private val _hasExactAlarmPermission = MutableLiveData<Boolean>()
    val hasExactAlarmPermission: LiveData<Boolean> = _hasExactAlarmPermission

    init {
        loadState()
        updateNextReminderText()
        checkExactAlarmPermission()
    }

    private fun loadState() {
        val enabled = prefs.getBoolean("reminder_enabled", false)
        _isEnabled.value = enabled
        _statusText.value = if (enabled) "Включено" else "Выключено"
    }

    private fun updateNextReminderText() {
        if (_isEnabled.value == true) {
            _nextReminderText.value = "Следующее напоминание: ${reminderManager.getNextReminderTimeString()}"
        } else {
            _nextReminderText.value = "Напоминание выключено"
        }
    }

    fun checkExactAlarmPermission() {
        _hasExactAlarmPermission.value = reminderManager.isExactAlarmPermissionGranted()
    }

    fun requestExactAlarmPermission() {
        reminderManager.requestExactAlarmPermission()
    }

    fun enableReminder() {
        viewModelScope.launch {
            try {
                if (!reminderManager.isExactAlarmPermissionGranted()) {
                    _statusText.value = "Требуется разрешение на точные будильники"
                    return@launch
                }

                val success = reminderManager.scheduleReminder()

                if (success) {
                    prefs.edit().putBoolean("reminder_enabled", true).apply()
                    _isEnabled.value = true
                    _statusText.value = "Включено"
                    updateNextReminderText()
                } else {
                    _statusText.value = "Не удалось включить напоминание"
                }
            } catch (e: Exception) {
                _statusText.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun disableReminder() {
        reminderManager.cancelReminder()
        prefs.edit().putBoolean("reminder_enabled", false).apply()
        _isEnabled.value = false
        _statusText.value = "Выключено"
        updateNextReminderText()
    }

    fun refresh() {
        loadState()
        updateNextReminderText()
        checkExactAlarmPermission()

        if (_isEnabled.value == true) {
            reminderManager.scheduleReminder()
            updateNextReminderText()
        }
    }
}