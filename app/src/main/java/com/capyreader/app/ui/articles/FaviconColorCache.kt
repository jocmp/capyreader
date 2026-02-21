package com.capyreader.app.ui.articles

import android.content.Context
import androidx.collection.LruCache
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FaviconColorCache {
    private data class ColorPair(val light: Color, val dark: Color)

    private val cache = LruCache<String, ColorPair>(MAX_ENTRIES)

    suspend fun getColor(url: String, context: Context, isDark: Boolean): Color? {
        val pair = cache[url]
            ?: extractColors(url, context)?.also { cache.put(url, it) }
            ?: return null

        return if (isDark) pair.dark else pair.light
    }

    private suspend fun extractColors(url: String, context: Context): ColorPair? {
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(Size(64, 64))
            .allowHardware(false)
            .build()

        val bitmap = context.imageLoader.execute(request).image?.toBitmap() ?: return null
        val palette = withContext(Dispatchers.Default) { Palette.from(bitmap).generate() }

        val darkSwatch = palette.lightVibrantSwatch
            ?: palette.vibrantSwatch
            ?: palette.lightMutedSwatch
            ?: palette.dominantSwatch
            ?: return null

        val lightSwatch = palette.darkVibrantSwatch
            ?: palette.vibrantSwatch
            ?: palette.darkMutedSwatch
            ?: palette.dominantSwatch
            ?: return null

        return ColorPair(
            dark = clampLightness(darkSwatch.rgb, min = MIN_DARK_LIGHTNESS),
            light = clampLightness(lightSwatch.rgb, max = MAX_LIGHT_LIGHTNESS),
        )
    }

    private fun clampLightness(
        rgb: Int,
        min: Float = 0f,
        max: Float = 1f,
    ): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(rgb, hsl)
        hsl[2] = hsl[2].coerceIn(min, max)

        return Color(ColorUtils.HSLToColor(hsl))
    }

    private const val MAX_ENTRIES = 200
    private const val MIN_DARK_LIGHTNESS = 0.8f
    private const val MAX_LIGHT_LIGHTNESS = 0.35f
}
