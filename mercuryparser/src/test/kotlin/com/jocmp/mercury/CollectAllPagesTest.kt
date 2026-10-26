package com.jocmp.mercury

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.extractors.RootExtractor
import com.jocmp.mercury.extractors.extractor
import com.jocmp.mercury.resource.Resource
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CollectAllPagesTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `walks next_page_url links and merges content`() {
        server.enqueue(pageResponse(body = "First page body.", nextHref = "/page/2"))
        server.enqueue(pageResponse(body = "Second page body.", nextHref = "/page/3"))
        server.enqueue(pageResponse(body = "Third page body.", nextHref = null))

        val merged = runParse(url = server.url("/page/1").toString())

        val content = assertNotNull(merged.content)
        assertContains(content, "First page body.")
        assertContains(content, "<hr><h4>Page 2</h4>")
        assertContains(content, "Second page body.")
        assertContains(content, "<hr><h4>Page 3</h4>")
        assertContains(content, "Third page body.")
        assertEquals(3, merged.totalPages)
        assertEquals(3, merged.renderedPages)
        assertNull(merged.nextPageUrl)
        assertTrue(merged.wordCount > 0)
    }

    @Test
    fun `stops when next_page_url points back to a seen page`() {
        server.enqueue(pageResponse(body = "First page body.", nextHref = "/page/2"))
        // Page 2 loops back to page 1 — collectAllPages must break, not refetch.
        server.enqueue(pageResponse(body = "Second page body.", nextHref = "/page/1"))

        val merged = runParse(url = server.url("/page/1").toString())

        assertEquals(2, merged.renderedPages)
        assertNull(merged.nextPageUrl)
        // Only 2 requests should have hit the server: page 1, page 2.
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `respects MAX_PAGES_TO_FETCH cap`() {
        // Enqueue 30 pages, each linking to the next. The cap is 26 — so we
        // should fetch exactly 26, then stop even though more links remain.
        repeat(30) { i ->
            server.enqueue(pageResponse(body = "Page ${i + 1} body.", nextHref = "/page/${i + 2}"))
        }

        val merged = runParse(url = server.url("/page/1").toString())

        assertEquals(26, merged.renderedPages)
        assertEquals(26, server.requestCount)
    }

    private fun runParse(url: String): ParseResult =
        runBlocking {
            val doc: Doc = Resource.create(url = url)
            val html = doc.html()
            val metaCache = doc("meta[name]").elements.mapNotNull { it.attr("name").ifEmpty { null } }
            val initial =
                RootExtractor.extract(EXTRACTOR, doc, url, html, metaCache, fallback = false)
            collectAllPages(initial, url, EXTRACTOR, ParseOptions())
        }

    private fun pageResponse(
        body: String,
        nextHref: String?,
    ): MockResponse {
        val nextLink =
            nextHref?.let { """<a class="next" href="$it">Next</a>""" }.orEmpty()
        val html =
            """
            <!doctype html>
            <html><head><title>Test</title></head>
            <body><article><p>$body</p>$nextLink</article></body>
            </html>
            """.trimIndent()
        return MockResponse
            .Builder()
            .setHeader("Content-Type", "text/html; charset=utf-8")
            .body(html)
            .build()
    }

    companion object {
        // Custom extractor that pulls content from <article> and the next page
        // link from a.next[href]. The generic next-page-url extractor returns
        // null in mercury-parser-kt today, so a custom one is required.
        private val EXTRACTOR =
            extractor(domain = "localhost") {
                title { selectors("title") }
                content {
                    selectors("article")
                    defaultCleaner = false
                }
                nextPageUrl { attr("a.next", "href") }
            }
    }
}
