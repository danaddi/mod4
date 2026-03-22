package com.example.kt11


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val isEnabled = prefs.getBoolean("reminder_enabled", false)

            if (isEnabled) {
                val reminderManager = ReminderManager(context)
                reminderManager.scheduleReminder()
            }
        }
    }
}