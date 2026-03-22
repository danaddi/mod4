package com.example.kt9

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class WeatherWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val city = inputData.getString("city") ?: return Result.failure()
        val index = inputData.getInt("index", 0)

        setForeground(createForegroundInfo("Загружаем погоду для $city"))

        delay((2000..5000).random().toLong())

        val temp = (-5..25).random()

        val output = Data.Builder()
            .putString("city", city)
            .putInt("temp", temp)
            .putInt("index", index)
            .build()

        return Result.success(output)
    }

    private fun createForegroundInfo(text: String): ForegroundInfo {

        val notification = NotificationCompat.Builder(applicationContext, "weather")
            .setContentTitle("Загрузка погоды")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()

        return ForegroundInfo(
            1,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }
}