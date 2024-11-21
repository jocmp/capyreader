package com.jocmp.capy.articles

import com.jocmp.capy.articles.HtmlHelpers.html
import com.jocmp.capy.testFile
import org.jsoup.Jsoup
import kotlin.test.Test
import kotlin.test.assertEquals

class CleanLinksTest {
    @Test
    fun `makes all subsequent links lazy loaded`() {
        val document = html {
            """
           <img src="https://example.com/1.png">
           <img src="https://example.com/2.png">
           <img src="https://example.com/3.png">
           """
        }

        cleanLinks(document)

        HtmlHelpers.assertEquals(document) {
            """
            <img src="https://example.com/1.png">
            <img src="https://example.com/2.png" loading="lazy">
            <img src="https://example.com/3.png" loading="lazy">
            """.trimIndent().lines().joinToString(" ")
        }
    }

    @Test
    fun `moves lazy-loaded src to src attribute`() {
        val document = html {
            """
            <img data-src="https://example.com/1.png">
            """
        }

        cleanLinks(document)

        HtmlHelpers.assertEquals(document) {
            """
            <img data-src="https://example.com/1.png" src="https://example.com/1.png">
           """
        }
    }

    @Test
    fun `pulls nested images out of links`() {
        val document = html {
            """
            <img src="https://example.com/1.png">
            <a href=""https://example.com/nested.png">
                <img src="https://example.com/nested.png">
            </a>
            """
        }

        cleanLinks(document)

        HtmlHelpers.assertEquals(document) {
            """
            <img src="https://example.com/1.png">
            <img src="https://example.com/nested.png" loading="lazy">
            """.trimIndent().lines().joinToString(" ")
        }
    }

    @Test
    fun `works on a real article`() {
        val document = Jsoup.parse(testFile("article_substack.html"))

        assertEquals(actual = document.select("a img").size, expected = 2)

        cleanLinks(document)

        assertEquals(actual = document.select("a img").size, expected = 0)
        assertEquals(actual = document.select("img").size, expected = 2)
    }
}
