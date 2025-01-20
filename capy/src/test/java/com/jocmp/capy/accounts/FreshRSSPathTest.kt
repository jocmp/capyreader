package com.jocmp.capy.accounts

import kotlin.test.Test
import kotlin.test.assertEquals

class FreshRSSPathTest {
    @Test
    fun `with a source that isn't FreshRSS`() {
        val input = "https://example.com/"
        val result = withFreshRSSPath(input, Source.READER)

        assertEquals(
            expected = "https://example.com/",
            actual = result
        )
    }

    @Test
    fun `with a FreshRSS source`() {
        val input = "https://example.com/"
        val result = withFreshRSSPath(input, Source.FRESHRSS)

        assertEquals(
            expected = "https://example.com/api/greader.php/",
            actual = result
        )
    }

    @Test
    fun `with a source with the API path`() {
        val input = "https://example.com/api/greader.php/"
        val result = withFreshRSSPath(input, Source.READER)

        assertEquals(
            expected = "https://example.com/api/greader.php/",
            actual = result
        )
    }
}
