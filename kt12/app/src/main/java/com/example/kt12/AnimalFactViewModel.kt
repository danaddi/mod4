package com.example.kt12

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class AnimalFactViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentFact = MutableStateFlow<String?>(null)
    val currentFact = _currentFact.asStateFlow()

    fun getRandomFact(): Flow<String> = flow {
        _isLoading.value = true
        _currentFact.value = null

        delay((1500..3000).random().toLong())

        val fact = AnimalFacts.getRandomFact()
        emit(fact)

        _currentFact.value = fact
        _isLoading.value = false
    }

    fun generateFact() {
        viewModelScope.launch {
            getRandomFact().collect { fact ->
                println("Fact generated: $fact")
            }
        }
    }

    fun refreshFact() {
        generateFact()
    }
}