package com.jocmp.hyperview

import com.jocmp.hyperview.HtmlNode.Block.Image
import com.jocmp.hyperview.HtmlNode.Block.ImageSource

/**
 * Picks the best image URL for a given display width (px) and pixel density.
 *
 * Order of preference:
 *   1. Width-descriptor candidate closest to `targetWidthPx` without being smaller.
 *   2. Density-descriptor candidate matching `pixelDensity` (with fallback to the next-highest).
 *   3. `src`.
 */
fun Image.bestUrl(targetWidthPx: Int? = null, pixelDensity: Float = 1f): String {
    val widthCandidate = if (targetWidthPx != null) {
        val sorted = sources.filter { it.width != null }.sortedBy { it.width!! }
        sorted.firstOrNull { it.width!! >= targetWidthPx } ?: sorted.lastOrNull()
    } else null

    if (widthCandidate != null) return widthCandidate.url

    val densitySorted = sources.filter { it.density != null }.sortedBy { it.density!! }
    val densityCandidate = densitySorted.firstOrNull { it.density!! >= pixelDensity }
        ?: densitySorted.lastOrNull()

    if (densityCandidate != null) return densityCandidate.url

    return sources.firstOrNull()?.url?.takeIf { src.isBlank() } ?: src
}
