package com.example.kt14

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompassViewModel : ViewModel() {

    private lateinit var sensorManager: CompassSensorManager

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(true)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    fun init(context: Context) {
        if (!::sensorManager.isInitialized) {
            sensorManager = CompassSensorManager(context)

            viewModelScope.launch {
                sensorManager.azimuth.collect { newAzimuth ->
                    _azimuth.value = newAzimuth
                }
            }

            viewModelScope.launch {
                sensorManager.isSensorAvailable.collect { available ->
                    _isSensorAvailable.value = available
                }
            }
        }
    }

    fun startListening() {
        if (::sensorManager.isInitialized && _isSensorAvailable.value) {
            sensorManager.startListening()
        }
    }

    fun stopListening() {
        if (::sensorManager.isInitialized) {
            sensorManager.stopListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::sensorManager.isInitialized) {
            sensorManager.stopListening()
        }
    }
}