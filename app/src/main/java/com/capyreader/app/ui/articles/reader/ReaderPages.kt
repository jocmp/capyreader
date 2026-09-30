package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

private data class PageLayoutKey(
    val contentHeight: Int,
    val viewportSize: Int,
    val insets: PageInsets,
)

class ReaderPages(private val scrollState: ScrollState) {
    private val texts = mutableMapOf<LayoutCoordinates, () -> TextLayoutResult?>()
    private val elements = mutableMapOf<Int, LayoutCoordinates>()
    private val unsplittables = mutableSetOf<LayoutCoordinates>()
    private val history = ArrayDeque<PageJump>()

    var contentCoordinates: LayoutCoordinates? = null

    var pageTops by mutableStateOf(listOf(0f))
        private set

    private var pageInsets = PageInsets(top = 0f, bottom = 0f)
    private var holdLastPage = false
    private var pageLayoutKey: PageLayoutKey? = null

    val pageCount: Int
        get() = pageTops.size

    val currentPage: Int
        get() {
            val scroll = scrollState.value + pageInsets.top

            return pageTops.indexOfLast { it <= scroll + 1f }.coerceAtLeast(0)
        }

    val pageStart: Float
        get() = pageTops.getOrElse(currentPage) { 0f } - scrollState.value

    val pageCut: Float?
        get() {
            val next = pageTops.getOrNull(currentPage + 1) ?: return null

            return next - scrollState.value
        }

    fun layoutPages(insets: PageInsets) {
        val content = contentCoordinates?.takeIf { it.isAttached } ?: return
        val key = PageLayoutKey(
            contentHeight = content.size.height,
            viewportSize = scrollState.viewportSize,
            insets = insets,
        )

        if (key == pageLayoutKey) {
            return
        }

        val height = scrollState.viewportSize - insets.top - insets.bottom

        if (height <= 0f) {
            return
        }

        val lines = textLines(content)
        val blocks = unsplittableBlocks(content)
        val contentBottom = (lines + blocks + elementBlocks(content)).maxOfOrNull { it.bottom } ?: 0f

        pageLayoutKey = key
        pageInsets = insets
        pageTops = PageTurn.breaks(
            start = insets.top,
            height = height,
            contentBottom = contentBottom,
            lines = lines,
            blocks = blocks,
        )
    }

    fun openAtLastPage() {
        holdLastPage = true
    }

    suspend fun alignToPage() {
        if (holdLastPage) {
            showPage(pageCount - 1)
            return
        }

        showPage(currentPage)
    }

    suspend fun turnPage(direction: PageDirection): Boolean {
        holdLastPage = false
        val page = currentPage

        if (direction == PageDirection.FORWARD) {
            if (page + 1 >= pageCount) {
                return false
            }

            showPage(page + 1)
            return true
        }

        if (page == 0) {
            return false
        }

        showPage(page - 1)
        return true
    }

    suspend fun showPageContaining(offset: Float) {
        val top = offset + pageInsets.top
        val page = pageTops.indexOfLast { it <= top + 1f }.coerceAtLeast(0)

        showPage(page)
    }

    private suspend fun showPage(page: Int) {
        val top = pageTops.getOrNull(page) ?: return

        scrollState.scrollTo((top - pageInsets.top).roundToInt())
    }

    fun registerElement(index: Int, coordinates: LayoutCoordinates) {
        elements[index] = coordinates
    }

    fun lines(layout: () -> TextLayoutResult?): Modifier {
        return Modifier.onPlaced { coordinates -> texts[coordinates] = layout }
    }

    fun unsplittable(): Modifier {
        return Modifier.onPlaced { coordinates -> unsplittables.add(coordinates) }
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
        val blocks = unsplittableBlocks(content)

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

    private fun unsplittableBlocks(content: LayoutCoordinates): List<PageBox> {
        unsplittables.removeAll { !it.isAttached }

        return unsplittables.map { coordinates -> box(content, coordinates) }
    }

    private fun box(content: LayoutCoordinates, coordinates: LayoutCoordinates): PageBox {
        val offset = content.localPositionOf(coordinates, Offset.Zero).y

        return PageBox(top = offset, bottom = offset + coordinates.size.height)
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
