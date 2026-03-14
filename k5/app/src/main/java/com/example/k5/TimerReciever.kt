package com.example.k5

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerReceiver : BroadcastReceiver() {

    companion object {
        var onTimeUpdate: ((Int) -> Unit)? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "TIMER_UPDATE") {
            val seconds = intent.getIntExtra("seconds", 0)
            onTimeUpdate?.invoke(seconds)
        }
    }
}