package com.jocmp.mercury.utils.text

import kotlin.test.Test
import kotlin.test.assertEquals

class GetEncodingTest {
    @Test
    fun `returns the encoding as a string`() {
        val contentType = "text/html; charset=iso-8859-15"
        assertEquals("iso-8859-15", getEncoding(contentType))
    }

    @Test
    fun `returns utf-8 as a default if no encoding found`() {
        val contentType = "text/html"
        assertEquals("utf-8", getEncoding(contentType))
    }

    @Test
    fun `returns utf-8 if there is an invalid encoding`() {
        val contentType = "text/html; charset=fake-charset"
        assertEquals("utf-8", getEncoding(contentType))
    }
}
