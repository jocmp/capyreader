package com.jocmp.mercury.extractors.custom.www.digitalfoundry.net

import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WwwDigitalfoundryNetExtractorTest {
    @Test
    fun `keeps the lazy-loaded YouTube embed`() {
        val content = parsedContent()

        assertTrue(
            content.contains("src=\"https://www.youtube.com/embed/hNkvZN2Cn80?"),
            "Expected the YouTube iframe to survive cleaning",
        )
    }

    @Test
    fun `cleans the see-also aside`() {
        assertFalse(parsedContent().contains("see-also"))
    }

    private fun parsedContent(): String {
        val html = javaClass.getResource("/fixtures/www.digitalfoundry.net/1790467953854.html")!!.readText()
        val uri = "https://www.digitalfoundry.net/features/world-of-warcraft-forever-brings-the-largest-visual-leap-in-the-mmos-history"

        val result = runBlocking { Mercury.parse(uri, ParseOptions(html = html)) }

        return result.content.orEmpty()
    }
}
