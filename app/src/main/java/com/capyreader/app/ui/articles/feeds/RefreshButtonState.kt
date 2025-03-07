package com.capyreader.app.ui.articles.feeds

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jocmp.capy.logging.CapyLog


data class RefreshButtonState(
    val refreshing: Boolean,
    val refresh: () -> Unit,
    val iconRotation: Float,
)

@Composable
fun rememberRefreshButtonState(
    onRefresh: (completion: () -> Unit) -> Unit
): RefreshButtonState {
    var refreshState by remember { mutableStateOf(AngleRefreshState.DEFAULT) }
    val refreshing = refreshState != AngleRefreshState.DEFAULT

    val refresh = {
        refreshState = AngleRefreshState.RUNNING
        onRefresh {
            refreshState = AngleRefreshState.SETTLING
        }
    }

    if (refreshState == AngleRefreshState.DEFAULT) {
        return RefreshButtonState(
            refreshing,
            refresh,
            0f,
        )
    }

    val angle by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = ""
    )

    val iconRotation = if (refreshing) angle else 0f

    LaunchedEffect(refreshState, angle) {
        CapyLog.info(
            "angle",
            mapOf("value" to angle.toString(), "state" to refreshState.toString())
        )
        if (refreshState == AngleRefreshState.SETTLING && angle > 350) {
            refreshState = AngleRefreshState.DEFAULT
        }
    }

    return RefreshButtonState(
        refreshing,
        refresh,
        iconRotation,
    )
}

enum class AngleRefreshState {
    DEFAULT,
    RUNNING,
    SETTLING,
}
