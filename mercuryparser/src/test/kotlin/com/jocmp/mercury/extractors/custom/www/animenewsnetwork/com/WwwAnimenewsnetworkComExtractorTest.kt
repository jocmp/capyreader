package com.jocmp.mercury.extractors.custom.www.animenewsnetwork.com

import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions
import com.jocmp.mercury.ParseResult
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WwwAnimenewsnetworkComExtractorTest {
    @Test
    fun `keeps the news body and its image`() {
        assertTrue(newsContent.contains("announced on Friday that it has licensed storywriter"))
        assertTrue(newsContent.contains("src=\"https://cdn.animenewsnetwork.com/thumbnails/max300x600/cms/news.9/238726/face.jfif.jpeg\""))
    }

    @Test
    fun `keeps the intro and cleans the audio player`() {
        assertTrue(newsContent.contains("Manga about classmates who swap bodies"))
        assertFalse(newsContent.contains("instaread"))
    }

    @Test
    fun `unwraps the review layout tables`() {
        assertFalse(reviewContent.contains("<table"))
        assertTrue(reviewContent.contains("Tsubasa and Gyotaro are best friends"))
        assertTrue(reviewContent.contains("I've never seen a movie that looks quite like"))
    }

    @Test
    fun `labels the synopsis and review`() {
        val synopsis = reviewContent.indexOf("<strong>Synopsis:</strong>")
        val review = reviewContent.indexOf("<strong>Review:</strong>")

        assertTrue(synopsis >= 0)
        assertTrue(synopsis < reviewContent.indexOf("Tsubasa and Gyotaro are best friends"))
        assertTrue(review > reviewContent.indexOf("Tsubasa and Gyotaro are best friends"))
        assertTrue(review < reviewContent.indexOf("I've never seen a movie"))
    }

    @Test
    fun `keeps a single review cover image`() {
        val cover = "https://cdn.animenewsnetwork.com/thumbnails/area200x300/cms/review.2/238373/1373539-we-are-aliens-1-0-2000-0-3000-crop.jpg"

        assertEquals(1, Regex(Regex.escape("src=\"$cover\"")).findAll(reviewContent).count())
    }

    @Test
    fun `resolves lazy images from data-src`() {
        val content = lazyResult.content.orEmpty()

        assertEquals("School of Horns' Mito Aoi Launches New Manga", lazyResult.title)
        assertEquals(Instant.parse("2026-06-15T15:15:00Z"), lazyResult.datePublished)
        assertEquals(
            "https://www.animenewsnetwork.com/thumbnails/crop600x315gIB/cms/news.9/238512/fukuenflag.jpg",
            lazyResult.leadImageUrl,
        )
        assertTrue(content.contains("src=\"https://www.animenewsnetwork.com/thumbnails/max300x600/cms/news.9/238512/fukuenflag.jpg\""))
        assertFalse(content.contains("spacer.gif"))
        assertFalse(content.contains("data-src"))
    }

    @Test
    fun `unwraps nested layout tables`() {
        assertFalse(layoutContent.contains("<table"))
        assertEquals(1, Regex(Regex.escape("nested-cover.jpg")).findAll(layoutContent).count())
        assertEquals(1, Regex("Synopsis:").findAll(layoutContent).count())
    }

    @Test
    fun `keeps media-only layout cells`() {
        assertTrue(layoutContent.contains("<iframe"))
        assertTrue(layoutContent.contains("https://www.youtube.com/embed/abc123"))
    }

    @Test
    fun `keeps labels wrapped in elements`() {
        assertTrue(layoutContent.contains("<strong>Synopsis:</strong>"))
        assertTrue(layoutContent.contains("<strong>Review:</strong>"))
    }

    companion object {
        private val layoutContent: String by lazy {
            val html =
                """
                <html><body><div class="KonaBody">
                <table class="layout"><tbody>
                  <tr>
                    <td class="horizontal"><span style="position:absolute">Synopsis:</span></td>
                    <td>
                      <div class="floating-image fleft">
                        <table class="layout"><tbody><tr>
                          <td><img src="//cdn.animenewsnetwork.com/nested-cover.jpg" width="200" height="300"></td>
                          <td class="vertical"><div></div></td>
                        </tr></tbody></table>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td class="text-zone"><p>A synopsis that is long enough to read as article content, with some detail.</p></td>
                    <td><iframe src="https://www.youtube.com/embed/abc123" width="640" height="360"></iframe></td>
                  </tr>
                  <tr><td class="horizontal"><b>Review:</b></td><td class="vertical"></td></tr>
                </tbody></table>
                <p>The review body follows the layout table, and it runs long enough to count as content.</p>
                </div></body></html>
                """.trimIndent()

            runBlocking {
                Mercury.parse("https://www.animenewsnetwork.com/review/example/.1", ParseOptions(html = html))
            }.content.orEmpty()
        }

        private val newsContent: String by lazy {
            parse(
                fixture = "viz-media-releases-kiss-me-with-your-face-manga",
                uri = "https://www.animenewsnetwork.com/news/2026-06-20/viz-media-releases-yukiaki-kurando-wooma-kiss-me-with-your-face-manga-in-english/.238726",
            ).content.orEmpty()
        }

        private val reviewContent: String by lazy {
            parse(
                fixture = "we-are-aliens-anime-movie",
                uri = "https://www.animenewsnetwork.com/review/we-are-aliens-anime-movie/.238373",
            ).content.orEmpty()
        }

        private val lazyResult: ParseResult by lazy {
            parse(
                fixture = "1781536993736",
                uri = "https://www.animenewsnetwork.com/news/2026-06-14/school-of-horns-mito-aoi-launches-new-manga/.238512",
            )
        }

        private fun parse(
            fixture: String,
            uri: String,
        ): ParseResult {
            val html =
                WwwAnimenewsnetworkComExtractorTest::class.java
                    .getResource("/fixtures/www.animenewsnetwork.com/$fixture.html")!!
                    .readText()

            return runBlocking { Mercury.parse(uri, ParseOptions(html = html)) }
        }
    }
}
