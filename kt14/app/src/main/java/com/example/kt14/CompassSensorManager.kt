package com.example.kt14

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CompassSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var rotationSensor: Sensor? = null

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(true)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private var lastAzimuth = 0f
    private var lastTimestamp = 0L

    init {
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
            if (rotationSensor == null) {
                _isSensorAvailable.value = false
                Log.e("Compass", "Compass sensor not available")
            }
        }
    }

    fun startListening() {
        if (rotationSensor != null) {
            sensorManager.registerListener(
                this,
                rotationSensor,
                SensorManager.SENSOR_DELAY_UI  // Обновление 60 раз в секунду
            )
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)

                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)

                    var azimuthRad = orientation[0]
                    var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()

                    azimuthDeg = (azimuthDeg + 360) % 360

                    _azimuth.value = smoothAzimuth(azimuthDeg)
                }

                Sensor.TYPE_ORIENTATION -> {
                    var azimuthDeg = it.values[0]
                    azimuthDeg = (azimuthDeg + 360) % 360
                    _azimuth.value = smoothAzimuth(azimuthDeg)
                }
            }
        }
    }

    private fun smoothAzimuth(newAzimuth: Float): Float {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastTimestamp

        if (timeDiff > 0 && timeDiff < 500) {
            val smoothed = lastAzimuth * 0.7f + newAzimuth * 0.3f
            lastAzimuth = smoothed
            lastTimestamp = currentTime
            return smoothed
        } else {
            lastAzimuth = newAzimuth
            lastTimestamp = currentTime
            return newAzimuth
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}