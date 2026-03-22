package com.example.kt9

import android.Manifest
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class ReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        setForeground(createForegroundInfo("Формируем итоговый отчёт..."))

        val temps = mutableListOf<Int>()
        val cities = mutableListOf<String>()

        inputData.keyValueMap.forEach { (key, value) ->
            when {
                key.startsWith("city_") -> {
                    cities.add(value as String)
                }
                key.startsWith("temp_") -> {
                    temps.add(value as Int)
                }
            }
        }

        delay(1000)

        val avg = if (temps.isNotEmpty()) temps.average() else 0.0

        showFinalNotification(cities, temps, avg)

        return Result.success()
    }

    private fun createForegroundInfo(text: String): ForegroundInfo {

        val notification = NotificationCompat.Builder(applicationContext, "weather")
            .setContentTitle("Погода")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()

        return ForegroundInfo(
            2,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showFinalNotification(cities: List<String>, temps: List<Int>, avg: Double) {

        val resultsText = cities.zip(temps).joinToString("\n") {
            "${it.first}: ${it.second}°C"
        }

        val notification = NotificationCompat.Builder(applicationContext, "weather")
            .setContentTitle("Отчёт готов")
            .setContentText("Средняя температура ${avg.toInt()}°C")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Средняя: ${avg.toInt()}°C\n\n$resultsText"))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(3, notification)
    }
}