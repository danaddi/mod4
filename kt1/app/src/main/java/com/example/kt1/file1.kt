package com.example.kt1

import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import kotlin.system.measureTimeMillis
import kotlin.random.Random

data class User(val id: Int, val name: String)
data class SaleItem(val product: String, val qty: Int, val revenue: Int)
data class SalesData(val today: String, val items: List<SaleItem>)
data class Weather(val city: String, val temp: Int, val condition: String)

suspend fun loadUsers(): List<String> {
    return withContext(Dispatchers.IO) {
        println("Загрузка пользователей...")
        delay(1800)

        if (Random.nextInt(100) < 20) {
            throw RuntimeException("Ошибка загрузки пользователей: сервер не отвечает")
        }

        val users = listOf(
            User(1, "Alice"),
            User(2, "Bob"),
            User(3, "Ivan"),
            User(4, "Olga")
        )
        println("Пользователи загружены")
        users.map { it.name }
    }
}

suspend fun loadSales(): Map<String, Int> {
    return withContext(Dispatchers.IO) {
        println("Загрузка статистики продаж...")
        delay(1200)

        if (Random.nextInt(100) < 20) {
            throw RuntimeException("Ошибка загрузки продаж: данные повреждены")
        }

        val sales = SalesData(
            today = "2025-12-01",
            items = listOf(
                SaleItem("Coffee", 42, 1680),
                SaleItem("Tea", 19, 475)
            )
        )
        println("Статистика продаж загружена")
        mapOf(
            "total_items" to sales.items.sumOf { it.qty },
            "total_revenue" to sales.items.sumOf { it.revenue }
        )
    }
}

suspend fun loadWeather(): List<String> {
    return withContext(Dispatchers.IO) {
        println("Загрузка погоды...")
        delay(2500)

        if (Random.nextInt(100) < 20) {
            throw RuntimeException("Ошибка загрузки погоды: таймаут соединения")
        }

        val weatherData = listOf(
            Weather("Москва", -18, "snow"),
            Weather("Нью-Йорк", -5, "cloudy"),
            Weather("Токио", 11, "rain")
        )
        println("Погода загружена")
        weatherData.map { "${it.city}: ${it.temp}°C" }
    }
}

suspend fun <T> runCatchingAsync(
    taskName: String,
    block: suspend () -> T
): Result<T> = runCatching {
    block()
}.onFailure { error ->
    println("Ошибка в задаче '$taskName': ${error.message}")
}

fun main() {
    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))
    runBlocking {
        println("Начинаем параллельную загрузку данных...\n")

        val totalTime = measureTimeMillis {
            val usersDeferred = async { runCatchingAsync("Пользователи") { loadUsers() } }
            val salesDeferred = async { runCatchingAsync("Продажи") { loadSales() } }
            val weatherDeferred = async { runCatchingAsync("Погода") { loadWeather() } }

            val usersResult = usersDeferred.await()
            val salesResult = salesDeferred.await()
            val weatherResult = weatherDeferred.await()

            println("\n" + "=".repeat(50))
            println("РЕЗУЛЬТАТЫ ВЫПОЛНЕНИЯ:")
            println("=".repeat(50))

            val allSuccess = listOf(usersResult, salesResult, weatherResult).all { it.isSuccess }

            if (allSuccess) {
                println("\nВсе задачи выполнены успешно!\n")
                usersResult.getOrNull()?.let { users ->
                    println("Пользователи (${users.size}):")
                    users.forEach { println("   - $it") }
                }
                salesResult.getOrNull()?.let { sales ->
                    println("\nСтатистика продаж:")
                    sales.forEach { (key, value) ->
                        println("   - $key: $value")
                    }
                }
                weatherResult.getOrNull()?.let { weather ->
                    println("\nПогода:")
                    weather.forEach { println("   - $it") }
                }
            } else {
                println("\nНе все задачи завершились успешно!")
                println("\nДетали ошибок:")

                usersResult.exceptionOrNull()?.let {
                    println("   - Пользователи: ${it.message}")
                }
                salesResult.exceptionOrNull()?.let {
                    println("   - Продажи: ${it.message}")
                }
                weatherResult.exceptionOrNull()?.let {
                    println("   - Погода: ${it.message}")
                }
            }
        }
        println("\n" + "=".repeat(50))
        println("Общее время выполнения: ${totalTime}мс")
        println("=".repeat(50))

    }
}