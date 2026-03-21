package com.example.kt8

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class PhotoProcessingViewModel(
    private val context: Context
) : ViewModel() {

    private val processor = PhotoProcessor(context)

    val state: StateFlow<ProcessingState> = processor.state

    fun startProcessing() {
        viewModelScope.launch {
            processor.processAndUpload()
        }
    }

    fun reset() {
        processor.reset()
    }

    fun selectDemoPhoto() {
        processor.selectPhoto("/storage/emulated/0/DCIM/demo_photo.jpg")
    }
}