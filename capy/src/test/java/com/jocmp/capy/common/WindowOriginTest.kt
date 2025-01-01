package com.jocmp.capy.common

import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WindowOriginTest {
    @Test
    fun `with an null URL`() {
        val result = windowOrigin(null)

        assertNull(result)
    }

    @Test
    fun `with a valid URL`() {
        val result = windowOrigin(URL("https://developer.mozilla.org/en-US/docs/Web/API/Window/origin"))

        assertEquals(expected = "https://developer.mozilla.org", actual = result)
    }

    @Test
    fun `with an HTTP URL`() {
        val result = windowOrigin(URL("http://developer.mozilla.org/en-US/docs/Web/API/Window/origin"))

        assertEquals(expected = "http://developer.mozilla.org", actual = result)
    }

    @Test
    fun `with an explicit port`() {
        val result = windowOrigin(URL("https://example.com:3000"))

        assertEquals(expected = "https://example.com:3000", actual = result)
    }

    @Test
    fun `with a non-HTTP scheme`() {
        val result = windowOrigin(URL("file:///Users/test/my.html"))

        assertNull(result)
    }
}
