package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.Response
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import java.net.URL
import kotlin.test.assertEquals

class XMLTest {
    @Test
    fun `it parses from an XML source`() = runBlocking {
        val body = File("src/test/resources/arstechnica_feed.xml").readText()

        val feeds = XML(Response(url = URL("https://arstechnica.com"), body = body)).find()

        assertEquals(expected = 1, actual = feeds.size)
    }
}
