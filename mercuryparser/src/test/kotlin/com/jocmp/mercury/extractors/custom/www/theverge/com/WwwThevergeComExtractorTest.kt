package com.jocmp.mercury.extractors.custom.www.theverge.com

import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class WwwThevergeComExtractorTest {
    @Test
    fun `handles images that share a parent`() {
        val html =
            """
            <html><body><article><div class="duet--layout--entry-body">
              <p>Lead paragraph with enough words to keep the content block around after cleaning.</p>
              <div class="gallery">
                <img srcset="https://example.com/a.jpg 1x" />
                <img srcset="https://example.com/b.jpg 1x" />
              </div>
              <p>Closing paragraph with enough words to keep the content block around after cleaning.</p>
            </div></article></body></html>
            """.trimIndent()

        val result =
            runBlocking {
                Mercury.parse("https://www.theverge.com/2026/10/23/example", ParseOptions(html = html))
            }

        assertTrue(result.content.orEmpty().contains("https://example.com/a.jpg"))
    }
}
