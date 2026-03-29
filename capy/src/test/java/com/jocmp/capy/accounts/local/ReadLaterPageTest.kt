package com.jocmp.capy.accounts.local

import org.jsoup.Jsoup
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadLaterPageTest {
    @Test
    fun `parses og title over document title`() {
        val html = """
            <html>
            <head>
                <title>Document Title</title>
                <meta property="og:title" content="OG Title">
            </head>
            <body></body>
            </html>
        """.trimIndent()

        val page = ReadLaterPage.fromDocument(Jsoup.parse(html))

        assertEquals("OG Title", page.title)
    }

    @Test
    fun `falls back to document title when og title is missing`() {
        val html = """
            <html>
            <head>
                <title>Document Title</title>
            </head>
            <body></body>
            </html>
        """.trimIndent()

        val page = ReadLaterPage.fromDocument(Jsoup.parse(html))

        assertEquals("Document Title", page.title)
    }

    @Test
    fun `title is null when both are missing`() {
        val html = "<html><head></head><body></body></html>"

        val page = ReadLaterPage.fromDocument(Jsoup.parse(html))

        assertNull(page.title)
    }

    @Test
    fun `parses og description over meta description`() {
        val html = """
            <html>
            <head>
                <meta name="description" content="Meta Description">
                <meta property="og:description" content="OG Description">
            </head>
            <body></body>
            </html>
        """.trimIndent()

        val page = ReadLaterPage.fromDocument(Jsoup.parse(html))

        assertEquals("OG Description", page.summary)
    }

    @Test
    fun `falls back to meta description`() {
        val html = """
            <html>
            <head>
                <meta name="description" content="Meta Description">
            </head>
            <body></body>
            </html>
        """.trimIndent()

        val page = ReadLaterPage.fromDocument(Jsoup.parse(html))

        assertEquals("Meta Description", page.summary)
    }

    @Test
    fun `parses og image`() {
        val html = """
            <html>
            <head>
                <meta property="og:image" content="https://example.com/image.jpg">
            </head>
            <body></body>
            </html>
        """.trimIndent()

        val page = ReadLaterPage.fromDocument(Jsoup.parse(html))

        assertEquals("https://example.com/image.jpg", page.imageURL)
    }

    @Test
    fun `ignores blank og values`() {
        val html = """
            <html>
            <head>
                <meta property="og:title" content="">
                <meta property="og:description" content="">
                <meta property="og:image" content="">
                <title>Fallback</title>
            </head>
            <body></body>
            </html>
        """.trimIndent()

        val page = ReadLaterPage.fromDocument(Jsoup.parse(html))

        assertEquals("Fallback", page.title)
        assertNull(page.summary)
        assertNull(page.imageURL)
    }
}
