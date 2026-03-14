package com.example.k5


import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.k5.ui.theme.K5Theme
import com.example.k5.ui.TimerScreen

class MainActivity : ComponentActivity() {
    private var mService: TimerForegroundService? = null
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerForegroundService.TimerBinder
            mService = binder.getService()
            mBound = true

            mService?.setTimeUpdateListener { seconds ->
                updateTimerState(seconds)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            mBound = false
        }
    }

    private val timerReceiver = TimerReceiver()
    private var timerState by mutableStateOf(TimerState())

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

        IntentFilter("TIMER_UPDATE").also {
            ContextCompat.registerReceiver(
                this,
                timerReceiver,
                it,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        TimerReceiver.onTimeUpdate = { seconds ->
            updateTimerState(seconds)
        }

        setContent {
            K5Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen(
                        timerState = timerState,
                        onStartClick = { startTimer() },
                        onStopClick = { stopTimer() }
                    )
                }
            }
        }
    }

    private fun updateTimerState(seconds: Int) {
        timerState = timerState.copy(
            seconds = seconds,
            isRunning = true
        )
    }

    private fun startTimer() {
        val intent = Intent(this, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun stopTimer() {
        if (mBound) {
            unbindService(connection)
            mBound = false
        }

        val intent = Intent(this, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        startService(intent)

        timerState = TimerState()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBound) {
            unbindService(connection)
        }
        unregisterReceiver(timerReceiver)
        TimerReceiver.onTimeUpdate = null
    }
}
