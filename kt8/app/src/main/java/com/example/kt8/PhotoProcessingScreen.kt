package com.example.kt8.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kt8.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoProcessingScreen(
    viewModel: PhotoProcessingViewModel
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.selectDemoPhoto()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Обработка фото") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StatusHeader(state = state)

            if (state.currentStep != ProcessingStep.IDLE &&
                state.currentStep != ProcessingStep.COMPLETED &&
                state.currentStep != ProcessingStep.ERROR
            ) {
                ProgressSection(state = state)
            }

            when (state.currentStep) {
                ProcessingStep.IDLE -> {
                    Button(
                        onClick = { viewModel.startProcessing() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Начать обработку и загрузку", fontSize = 16.sp)
                    }
                }

                ProcessingStep.COMPLETED -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.reset() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Новая обработка", fontSize = 16.sp)
                        }
                    }
                }

                ProcessingStep.ERROR -> {
                    Button(
                        onClick = { viewModel.reset() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Попробовать снова", fontSize = 16.sp)
                    }
                }

                else -> {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Обработка...", fontSize = 16.sp)
                    }
                }
            }

            ResultSection(state = state)
        }
    }
}

@Composable
fun StatusHeader(state: ProcessingState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (state.currentStep) {
                ProcessingStep.ERROR -> MaterialTheme.colorScheme.errorContainer
                ProcessingStep.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith
                            fadeOut() + slideOutHorizontally()
                },
                label = "status_animation"
            ) { step ->
                when (step) {
                    ProcessingStep.IDLE -> {
                        Text(
                            text = "Готов к обработке",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ProcessingStep.COMPRESSING -> {
                        Text(
                            text = "Сжимаем фото...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    ProcessingStep.ADDING_WATERMARK -> {
                        Text(
                            text = "Добавляем водяной знак...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    ProcessingStep.UPLOADING -> {
                        Text(
                            text = "Загружаем в облако...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    ProcessingStep.COMPLETED -> {
                        Text(
                            text = "Обработка завершена!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    ProcessingStep.ERROR -> {
                        Text(
                            text = "Ошибка обработки",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (state.resultMessage.isNotEmpty() &&
                state.currentStep != ProcessingStep.IDLE
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.resultMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProgressSection(state: ProcessingState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { state.progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${state.progress}%",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (state.progress < 100) {
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Text(
                text = "Обработка...",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ResultSection(state: ProcessingState) {
    when (state.currentStep) {
        ProcessingStep.COMPLETED -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Готово! Фото загружено",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.outputFileName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        ProcessingStep.ERROR -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "${state.errorMessage}",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        else -> {}
    }
}
