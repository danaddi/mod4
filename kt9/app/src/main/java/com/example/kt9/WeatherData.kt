package com.example.kt9

data class WeatherData(
    val city: String,
    val temperature: Int? = null,
    val status: WeatherStatus = WeatherStatus.PENDING
)

enum class WeatherStatus {
    PENDING,    // Ожидание
    LOADING,    // Загружается
    SUCCEEDED,  // Готово
    FAILED      // Ошибка
}