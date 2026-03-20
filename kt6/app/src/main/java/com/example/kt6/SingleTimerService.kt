package com.example.kt6

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class SingleTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null
    private var timerSeconds = 0

    companion object {
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "timer_channel"
        const val CHANNEL_NAME = "Timer Channel"
        const val ACTION_START_TIMER = "ACTION_START_TIMER"
        const val EXTRA_SECONDS = "EXTRA_SECONDS"
        const val TAG = "SingleTimerService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                timerSeconds = intent.getIntExtra(EXTRA_SECONDS, 30)
                Log.d(TAG, "Starting timer for $timerSeconds seconds")
                startTimer()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer() {
        startForeground(NOTIFICATION_ID, createTimerRunningNotification(timerSeconds).build())

        timerJob = serviceScope.launch {
            try {
                for (i in timerSeconds downTo 1) {
                    Log.d(TAG, "Time left: $i seconds")
                    updateNotification(i)
                    delay(1000)
                }

                showCompletionNotification()

                delay(2000)

                stopSelf()

            } catch (e: CancellationException) {
                Log.d(TAG, "Timer cancelled")
                stopSelf()
            } catch (e: Exception) {
                Log.e(TAG, "Error in timer", e)
                stopSelf()
            }
        }
    }

    private fun updateNotification(secondsLeft: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createTimerRunningNotification(secondsLeft).build())
    }

    private fun createTimerRunningNotification(secondsLeft: Int): NotificationCompat.Builder {
        val timeText = when {
            secondsLeft >= 60 -> {
                val minutes = secondsLeft / 60
                val secs = secondsLeft % 60
                String.format("%02d:%02d осталось", minutes, secs)
            }
            else -> "$secondsLeft сек осталось"
        }

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, notificationIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Таймер запущен")
            .setContentText(timeText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    private fun showCompletionNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, notificationIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Таймер завершён!")
            .setContentText("Время вышло")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        stopForeground(STOP_FOREGROUND_REMOVE)

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Канал для таймера"
                setSound(null, null)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null // Не поддерживаем привязку

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        timerJob?.cancel()
        serviceScope.cancel()
    }
}