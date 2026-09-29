package com.capyreader.app.ui.articles.reader

import com.jocmp.mallet.LinearImage
import com.jocmp.mallet.LinearImageSource
import kotlin.math.abs

fun LinearImage.bestSource(pixelDensity: Float, maxWidthPx: Int): LinearImageSource? {
    return sources.minByOrNull { candidate ->
        abs(candidate.relativeSize(pixelDensity, maxWidthPx) - 1f)
    }
}

private fun LinearImageSource.relativeSize(devicePixelDensity: Float, maxWidthPx: Int): Float {
    val density = pixelDensity
    val screenWidth = screenWidth
    val width = widthPx

    return when {
        density != null -> density / devicePixelDensity
        screenWidth != null -> screenWidth / maxWidthPx.toFloat()
        width != null -> width / maxWidthPx.toFloat()
        else -> 1f / devicePixelDensity
    }
}

fun LinearImageSource.requestWidth(maxWidthPx: Int): Int {
    val screenWidth = screenWidth
    val width = widthPx

    return when {
        pixelDensity != null -> maxWidthPx
        screenWidth != null -> screenWidth
        width != null -> width
        else -> maxWidthPx
    }
}

val LinearImageSource.aspectRatio: Float?
    get() {
        val width = widthPx ?: return null
        val height = heightPx ?: return null

        return width.toFloat() / height.toFloat()
    }
