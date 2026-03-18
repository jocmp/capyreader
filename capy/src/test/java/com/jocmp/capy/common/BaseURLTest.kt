package com.jocmp.capy.common

import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals

class BaseURLTest {
    @Test
    fun stripsPath() {
        val url = URL("https://telex.hu/rss")

        assertEquals(URL("https://telex.hu"), url.baseURL())
    }

    @Test
    fun preservesNonDefaultPort() {
        val url = URL("http://localhost:8080/feed")

        assertEquals(URL("http://localhost:8080"), url.baseURL())
    }

    @Test
    fun hostOnly() {
        val url = URL("https://example.com")

        assertEquals(URL("https://example.com"), url.baseURL())
    }
}
