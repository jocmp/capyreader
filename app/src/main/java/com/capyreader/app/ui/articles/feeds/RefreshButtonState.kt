package com.capyreader.app.ui.articles.feeds

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


data class RefreshButtonState(
    val refreshing: Boolean,
    val refresh: () -> Unit,
    val iconRotation: Float,
)

@Composable
fun rememberRefreshButtonState(
    onRefresh: (completion: () -> Unit) -> Unit
): RefreshButtonState {
    var refreshing by remember { mutableStateOf(false) }

    val angle by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = ""
    )

    val iconRotation = if (refreshing) angle else 0f

    val refresh = {
        refreshing = true
        onRefresh {
            refreshing = false
        }
    }

    return RefreshButtonState(
        refreshing,
        refresh,
        iconRotation,
    )
}
