package com.capyreader.app.ui.articles.reader

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import kotlin.math.min

@Stable
class RestrainedFillWidthScaling(
    private val pixelDensity: Float,
) : ContentScale {
    override fun computeScaleFactor(srcSize: Size, dstSize: Size): ScaleFactor {
        val fillWidth = dstSize.width / srcSize.width
        val scale = min(pixelDensity.coerceAtLeast(1f), fillWidth)

        return ScaleFactor(scale, scale)
    }
}
