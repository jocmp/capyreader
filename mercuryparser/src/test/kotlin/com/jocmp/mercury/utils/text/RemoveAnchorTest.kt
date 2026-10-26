package com.jocmp.mercury.utils.text

import kotlin.test.Test
import kotlin.test.assertEquals

class RemoveAnchorTest {
    @Test
    fun `returns a url w-out anchor`() {
        val url = "http://example.com/foo/bar/wow-cool/page=10/#wow"
        val cleaned = "http://example.com/foo/bar/wow-cool/page=10"

        assertEquals(cleaned, removeAnchor(url))
    }

    @Test
    fun `returns same url if url has no anchor found`() {
        val url = "http://example.com/foo/bar/wow-cool"
        val cleaned = "http://example.com/foo/bar/wow-cool"

        assertEquals(cleaned, removeAnchor(url))
    }
}
