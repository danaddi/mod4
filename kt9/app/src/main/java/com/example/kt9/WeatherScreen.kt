package com.example.kt9

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.compose.material3.CardDefaults

@Composable
fun WeatherScreen(
    onStartWork: () -> Unit,
    workManager: WorkManager
) {
    val context = LocalContext.current

    // Состояние для городов
    var citiesData by remember {
        mutableStateOf(
            listOf(
                WeatherData("Москва"),
                WeatherData("Лондон"),
                WeatherData("Нью-Йорк")
            )
        )
    }

    // Отслеживаем статус работы
    val workInfos by workManager
        .getWorkInfosForUniqueWorkLiveData("weather_work")
        .observeAsState()

    // Проверяем, есть ли активные воркеры
    val isWorkRunning = workInfos?.any {
        it.state == WorkInfo.State.RUNNING
    } ?: false

    // Проверяем, завершена ли работа
    val isWorkCompleted = workInfos?.all {
        it.state == WorkInfo.State.SUCCEEDED
    } == true && workInfos?.isNotEmpty() == true

    // Обновляем статусы на основе данных из WorkManager
    workInfos?.forEach { workInfo ->
        if (workInfo.state == WorkInfo.State.RUNNING) {
            val city = workInfo.tags.firstOrNull()
            citiesData = citiesData.map { cityData ->
                if (cityData.city == city) {
                    cityData.copy(status = WeatherStatus.LOADING)
                } else {
                    cityData
                }
            }
        }

        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
            val city = workInfo.outputData.getString("city") ?: return@forEach
            val temp = workInfo.outputData.getInt("temp", 0)

            citiesData = citiesData.map { cityData ->
                if (cityData.city == city) {
                    cityData.copy(
                        temperature = temp,
                        status = WeatherStatus.SUCCEEDED
                    )
                } else {
                    cityData
                }
            }
        }
    }

    // Вычисляем среднюю температуру
    val avgTemp = if (citiesData.all { it.status == WeatherStatus.SUCCEEDED }) {
        citiesData.mapNotNull { it.temperature }.average().toInt()
    } else null

    // Формируем описание погоды
    fun getWeatherDescription(temp: Int): String {
        return when {
            temp > 20 -> "ясно"
            temp > 10 -> "облачно"
            temp > 0 -> "дождь"
            else -> "снег"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = "Прогноз погоды",
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Список городов
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(citiesData) { city ->
                CityWeatherCard(city)
            }
        }

        // Статус загрузки
        if (isWorkRunning && !isWorkCompleted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(24.dp)
                        .padding(end = 8.dp)
                )
                Text("Загрузка... (${citiesData.count { it.status == WeatherStatus.SUCCEEDED }}/${citiesData.size} готово)")
            }
        }

        // Итоговый прогноз
        if (isWorkCompleted && avgTemp != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Итоговый прогноз:",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    citiesData.forEach { city ->
                        Text(
                            text = "${city.city}: ${city.temperature}°C, ${getWeatherDescription(city.temperature ?: 0)}",
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Средняя температура: $avgTemp°C",
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка
        Button(
            onClick = onStartWork,
            enabled = !isWorkRunning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isWorkRunning) "Загрузка..." else "Собрать прогноз")
        }
    }
}

@Composable
fun CityWeatherCard(weatherData: WeatherData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = weatherData.city,
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = when (weatherData.status) {
                        WeatherStatus.PENDING -> "Ожидание"
                        WeatherStatus.LOADING -> "Загружается..."
                        WeatherStatus.SUCCEEDED -> "Готово"
                        WeatherStatus.FAILED -> "Ошибка"
                    },
                    fontSize = 14.sp,
                    color = when (weatherData.status) {
                        WeatherStatus.SUCCEEDED -> MaterialTheme.colorScheme.primary
                        WeatherStatus.LOADING -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }

            when (weatherData.status) {
                WeatherStatus.LOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.height(32.dp)
                    )
                }
                WeatherStatus.SUCCEEDED -> {
                    Text(
                        text = "${weatherData.temperature}°C",
                        fontSize = 24.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                else -> {
                    Text(
                        text = "--°C",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
