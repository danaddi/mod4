package com.example.kt7

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import kotlin.random.Random

class RandomNumberService : Service() {

    private val binder = RandomNumberBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var isGenerating = false
    private var generatorJob: Job? = null
    private var currentNumber = 0

    private var listeners = mutableListOf<(Int) -> Unit>()

    companion object {
        const val TAG = "RandomNumberService"
    }

    inner class RandomNumberBinder : Binder() {
        fun getService(): RandomNumberService = this@RandomNumberService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "Service bound")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound")
        stopGenerating()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        stopGenerating()
        serviceScope.cancel()
        super.onDestroy()
    }

    fun startGenerating() {
        if (isGenerating) return

        isGenerating = true
        currentNumber = Random.nextInt(0, 101)
        notifyListeners(currentNumber)

        generatorJob = serviceScope.launch {
            while (isGenerating) {
                delay(1000)
                currentNumber = Random.nextInt(0, 101)
                Log.d(TAG, "Generated number: $currentNumber")
                notifyListeners(currentNumber)
            }
        }
    }

    fun stopGenerating() {
        isGenerating = false
        generatorJob?.cancel()
    }

    fun getCurrentNumber(): Int = currentNumber

    fun addListener(listener: (Int) -> Unit) {
        listeners.add(listener)
        listener(currentNumber)
    }

    fun removeListener(listener: (Int) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners(number: Int) {
        listeners.forEach {
            Log.d(TAG, "Calling listener: $it")
            it(number)
        }
    }
}