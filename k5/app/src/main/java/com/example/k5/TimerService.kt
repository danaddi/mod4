package com.example.k5

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Binder
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class TimerForegroundService : Service() {

    private val binder = TimerBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var elapsedSeconds = 0
    private var isRunning = false
    private var timerJob: Job? = null

    private var onTimeUpdateListener: ((Int) -> Unit)? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "timer_channel"
        const val CHANNEL_NAME = "Timer Service Channel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UPDATE = "ACTION_UPDATE"
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_STOP -> stopTimer()
            ACTION_UPDATE -> sendTimeUpdate()
        }
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun startTimer() {
        if (isRunning) return
        isRunning = true
        startForeground(NOTIFICATION_ID, createNotification(elapsedSeconds).build())
        timerJob = serviceScope.launch {
            while (isRunning) {
                delay(1000)
                elapsedSeconds++
                sendTimeUpdate()
                updateNotification(elapsedSeconds)
            }
        }
    }

    private fun stopTimer() {
        isRunning = false
        timerJob?.cancel()
        elapsedSeconds = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun sendTimeUpdate() {
        onTimeUpdateListener?.invoke(elapsedSeconds)
        val updateIntent = Intent("TIMER_UPDATE").apply {
            putExtra("seconds", elapsedSeconds)
        }
        sendBroadcast(updateIntent)
    }

    private fun updateNotification(seconds: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(seconds).build())
    }

    private fun createNotification(seconds: Int): NotificationCompat.Builder {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        val timeText = when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, secs)
            else -> String.format("%02d:%02d", minutes, secs)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, notificationIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Таймер")
            .setContentText("Прошло: $timeText")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Канал для сервиса таймера"
                setSound(null, null)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }

    fun setTimeUpdateListener(listener: (Int) -> Unit) {
        onTimeUpdateListener = listener
    }
}