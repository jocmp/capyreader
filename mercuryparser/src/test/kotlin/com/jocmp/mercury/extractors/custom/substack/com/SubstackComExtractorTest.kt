package com.jocmp.mercury.extractors.custom.substack.com

import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions
import com.jocmp.mercury.ParseResult
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SubstackComExtractorTest {
    @Test
    fun `keeps section headings`() {
        val content = customDomain.content.orEmpty()

        listOf(
            "Vision 1: Kemalist AI",
            "Vision 2: President Claude",
            "Vision 3: Parliament Of Angels",
            "Vision 4: The Night Watchman",
        ).forEach { heading ->
            assertTrue(Regex("<h3[^>]*>$heading</h3>").containsMatchIn(content), "Missing heading: $heading")
        }
        assertFalse(content.contains("<svg"))
        assertFalse(content.contains("<button"))
    }

    @Test
    fun `keeps images outside of lightbox links`() {
        val content = customDomain.content.orEmpty()

        assertEquals(
            listOf("https://substack-post-media.s3.amazonaws.com/public/images/06cbe7c3-3d03-43af-8cd0-6767390b781f_594x136.png"),
            figureImages(content),
        )
    }

    @Test
    fun `keeps footnotes`() {
        val footnote = Jsoup.parse(customDomain.content.orEmpty()).getElementById("footnote-1")?.parent()

        assertTrue(footnote?.text().orEmpty().contains("This section is self-plagiarized from a LessWrong comment"))
    }

    @Test
    fun `detects custom domains`() {
        assertEquals("Against Against Handover", customDomain.title)
        assertEquals("Scott Alexander", customDomain.author)
        assertEquals("2026-10-20T20:32:59.315Z", customDomain.datePublished.toString())
    }

    @Test
    fun `keeps images without the lightbox controls`() {
        val content = subdomain.content.orEmpty()

        assertEquals(
            listOf(
                "https://substack-post-media.s3.amazonaws.com/public/images/24009919-6a50-499d-8324-1a93ea5f6b4e_1183x1080.png",
                "https://substack-post-media.s3.amazonaws.com/public/images/7f214260-8162-4f0f-8678-27f6427ddfff_904x764.png",
                "https://substack-post-media.s3.amazonaws.com/public/images/08003006-fcf9-476d-854d-9439ae8665b5_2398x1354.png",
            ),
            figureImages(content),
        )
        assertFalse(content.contains("<figure><figure"))
        assertFalse(content.contains("image-link-expand"))
        assertFalse(content.contains("subscribe-widget"))
    }

    @Test
    fun `replaces tweet embeds with a quote`() {
        val quote = Jsoup.parse(subdomain.content.orEmpty()).selectFirst("blockquote")!!

        assertTrue(quote.text().startsWith("Jason is 100% right. Code red."))
        assertEquals("https://x.com/benioff/status/2113407312866197954?s=61", quote.selectFirst("> p > a")?.attr("href"))
        assertTrue(quote.select("img, svg, picture").isEmpty())
    }

    @Test
    fun `reads post metadata on substack subdomains`() {
        assertEquals("Will Generative AI do more harm than good?", subdomain.title)
        assertEquals("2026-10-24T19:32:01.767Z", subdomain.datePublished.toString())
    }

    companion object {
        private val customDomain: ParseResult by lazy {
            parse(
                fixture = "www.astralcodexten.com/against-against-handover",
                url = "https://www.astralcodexten.com/p/against-against-handover",
            )
        }

        private val subdomain: ParseResult by lazy {
            parse(
                fixture = "garymarcus.substack.com/will-generative-ai-do-more-harm-than",
                url = "https://garymarcus.substack.com/p/will-generative-ai-do-more-harm-than",
            )
        }

        private fun figureImages(content: String): List<String> = Jsoup.parse(content).select("figure img").map { it.attr("src") }

        private fun parse(
            fixture: String,
            url: String,
        ): ParseResult {
            val html =
                SubstackComExtractorTest::class.java
                    .getResource("/fixtures/$fixture.html")!!
                    .readText()

            return runBlocking { Mercury.parse(url, ParseOptions(html = html)) }
        }
    }
}
