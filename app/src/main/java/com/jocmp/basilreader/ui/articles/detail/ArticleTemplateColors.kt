package com.jocmp.basilreader.ui.articles.detail

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

@Composable
fun articleTemplateColors(): Map<String, String> {
    return mapOf(
        "color_on_primary_container" to colorScheme.onPrimaryContainer.toHTMLColor(),
        "color_on_surface" to colorScheme.onSurface.toHTMLColor(),
        "color_on_surface_variant" to colorScheme.onSurfaceVariant.toHTMLColor(),
        "color_primary" to colorScheme.primary.toHTMLColor(),
        "color_primary_container" to colorScheme.primaryContainer.toHTMLColor(),
        "color_surface" to colorScheme.surface.toHTMLColor(),
        "color_surface_variant" to colorScheme.surfaceVariant.toHTMLColor(),
    )
}

private fun Color.toHTMLColor(): String {
    val hex = Integer.toHexString(toArgb()).takeLast(6)

    return "#${hex.uppercase()}"
}
