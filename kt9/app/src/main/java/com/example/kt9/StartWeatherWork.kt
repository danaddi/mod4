package com.example.kt9

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

fun startWeatherWork(context: Context) {

    val cities = listOf("Москва", "Лондон", "Нью-Йорк")

    val workers = cities.mapIndexed { index, city ->
        val data = Data.Builder()
            .putString("city", city)
            .putInt("index", index)
            .build()

        OneTimeWorkRequestBuilder<WeatherWorker>()
            .setInputData(data)
            .addTag(city)  // Добавляем тег для идентификации города
            .build()
    }

    val reportWorker = OneTimeWorkRequestBuilder<ReportWorker>()
        .build()

    WorkManager.getInstance(context)
        .beginUniqueWork(
            "weather_work",
            ExistingWorkPolicy.REPLACE,
            workers
        )
        .then(reportWorker)
        .enqueue()
}