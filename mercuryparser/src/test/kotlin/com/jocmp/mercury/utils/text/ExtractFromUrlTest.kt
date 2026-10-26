package com.jocmp.mercury.utils.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExtractFromUrlTest {
    @Test
    fun `extracts datePublished from url`() {
        val url = "https://example.com/2012/08/01/this-is-good"
        val regexList = listOf(Regex("/(20\\d{2}/\\d{2}/\\d{2})/"))
        val result = extractFromUrl(url, regexList)

        assertEquals("2012/08/01", result)
    }

    @Test
    fun `returns null if nothing found`() {
        val url = "https://example.com/this-is-good"
        val regexList = listOf(Regex("/(20\\d{2}/\\d{2}/\\d{2})/"))
        val result = extractFromUrl(url, regexList)

        assertNull(result)
    }
}
