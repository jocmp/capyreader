package com.jocmp.mercury.cleaners

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LeadImageUrlTest {
    @Test
    fun `returns the url if valid`() {
        val url = "https://example.com/"
        assertEquals(url, cleanLeadImageUrl(url))
    }

    @Test
    fun `returns null if the url is not valid`() {
        assertNull(cleanLeadImageUrl("this is not a valid url"))
    }

    @Test
    fun `trims whitespace`() {
        val url = "  https://example.com/foo/bar.jpg"
        assertEquals(url.trim(), cleanLeadImageUrl(url))
    }
}
