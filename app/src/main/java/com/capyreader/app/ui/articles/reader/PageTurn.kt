package com.capyreader.app.ui.articles.reader

enum class PageDirection {
    BACK,
    FORWARD,
}

data class PageBox(val top: Float, val bottom: Float) {
    fun straddles(edge: Float) = top < edge && bottom > edge
}

object PageTurn {
    fun next(
        top: Float,
        height: Float,
        lines: List<PageBox>,
        blocks: List<PageBox>,
    ): Float {
        val edge = top + height
        val minimum = top + height / 2

        val line = lines
            .filter { it.straddles(edge) && it.top > minimum }
            .minOfOrNull { it.top }

        val block = blocks
            .filter { it.straddles(edge) && it.top > top && it.bottom - it.top <= height }
            .minOfOrNull { it.top }

        return listOfNotNull(line, block).minOrNull() ?: edge
    }

    fun breaks(
        start: Float,
        height: Float,
        contentBottom: Float,
        lines: List<PageBox>,
        blocks: List<PageBox>,
    ): List<Float> {
        val tops = mutableListOf(start)

        while (tops.size < MAX_PAGES) {
            val top = next(tops.last(), height, lines, blocks)

            if (top >= contentBottom || top <= tops.last()) {
                return tops
            }

            tops.add(top)
        }

        return tops
    }

    fun previous(
        top: Float,
        height: Float,
        lines: List<PageBox>,
        blocks: List<PageBox>,
    ): Float {
        val edge = top - height
        val maximum = top - height / 2

        if (edge <= 0f) {
            return 0f
        }

        val line = lines
            .filter { it.straddles(edge) && it.bottom < maximum }
            .maxByOrNull { it.bottom }

        if (line != null) {
            return line.bottom
        }

        val block = blocks
            .filter { it.straddles(edge) && it.bottom < maximum }
            .maxByOrNull { it.bottom }

        return block?.bottom ?: edge
    }
}

private const val MAX_PAGES = 1000
