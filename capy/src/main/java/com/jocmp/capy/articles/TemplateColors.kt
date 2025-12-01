package com.jocmp.capy.articles

data class TemplateColors(
    val primary: String,
    val surface: String,
    val surfaceContainerHighest: String,
    val onSurface: String,
    val onSurfaceVariant: String,
    val surfaceVariant: String,
    val primaryContainer: String,
    val onPrimaryContainer: String,
    val secondary: String,
    val surfaceContainer: String,
    val surfaceTint: String,
) {
    fun asMap() = mapOf(
        "color_primary" to primary,
        "color_surface" to surface,
        "color_surface_container_highest" to surfaceContainerHighest,
        "color_on_surface" to onSurface,
        "color_on_surface_variant" to onSurfaceVariant,
        "color_surface_variant" to surfaceVariant,
        "color_primary_container" to primaryContainer,
        "color_on_primary_container" to onPrimaryContainer,
        "color_secondary" to secondary,
        "color_surface_container" to surfaceContainer,
        "color_surface_tint" to surfaceTint,
    )
}
