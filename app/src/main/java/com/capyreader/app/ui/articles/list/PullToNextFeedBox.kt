package com.capyreader.app.ui.articles.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.capyreader.app.ui.components.pullrefresh.SwipeRefresh

@Composable
fun PullToNextFeedBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onRequestNext: () -> Unit,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val triggerThreshold = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    SwipeRefresh(
        onRefresh = { onRequestNext() },
        swipeEnabled = enabled,
        onTriggerThreshold = { triggerThreshold() },
        indicatorAlignment = Alignment.BottomCenter,
        icon = Icons.Rounded.KeyboardArrowDown,
        modifier = modifier,
    ) {
        content()
    }
}
