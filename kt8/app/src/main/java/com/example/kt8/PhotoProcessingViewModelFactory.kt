package com.example.kt8

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PhotoProcessingViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhotoProcessingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhotoProcessingViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}