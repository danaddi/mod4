package com.example.kt1.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kt1.data.RepositoryDataSource

class GithubSearchViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GithubSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GithubSearchViewModel(
                dataSource = RepositoryDataSource(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

fun githubSearchViewModelFactory(context: Context): GithubSearchViewModelFactory {
    return GithubSearchViewModelFactory(context)
}