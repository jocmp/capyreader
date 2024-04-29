package com.jocmp.basilreader.ui.articles.detail

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.json.JSONObject

@Immutable
data class TemplateColors(
    val onPrimaryContainer: String,
    val onSurface: String,
    val onSurfaceVariant: String,
    val primary: String,
    val primaryContainer: String,
    val surface: String,
    val surfaceVariant: String,
) {
    fun asMap() = mapOf(
        "color_on_primary_container" to onPrimaryContainer,
        "color_on_surface" to onSurface,
        "color_on_surface_variant" to onSurfaceVariant,
        "color_primary" to primary,
        "color_primary_container" to primaryContainer,
        "color_surface" to surface,
        "color_surface_variant" to surfaceVariant,
    )

    fun toJSON() = JSONObject(
        mapOf(
            "--color-on-surface" to onSurface,
            "--color-on-primary-container" to onPrimaryContainer,
            "--color-on-surface-variant" to onSurfaceVariant,
            "--color-primary" to primary,
            "--color-primary-container" to primaryContainer,
            "--color-surface" to surface,
            "--color-surface-variant" to surfaceVariant,
        )
    )
}

@Composable
fun articleTemplateColors(): TemplateColors {
    return TemplateColors(
        onPrimaryContainer = colorScheme.onPrimaryContainer.toHTMLColor(),
        onSurface = colorScheme.onSurface.toHTMLColor(),
        onSurfaceVariant = colorScheme.onSurfaceVariant.toHTMLColor(),
        primary = colorScheme.primary.toHTMLColor(),
        primaryContainer = colorScheme.primaryContainer.toHTMLColor(),
        surface = colorScheme.surface.toHTMLColor(),
        surfaceVariant = colorScheme.surfaceVariant.toHTMLColor(),
    )
}

private fun Color.toHTMLColor(): String {
    val hex = Integer.toHexString(toArgb()).takeLast(6)

    return "#${hex.uppercase()}"
}
