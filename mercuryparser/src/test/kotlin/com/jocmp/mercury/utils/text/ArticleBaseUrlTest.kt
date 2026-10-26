package com.jocmp.mercury.utils.text

import kotlin.test.Test
import kotlin.test.assertEquals

class ArticleBaseUrlTest {
    @Test
    fun `returns the base url of a paginated url`() {
        val url = "http://example.com/foo/bar/wow-cool/page=10"
        val cleaned = "http://example.com/foo/bar/wow-cool"

        assertEquals(cleaned, articleBaseUrl(url))
    }

    @Test
    fun `returns same url if url has no pagination info`() {
        val url = "http://example.com/foo/bar/wow-cool/"
        val cleaned = "http://example.com/foo/bar/wow-cool"

        assertEquals(cleaned, articleBaseUrl(url))
    }
}
