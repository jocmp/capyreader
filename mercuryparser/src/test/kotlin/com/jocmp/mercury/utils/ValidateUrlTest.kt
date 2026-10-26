package com.jocmp.mercury.utils

import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateUrlTest {
    @Test
    fun `returns false if url is not valid`() {
        val url = URI.create("example.com")
        assertEquals(false, validateUrl(url))
    }

    @Test
    fun `returns true if url is valid`() {
        val url = URI.create("http://example.com")
        assertEquals(true, validateUrl(url))
    }
}
