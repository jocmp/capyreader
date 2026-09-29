package com.capyreader.app.ui.articles.reader

import com.capyreader.app.common.MediaItem
import com.jocmp.mallet.LinearArticle
import com.jocmp.mallet.LinearAudio
import com.jocmp.mallet.LinearBlockQuote
import com.jocmp.mallet.LinearElement
import com.jocmp.mallet.LinearImage
import com.jocmp.mallet.LinearImageSource
import com.jocmp.mallet.LinearListItem
import com.jocmp.mallet.LinearTable
import com.jocmp.mallet.LinearText
import com.jocmp.mallet.LinearVideo

fun LinearArticle.images(): List<LinearImage> {
    return elements.flatMap { it.images() }
}

fun LinearArticle.galleryItems(): List<MediaItem> {
    return images().mapNotNull { image ->
        val source = image.largestSource() ?: return@mapNotNull null

        MediaItem(url = source.imgUri, altText = image.caption?.text)
    }
}

fun LinearImage.largestSource(): LinearImageSource? {
    return sources.maxByOrNull { it.sizeRank }
}

private val LinearImageSource.sizeRank: Float
    get() {
        val width = widthPx
        val screenWidth = screenWidth
        val density = pixelDensity

        return when {
            width != null -> width.toFloat()
            screenWidth != null -> screenWidth.toFloat()
            density != null -> density
            else -> 0f
        }
    }

private fun LinearElement.images(): List<LinearImage> {
    return when (this) {
        is LinearImage -> listOf(this)
        is LinearBlockQuote -> content.flatMap { it.images() }
        is LinearListItem -> content.flatMap { it.images() }
        is LinearTable -> cells.values.flatMap { cell -> cell.content.flatMap { it.images() } }
        is LinearText, is LinearAudio, is LinearVideo -> emptyList()
    }
}
