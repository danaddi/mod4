package com.example.kt8

enum class ProcessingStep {
    IDLE,
    COMPRESSING,
    ADDING_WATERMARK,
    UPLOADING,
    COMPLETED,
    ERROR
}

data class ProcessingState(
    val currentStep: ProcessingStep = ProcessingStep.IDLE,
    val progress: Int = 0,
    val resultMessage: String = "",
    val errorMessage: String = "",
    val outputFileName: String = ""
)