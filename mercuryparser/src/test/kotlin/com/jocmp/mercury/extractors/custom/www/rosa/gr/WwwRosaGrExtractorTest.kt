package com.jocmp.mercury.extractors.custom.www.rosa.gr

import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions
import com.jocmp.mercury.ParseResult
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WwwRosaGrExtractorTest {
    @Test
    fun `returns the metadata`() {
        assertEquals(
            "Χειμερινοί Ολυμπιακοί: Η ΔΟΕ απέκλεισε τον Ουκρανό αθλητή που φόρεσε κράνος με τα θύματα του πολέμου",
            result.title,
        )
        assertEquals("Ιωάννα Καρδούλια", result.author)
        assertEquals(Instant.parse("2026-02-12T12:12:00Z"), result.datePublished)
        assertEquals("https://www.rosa.gr/wp-content/uploads/2026/02/ukraine-athlitis.jpg", result.leadImageUrl)
    }

    @Test
    fun `keeps the hero image as a figure ahead of the body`() {
        val figures = content.select("figure")
        val figure = figures.first()!!

        assertEquals(1, figures.size)
        assertEquals(
            "https://www.rosa.gr/wp-content/uploads/2026/02/ukraine-athlitis.jpg",
            figure.selectFirst("> img")?.attr("src"),
        )
        assertTrue(figure.nextElementSibling()?.hasClass("post-text") == true)
    }

    @Test
    fun `drops a bare credit label`() {
        assertTrue(content.select("figcaption, .image-credits").isEmpty())
    }

    @Test
    fun `keeps a real credit as the figure caption`() {
        val credited = parse(fixture.replace("<span class=\"image-credits\">X</span>", "<span class=\"image-credits\">AP</span>"))
        val captions = Jsoup.parseBodyFragment(credited.content.orEmpty()).select("figure > figcaption")

        assertEquals(listOf("AP"), captions.map { it.text() })
    }

    @Test
    fun `cleans the read also block`() {
        assertTrue(content.select(".short-read-also").isEmpty())
        assertFalse(html.contains("cheimerinoi-olybiakoi-o-foros-timis-oukranou-athliti"))
    }

    @Test
    fun `excludes the social icons`() {
        assertTrue(content.select("svg, img[src*=rosa-theme/assets/images/]").isEmpty())
    }

    @Test
    fun `keeps the article body`() {
        assertTrue(html.contains("Αποκλείστηκε με απόφαση της ΔΟΕ"))
        assertTrue(html.contains("Ο υπουργός Εξωτερικών της χώρας"))
    }

    companion object {
        private const val URI =
            "https://www.rosa.gr/athlitismos/cheimerinoi-olybiakoi-i-doe-apekleise-ton-oukrano-athliti-pou-forese-kranos-me-ta-thymata-tou-polemou/"

        private val fixture: String by lazy {
            WwwRosaGrExtractorTest::class.java
                .getResource("/fixtures/www.rosa.gr/cheimerinoi-olybiakoi-i-doe-apekleise-ton-oukrano-athliti.html")!!
                .readText()
        }

        private val result: ParseResult by lazy { parse(fixture) }

        private val html: String by lazy { result.content.orEmpty() }

        private val content by lazy { Jsoup.parseBodyFragment(html).body() }

        private fun parse(html: String): ParseResult = runBlocking { Mercury.parse(URI, ParseOptions(html = html)) }
    }
}
