package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.capyreader.app.ui.rememberLayoutPreference

@Composable
fun LayoutNavigationHandler(
    enabled: Boolean,
    onChange: suspend () -> Unit,
) {
    val layout = rememberLayoutPreference()

    LaunchedEffect(layout) {
        if (enabled) {
            onChange()
        }
    }
}
