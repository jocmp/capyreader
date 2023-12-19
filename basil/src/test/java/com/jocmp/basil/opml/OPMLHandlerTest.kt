package com.jocmp.basil.opml

import org.junit.Test
import javax.xml.parsers.SAXParserFactory
import kotlin.test.assertEquals

class OPMLHandlerTest {
    @Test
    fun parse_TopLevelFeed() {
        val factory = SAXParserFactory.newInstance()
        val saxParser = factory.newSAXParser()
        val handler = OPMLHandler()

        saxParser.parse("src/test/resources/local.xml", handler)

        assertEquals(3, handler.opmlDocument.outlines.size)
    }
}
