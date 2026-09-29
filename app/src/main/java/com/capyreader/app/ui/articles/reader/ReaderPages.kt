package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.TextLayoutResult
import kotlin.math.roundToInt

val LocalReaderPages = staticCompositionLocalOf<ReaderPages?> { null }

data class PageInsets(val top: Float, val bottom: Float)

private data class PageJump(val from: Int, val to: Int)

class ReaderPages(private val scrollState: ScrollState) {
    private val texts = mutableMapOf<LayoutCoordinates, () -> TextLayoutResult?>()
    private val elements = mutableMapOf<Int, LayoutCoordinates>()
    private val history = ArrayDeque<PageJump>()

    var contentCoordinates: LayoutCoordinates? = null

    fun registerElement(index: Int, coordinates: LayoutCoordinates) {
        elements[index] = coordinates
    }

    fun lines(layout: () -> TextLayoutResult?): Modifier {
        return Modifier.onPlaced { coordinates -> texts[coordinates] = layout }
    }

    suspend fun turn(direction: PageDirection, visible: PageInsets, next: PageInsets) {
        val content = contentCoordinates?.takeIf { it.isAttached } ?: return
        val current = scrollState.value

        if (history.lastOrNull()?.to != current) {
            history.clear()
        }

        if (direction == PageDirection.BACK && history.isNotEmpty()) {
            scrollState.scrollTo(history.removeLast().from)
            return
        }

        val top = current + visible.top
        val height = scrollState.viewportSize - visible.top - visible.bottom

        if (height <= 0f) {
            return
        }

        val lines = textLines(content)
        val blocks = elementBlocks(content)

        if (direction == PageDirection.BACK) {
            val previousTop = PageTurn.previous(top, height, lines, blocks)

            scrollState.scrollTo((previousTop - next.top).roundToInt())
            return
        }

        val nextTop = PageTurn.next(top, height, lines, blocks)
        scrollState.scrollTo((nextTop - next.top).roundToInt())

        if (scrollState.value != current) {
            history.addLast(PageJump(from = current, to = scrollState.value))
        }
    }

    private fun textLines(content: LayoutCoordinates): List<PageBox> {
        texts.keys.removeAll { !it.isAttached }

        return texts.flatMap { (coordinates, layout) ->
            val result = layout() ?: return@flatMap emptyList()
            val offset = content.localPositionOf(coordinates, Offset.Zero).y

            (0 until result.lineCount).map { line ->
                PageBox(
                    top = offset + result.getLineTop(line),
                    bottom = offset + result.getLineBottom(line),
                )
            }
        }
    }

    private fun elementBlocks(content: LayoutCoordinates): List<PageBox> {
        return elements.values
            .filter { it.isAttached }
            .map { coordinates ->
                val offset = content.localPositionOf(coordinates, Offset.Zero).y

                PageBox(top = offset, bottom = offset + coordinates.size.height)
            }
    }
}
