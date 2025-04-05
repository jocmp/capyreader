package com.capyreader.app.ui.articles

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.ColumnScrollbar
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Composable
fun ColumnScrollbar(state: ScrollState, content: @Composable () -> Unit) {
    ColumnScrollbar(
        state = state,
        settings = settings(),
    ) {
        content()
    }
}

@Composable
fun LazyScrollbar(state: LazyListState, content: @Composable () -> Unit) {
    LazyColumnScrollbar(
        state = state,
        settings = settings()
    ) {
        content()
    }
}

@Composable
fun settings() = ScrollbarSettings.Default.copy(
    thumbThickness = 4.dp,
    hideDisplacement = 0.dp,
    scrollbarPadding = 2.dp,
    thumbSelectedColor = colorScheme.onSurfaceVariant,
    thumbUnselectedColor = colorScheme.onSurfaceVariant
)
