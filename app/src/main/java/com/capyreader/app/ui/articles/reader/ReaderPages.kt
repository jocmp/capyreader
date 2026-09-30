package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

val LocalReaderPages = staticCompositionLocalOf<ReaderPages?> { null }

val LocalReaderPageHeight = compositionLocalOf<Dp?> { null }

data class PageInsets(val top: Float, val bottom: Float)

private data class PageJump(val from: Int, val to: Int)

class ReaderPages(private val scrollState: ScrollState) {
    private val texts = mutableMapOf<LayoutCoordinates, () -> TextLayoutResult?>()
    private val unsplittables = mutableSetOf<LayoutCoordinates>()
    private val history = ArrayDeque<PageJump>()

    var contentCoordinates: LayoutCoordinates? = null

    fun lines(layout: () -> TextLayoutResult?): Modifier {
        return Modifier.onPlaced { coordinates -> texts[coordinates] = layout }
    }

    fun unsplittable(): Modifier {
        return Modifier.onPlaced { coordinates -> unsplittables.add(coordinates) }
    }

    suspend fun turn(direction: PageDirection, visible: PageInsets, next: PageInsets): Boolean {
        val content = contentCoordinates?.takeIf { it.isAttached } ?: return false
        val current = scrollState.value

        if (history.lastOrNull()?.to != current) {
            history.clear()
        }

        if (direction == PageDirection.BACK && history.isNotEmpty()) {
            scrollState.scrollTo(history.removeLast().from)
            return true
        }

        val top = current + visible.top
        val height = scrollState.viewportSize - visible.top - visible.bottom

        if (height <= 0f) {
            return false
        }

        val lines = textLines(content)
        val blocks = unsplittableBlocks(content)

        if (direction == PageDirection.BACK) {
            val previousTop = PageTurn.previous(top, height, lines, blocks)

            scrollState.scrollTo((previousTop - next.top).roundToInt())

            return scrollState.value != current
        }

        val nextTop = PageTurn.next(top, height, lines, blocks)
        scrollState.scrollTo((nextTop - next.top).roundToInt())

        if (scrollState.value == current) {
            return false
        }

        history.addLast(PageJump(from = current, to = scrollState.value))

        return true
    }

    suspend fun line(direction: PageDirection, visible: PageInsets, fallback: Float) {
        val content = contentCoordinates?.takeIf { it.isAttached } ?: return
        val top = scrollState.value + visible.top
        val target = lineTop(direction, top, textLines(content))

        if (target == null) {
            scrollState.scrollBy(fallback * direction.sign)
            return
        }

        scrollState.scrollTo((target - visible.top).roundToInt())
    }

    private fun lineTop(direction: PageDirection, top: Float, lines: List<PageBox>): Float? {
        val tops = lines.map { it.top }

        if (direction == PageDirection.FORWARD) {
            return tops.filter { it > top + 1f }.minOrNull()
        }

        return tops.filter { it < top - 1f }.maxOrNull()
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

    private fun unsplittableBlocks(content: LayoutCoordinates): List<PageBox> {
        unsplittables.removeAll { !it.isAttached }

        return unsplittables.map { coordinates ->
            val offset = content.localPositionOf(coordinates, Offset.Zero).y

            PageBox(top = offset, bottom = offset + coordinates.size.height)
        }
    }
}

private val PageDirection.sign: Float
    get() {
        if (this == PageDirection.FORWARD) {
            return 1f
        }

        return -1f
    }
