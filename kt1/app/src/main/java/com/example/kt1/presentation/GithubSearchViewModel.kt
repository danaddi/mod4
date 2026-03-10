package com.example.kt1.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kt1.data.RepositoryDataSource
import com.example.kt1.domain.Repository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GithubSearchViewModel(
    private val dataSource: RepositoryDataSource
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<Repository>>(emptyList())
    val searchResults: StateFlow<List<Repository>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query

        //отменяем предыдущий поиск
        searchJob?.cancel()

        //начинаем новый с debounce
        searchJob = viewModelScope.launch {
            delay(500)

            if (query.isBlank()) {
                _searchResults.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            try {
                val results = dataSource.searchRepositories(query)
                _searchResults.value = results
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка поиска: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun preloadData() {
        viewModelScope.launch {
            try {
                dataSource.loadAllRepositories()
            } catch (e: Exception) {
                //игнорируем, так как это просто предзагрузка
            }
        }
    }
}