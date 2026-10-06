package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import kotlin.math.roundToInt

data class ReadingPosition(val index: Int, val fraction: Float)

class AnchorRegistry(private val scrollState: ScrollState) {
    private val offsets = mutableMapOf<Int, Float>()
    private val heights = mutableMapOf<Int, Int>()

    var contentCoordinates: LayoutCoordinates? = null

    fun register(index: Int, coordinates: LayoutCoordinates) {
        val content = contentCoordinates ?: return

        offsets[index] = coordinates.positionInWindow().y - content.positionInWindow().y
        heights[index] = coordinates.size.height
    }

    fun offset(index: Int): Float? = offsets[index]

    suspend fun scrollTo(index: Int): Boolean {
        val offset = offsets[index] ?: return false

        scrollState.animateScrollTo(offset.roundToInt())

        return true
    }

    fun readingPosition(): ReadingPosition? {
        val scrollY = scrollState.value

        if (scrollY == 0) {
            return null
        }

        val (index, offset) = offsets.entries
            .filter { it.value <= scrollY }
            .maxByOrNull { it.value } ?: return null

        val height = heights[index]?.coerceAtLeast(1) ?: return null

        return ReadingPosition(
            index = index,
            fraction = ((scrollY - offset) / height).coerceIn(0f, 1f),
        )
    }

    suspend fun restore(position: ReadingPosition): Boolean {
        val offset = offsets[position.index] ?: return false
        val height = heights[position.index] ?: return false

        scrollState.scrollTo((offset + height * position.fraction).roundToInt())

        return true
    }
}
