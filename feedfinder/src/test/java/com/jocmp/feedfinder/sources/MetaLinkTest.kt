package com.jocmp.feedfinder.sources

import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetaLinkTest {
    @Test
    fun find() {
//        val source = MetaLink(source = TestSource("arstechnica.html"))
//        val feed = source.find().first()
//
//        assertEquals(expected = URL("http://feeds.arstechnica.com/arstechnica/index"), actual = feed.url)
    }

    @Test
    fun `find is empty if document is missing`() {
//        val source = MetaLink(source = EmptySource())
//
//        assertTrue(source.find().isEmpty())
    }
}
