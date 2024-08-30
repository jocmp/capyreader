package com.capyreader.app.ui.articles.detail

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jocmp.capy.articles.TemplateColors

@Composable
fun articleTemplateColors() = TemplateColors(
    onPrimaryContainer = colorScheme.onPrimaryContainer.toHTMLColor(),
    onSurface = colorScheme.onSurface.toHTMLColor(),
    onSurfaceVariant = colorScheme.onSurfaceVariant.toHTMLColor(),
    primary = colorScheme.primary.toHTMLColor(),
    primaryContainer = colorScheme.primaryContainer.toHTMLColor(),
    surface = colorScheme.surface.toHTMLColor(),
    surfaceContainerHighest = colorScheme.surfaceContainerHighest.toHTMLColor(),
    surfaceVariant = colorScheme.surfaceVariant.toHTMLColor(),
)


private fun Color.toHTMLColor(): String {
    val hex = Integer.toHexString(toArgb()).takeLast(6)

    return "#${hex.uppercase()}"
}
