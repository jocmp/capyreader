package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.collectChangesWithDefault
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import org.koin.compose.koinInject

@Composable
fun HorizontalReaderPager(
    enablePrevious: Boolean,
    enableNext: Boolean,
    onSelectPrevious: () -> Unit,
    onSelectNext: () -> Unit,
    appPreferences: AppPreferences = koinInject(),
    content: @Composable () -> Unit,
) {
    val enabled by appPreferences.readerOptions.enableHorizontaPagination.collectChangesWithDefault()

    if (!enabled) {
        return content()
    }

    SwipeableActionsBox(
        disableRipple = true,
        swipeThreshold = 24.dp,
        backgroundUntilSwipeThreshold = colorScheme.surface,
        startActions = action(
            enabled = enablePrevious,
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            onSwipe = onSelectPrevious,
        ),
        endActions = action(
            enabled = enableNext,
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            onSwipe = onSelectNext
        )
    ) {
        content()
    }
}

@Composable
fun action(
    enabled: Boolean,
    icon: ImageVector,
    onSwipe: () -> Unit
): List<SwipeAction> {
    if (!enabled) {
        return emptyList()
    }

    return listOf(
        SwipeAction(
            onSwipe = onSwipe,
            background = colorScheme.surfaceContainerHighest,
            icon = {
               Box(Modifier.padding(16.dp)) {
                   Icon(
                       icon,
                       contentDescription = null,
                   )
               }
            }
        )
    )
}
