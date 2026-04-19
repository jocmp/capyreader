package com.capyreader.app.ui.articles.feeds

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

data class RefreshButtonState(
    val iconRotation: Float,
)

@Composable
fun rememberRefreshButtonState(
    refreshState: AngleRefreshState,
): RefreshButtonState {
    val angle = remember {
        Animatable(0f)
    }

    val startRunning = refreshState == AngleRefreshState.RUNNING

    LaunchedEffect(startRunning) {
        if (!startRunning) {
            return@LaunchedEffect
        }

        angle.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                tween(
                    RotationDuration,
                    easing = LinearEasing
                )
            )
        )
    }

    val isSettling = refreshState == AngleRefreshState.SETTLING

    LaunchedEffect(isSettling) {
        if (!isSettling) {
            return@LaunchedEffect
        }

        angle.animateTo(
            360f,
            animationSpec = tween(
                RotationDuration - (angle.value.toInt() * MillisPerDegree),
                easing = LinearEasing
            )
        )

        angle.snapTo(0f)
    }

    return RefreshButtonState(
        angle.value,
    )
}

enum class AngleRefreshState {
    STOPPED,
    RUNNING,
    SETTLING,
}

private const val RotationDuration = 1080

private const val MillisPerDegree = 3
