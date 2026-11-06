package com.jocmp.capy.common

import com.jocmp.capy.logging.CapyLog
import com.jocmp.mallet.replaceMathWithSource
import org.jsoup.Jsoup

internal object ContentFormatter {
    private const val MAX_LENGTH = 256

    fun summary(content: String?): String {
        if (content.isNullOrBlank()) {
            return ""
        }

        return try {
            truncate(plainText(content))
        } catch (e: Exception) {
            CapyLog.error("content_formatter_summary", e)
            ""
        }
    }

    /**
     * Visible text of an HTML fragment, with rendered math reduced to its TeX source.
     */
    fun plainText(content: String): String {
        val document = Jsoup.parse(content.unwrapCDATA())
        document.body().replaceMathWithSource()
        return document.text()
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
