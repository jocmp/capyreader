package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import kotlin.math.roundToInt

class AnchorRegistry(private val scrollState: ScrollState) {
    private val offsets = mutableMapOf<Int, Float>()

    var contentCoordinates: LayoutCoordinates? = null

    fun register(index: Int, coordinates: LayoutCoordinates) {
        val content = contentCoordinates ?: return

        offsets[index] = coordinates.positionInWindow().y - content.positionInWindow().y
    }

    suspend fun scrollTo(index: Int): Boolean {
        val offset = offsets[index] ?: return false

        scrollState.animateScrollTo(offset.roundToInt())

        return true
    }
}
