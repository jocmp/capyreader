package com.jocmp.capy.articles

import org.json.JSONObject

data class TemplateColors(
    val onPrimaryContainer: String,
    val onSurface: String,
    val onSurfaceVariant: String,
    val primary: String,
    val primaryContainer: String,
    val surface: String,
    val surfaceContainerHighest: String,
    val surfaceVariant: String,
) {
    fun asMap() = mapOf(
        "color_on_primary_container" to onPrimaryContainer,
        "color_on_surface" to onSurface,
        "color_on_surface_variant" to onSurfaceVariant,
        "color_primary" to primary,
        "color_primary_container" to primaryContainer,
        "color_surface" to surface,
        "color_surface_container_highest" to surfaceContainerHighest,
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
            "--color-surface-container-highest" to surfaceContainerHighest,
            "--color-surface-variant" to surfaceVariant,
        )
    )
}
