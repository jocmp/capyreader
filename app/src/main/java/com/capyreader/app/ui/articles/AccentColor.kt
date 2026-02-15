package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.ui.theme.LocalAppTheme

@Composable
fun rememberAccentColor(faviconURL: String?): Color? {
    faviconURL ?: return null

    val isDark = LocalAppTheme.current.isDark
    var color by remember(faviconURL, isDark) { mutableStateOf<Color?>(null) }
    val context = LocalContext.current

    LaunchedEffect(faviconURL, isDark) {
        color = FaviconColorCache.getColor(faviconURL, context, isDark)
    }

    return color
}
