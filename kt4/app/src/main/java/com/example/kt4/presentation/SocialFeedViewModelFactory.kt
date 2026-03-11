package com.example.kt4.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kt4.data.datasource.SocialDataSource
import com.example.kt4.data.repository.SocialRepository

class SocialFeedViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialFeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SocialFeedViewModel(
                repository = SocialRepository(SocialDataSource(context))
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

fun socialFeedViewModelFactory(context: Context): SocialFeedViewModelFactory {
    return SocialFeedViewModelFactory(context)
}