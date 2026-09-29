package com.capyreader.app.ui.articles.reader

object PageFooterTitle {
    fun truncate(title: String, limit: Int = MAX_LENGTH): String {
        val trimmed = title.trim()

        if (trimmed.length <= limit) {
            return trimmed
        }

        val cut = trimmed.take(limit)
        val space = cut.lastIndexOf(' ')

        if (space > 0) {
            return cut.take(space).trimEnd() + ELLIPSIS
        }

        return cut + ELLIPSIS
    }

    private const val MAX_LENGTH = 100

    private const val ELLIPSIS = "…"
}
