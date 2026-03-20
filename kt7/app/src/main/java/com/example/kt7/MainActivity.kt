package com.example.kt7

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.kt7.ui.theme.Kt7Theme


class MainActivity : ComponentActivity() {

    private var randomService: RandomNumberService? = null
    private var isBound = false

    private val _connectionState = mutableStateOf(ConnectionState.DISCONNECTED)
    val connectionState: ConnectionState get() = _connectionState.value

    private val _currentNumber = mutableStateOf(0)
    val currentNumber: Int get() = _currentNumber.value

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RandomNumberService.RandomNumberBinder
            randomService = binder.getService()
            isBound = true

            randomService?.addListener { number ->
                _currentNumber.value = number
            }

            randomService?.startGenerating()
            _connectionState.value = ConnectionState.CONNECTED
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            randomService = null
            isBound = false
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Kt7Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RandomNumberScreen(
                        connectionState = connectionState,
                        currentNumber = currentNumber,
                        onConnect = { connectToService() },
                        onDisconnect = { disconnectFromService() }
                    )
                }
            }
        }
    }

    private fun connectToService() {
        _connectionState.value = ConnectionState.CONNECTING
        val intent = Intent(this, RandomNumberService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun disconnectFromService() {
        if (isBound) {
            randomService?.stopGenerating()
            unbindService(connection)
            isBound = false
            randomService = null
        }
        _connectionState.value = ConnectionState.DISCONNECTED
        _currentNumber.value = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            disconnectFromService()
        }
    }
}
