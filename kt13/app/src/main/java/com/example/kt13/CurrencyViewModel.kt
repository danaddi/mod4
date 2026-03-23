package com.example.kt13


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class CurrencyViewModel : ViewModel() {

    private val _currencyRate = MutableStateFlow(90.5)
    val currencyRate: StateFlow<Double> = _currencyRate.asStateFlow()

    private var lastRate = 90.5

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    init {
        startAutoUpdate()
    }

    private fun startAutoUpdate() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                updateRate()
            }
        }
    }

    fun manualUpdate() {
        viewModelScope.launch {
            _isUpdating.value = true
            updateRate()
            delay(500)
            _isUpdating.value = false
        }
    }

    private fun updateRate() {
        lastRate = _currencyRate.value

        val change = Random.nextDouble(-2.0, 2.0)
        var newRate = _currencyRate.value + change

        newRate = String.format("%.2f", newRate).toDouble()

        _currencyRate.value = newRate
    }

    fun getTrend(): Trend {
        return when {
            _currencyRate.value > lastRate -> Trend.UP
            _currencyRate.value < lastRate -> Trend.DOWN
            else -> Trend.STABLE
        }
    }
}

enum class Trend {
    UP, DOWN, STABLE
}