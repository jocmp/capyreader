package com.jocmp.capy.articles

import com.jocmp.capy.Article
import org.jsoup.Jsoup

private const val WORDS_PER_MINUTE = 225

/**
 * Estimated reading time in minutes for the article, based on the
 * article's content (or summary as a fallback). Always returns at
 * least one minute when there is any content.
 */
fun Article.readingTimeMinutes(): Int {
    val source = contentHTML.ifBlank { summary }

    if (source.isBlank()) {
        return 0
    }

    val text = Jsoup.parse(source).text()
    val wordCount = text
        .split(WORD_SEPARATORS)
        .count { it.isNotBlank() }

    if (wordCount == 0) {
        return 0
    }

    return ((wordCount + WORDS_PER_MINUTE - 1) / WORDS_PER_MINUTE).coerceAtLeast(1)
}

private val WORD_SEPARATORS = Regex("\\s+")
