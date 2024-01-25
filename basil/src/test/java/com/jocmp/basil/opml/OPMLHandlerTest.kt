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

    @Test
    fun parse_NestedFolders() {
        val factory = SAXParserFactory.newInstance()
        val saxParser = factory.newSAXParser()
        val handler = OPMLHandler()

        saxParser.parse("src/test/resources/nested_import.xml", handler)
        handler.opmlDocument

        val news = handler
            .opmlDocument
            .outlines
            .find { (it as? Outline.FolderOutline)?.folder?.title == "News" } as Outline.FolderOutline

        val topLevelOutlines = handler.opmlDocument.outlines.map { it.title }
        val newsFeeds = news.folder.feeds.map { it.title }
        val localNewsFeeds = news.folder.folders.first().feeds.map { it.title }

        assertEquals(expected = listOf("Daring Fireball", "News", "Julia Evans"), actual = topLevelOutlines)
        assertEquals(expected = listOf("BBC News - World", "NetNewsWire"), actual = newsFeeds)
        assertEquals(expected = listOf("Block Club Chicago"), actual = localNewsFeeds)
    }
}
