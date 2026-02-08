package com.jocmp.capy.common

import com.jocmp.capy.logging.CapyLog
import org.jsoup.Jsoup

internal object ContentFormatter {
    private const val MAX_LENGTH = 256

    fun summary(content: String?): String {
        if (content.isNullOrBlank()) {
            return ""
        }

        return try {
            val text = Jsoup.parse(content).text()

            truncate(text)
        } catch (e: Exception) {
            CapyLog.error("content_formatter_summary", e)
            ""
        }
    }

    private fun truncate(text: String): String {
        if (text.length <= MAX_LENGTH) {
            return text
        }

        val truncated = text.take(MAX_LENGTH)
        val lastSpace = truncated.lastIndexOf(' ')

        if (lastSpace <= 0) {
            return truncated
        }

        return truncated.take(lastSpace)
    }
}
