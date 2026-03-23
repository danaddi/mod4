package com.example.kt14

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CompassScreen(
    viewModel: CompassViewModel = viewModel()
) {
    val context = LocalContext.current
    val azimuth by viewModel.azimuth.collectAsState()
    val isSensorAvailable by viewModel.isSensorAvailable.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(context)
    }

    CompassLifecycleEffect(
        onResume = { viewModel.startListening() },
        onPause = { viewModel.stopListening() }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1A1A2E)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "КОМПАС",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            if (isSensorAvailable) {
                CompassDial(
                    azimuth = azimuth,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A3E)
                    ),
                    shape = CircleShape
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "АЗИМУТ",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = String.format("%.0f°", azimuth),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Устройство не поддерживает\nдатчик ориентации",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Компас не работает на этом устройстве",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompassDial(
    azimuth: Float,
    modifier: Modifier = Modifier
) {
    val animatedRotation by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color(0xFF2A2A3E),
                radius = size.minDimension / 2,
                center = center
            )

            drawCircle(
                color = Color(0xFF3A3A4E),
                radius = size.minDimension / 2 - 4.dp.toPx(),
                center = center
            )

            val radius = size.minDimension / 2 - 40.dp.toPx()
            for (angle in 0..360 step 10) {
                val angleRad = Math.toRadians(angle.toDouble())
                val startX = center.x + radius * 0.8f * Math.cos(angleRad).toFloat()
                val startY = center.y + radius * 0.8f * Math.sin(angleRad).toFloat()
                val endX = center.x + radius * Math.cos(angleRad).toFloat()
                val endY = center.y + radius * Math.sin(angleRad).toFloat()

                val lineWidth = if (angle % 30 == 0) 4f else 2f
                val alpha = if (angle % 30 == 0) 1f else 0.5f

                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = lineWidth
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .rotate(animatedRotation)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val arrowLength = size.width * 0.4f

            drawLine(
                color = Color(0xFFF44336),
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY - arrowLength),
                strokeWidth = 16.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawLine(
                color = Color(0xFF9E9E9E),
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY + arrowLength),
                strokeWidth = 16.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawCircle(
                color = Color(0xFF1A1A2E),
                radius = 20.dp.toPx(),
                center = Offset(centerX, centerY)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = 24.dp.toPx(),
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Text(
            text = "N",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF44336),
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Text(
            text = "S",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        Text(
            text = "E",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        Text(
            text = "W",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Composable
fun CompassLifecycleEffect(
    onResume: () -> Unit,
    onPause: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        onResume()
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            onPause()
        }
    }
}