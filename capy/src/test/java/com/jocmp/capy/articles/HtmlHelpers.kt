package com.jocmp.capy.articles

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.test.assertEquals

object HtmlHelpers {
    fun html(content: String): Document {
        val documentString = """
        <html>
        <body>
        $content
        </body>
        </html>
    """.trimIndent()

        val doc = Jsoup.parse(documentString)
        return doc
    }

    fun assertEquals(actual: Document, expected: () -> String) {
        assertEquals(expected = snapshot(expected()), actual = snapshot(actual.body().html()))
    }

    private fun snapshot(content: String): String {
        return content.trimIndent()
    }
}
