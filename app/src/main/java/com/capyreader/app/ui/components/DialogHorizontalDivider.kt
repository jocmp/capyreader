package com.capyreader.app.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DialogHorizontalDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        color = colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
