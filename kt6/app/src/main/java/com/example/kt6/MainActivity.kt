package com.example.kt6

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kt6.SingleTimerService
import com.example.kt6.ui.theme.Kt6Theme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        setContent {
            Kt6Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen()
                }
            }
        }
    }
}

@Composable
fun TimerScreen() {
    val context = LocalContext.current
    var secondsInput by remember { mutableStateOf("") }
    var isTimerRunning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timerSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(isTimerRunning, timerSeconds) {
        if (isTimerRunning && timerSeconds > 0) {
            delay(timerSeconds * 1000L + 2000)
            isTimerRunning = false
            secondsInput = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Одноразовый таймер",
            fontSize = 24.sp,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = secondsInput,
            onValueChange = {
                secondsInput = it.filter { char -> char.isDigit() }
                errorMessage = null
            },
            label = { Text("Введите количество секунд") },
            placeholder = { Text("Например: 30") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = errorMessage != null,
            supportingText = errorMessage?.let {
                { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val seconds = secondsInput.toIntOrNull()
                when {
                    secondsInput.isBlank() -> {
                        errorMessage = "Введите количество секунд"
                    }
                    seconds == null || seconds <= 0 -> {
                        errorMessage = "Введите положительное число"
                    }
                    seconds > 3600 -> {
                        errorMessage = "Максимум 3600 секунд (1 час)"
                    }
                    else -> {
                        errorMessage = null
                        startTimer(context, seconds)
                        isTimerRunning = true
                        timerSeconds = seconds
                    }
                }
            },
            enabled = !isTimerRunning,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (isTimerRunning) "Таймер запущен" else "Запустить таймер",
                fontSize = 16.sp
            )
        }
    }
}

private fun startTimer(context: android.content.Context, seconds: Int) {
    val intent = Intent(context, SingleTimerService::class.java).apply {
        action = SingleTimerService.ACTION_START_TIMER
        putExtra(SingleTimerService.EXTRA_SECONDS, seconds)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }

    Toast.makeText(context, "Таймер запущен на $seconds секунд", Toast.LENGTH_SHORT).show()
}