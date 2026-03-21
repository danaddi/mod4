package com.example.kt8

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.random.Random
import java.io.File

class PhotoProcessor(private val context: Context) {

    private val _state = MutableStateFlow(ProcessingState())
    val state: StateFlow<ProcessingState> = _state.asStateFlow()

    private var currentPhotoPath: String? = null

    fun selectPhoto(photoPath: String) {
        currentPhotoPath = photoPath
    }

    suspend fun processAndUpload() {
        try {
            _state.value = ProcessingState(
                currentStep = ProcessingStep.COMPRESSING,
                progress = 0,
                resultMessage = "Сжимаем фото..."
            )

            val compressedPath = compressPhoto(currentPhotoPath ?: return)

            _state.value = ProcessingState(
                currentStep = ProcessingStep.ADDING_WATERMARK,
                progress = 0,
                resultMessage = "Добавляем водяной знак..."
            )

            val watermarkedPath = addWatermark(compressedPath)

            _state.value = ProcessingState(
                currentStep = ProcessingStep.UPLOADING,
                progress = 0,
                resultMessage = "Загружаем в облако..."
            )

            val uploadedUrl = uploadToCloud(watermarkedPath)

            _state.value = ProcessingState(
                currentStep = ProcessingStep.COMPLETED,
                progress = 100,
                resultMessage = "Готово! Фото загружено",
                outputFileName = uploadedUrl
            )

        } catch (e: Exception) {
            _state.value = ProcessingState(
                currentStep = ProcessingStep.ERROR,
                progress = 0,
                errorMessage = "Ошибка: ${e.message}",
                resultMessage = "Ошибка обработки"
            )
        }
    }

    private suspend fun compressPhoto(inputPath: String): String = withContext(Dispatchers.IO) {
        for (progress in 0..100 step 10) {
            delay(150)
            _state.value = _state.value.copy(progress = progress)
        }

        if (Random.nextInt(100) < 10) {
            throw RuntimeException("Не удалось сжать фото")
        }

        val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        outputFile.createNewFile()
        outputFile.absolutePath
    }

    private suspend fun addWatermark(inputPath: String): String = withContext(Dispatchers.IO) {
        for (progress in 0..100 step 10) {
            delay(120)
            _state.value = _state.value.copy(progress = progress)
        }

        if (Random.nextInt(100) < 10) {
            throw RuntimeException("Не удалось добавить водяной знак")
        }

        val outputFile = File(context.cacheDir, "watermarked_${System.currentTimeMillis()}.jpg")
        outputFile.createNewFile()
        outputFile.absolutePath
    }

    private suspend fun uploadToCloud(inputPath: String): String = withContext(Dispatchers.IO) {
        for (progress in 0..100 step 10) {
            delay(180)
            _state.value = _state.value.copy(progress = progress)
        }

        if (Random.nextInt(100) < 10) {
            throw RuntimeException("Ошибка загрузки в облако")
        }

        "https://cloud-storage.com/photos/${System.currentTimeMillis()}.jpg"
    }

    fun reset() {
        _state.value = ProcessingState()
    }
}