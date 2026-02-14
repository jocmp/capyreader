package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.capyreader.app.ui.isExpanded

@Composable
fun LayoutNavigationHandler(
    enabled: Boolean,
    onChange: suspend () -> Unit,
) {
    val expanded = isExpanded()

    LaunchedEffect(expanded) {
        if (enabled) {
            onChange()
        }
    }
}
