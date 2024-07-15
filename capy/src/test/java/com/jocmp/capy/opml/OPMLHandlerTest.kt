package com.jocmp.capy.opml

import com.jocmp.capy.testFile
import org.junit.Test
import javax.xml.parsers.SAXParserFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OPMLHandlerTest {
    @Test
    fun parse_TopLevelFeed() {
        val outlines = OPMLHandler.parse(testFile("local.xml").inputStream())

        assertEquals(3, outlines.size)
    }

    @Test
    fun parse_NestedFolders() {
        val outlines = OPMLHandler.parse(testFile("nested_import.xml").inputStream())

        val news = outlines
            .find { (it as? Outline.FolderOutline)?.folder?.title == "News" } as Outline.FolderOutline

        val topLevelOutlines = outlines.map { it.title }
        val newsFeeds = news.folder.feeds.map { it.title }
        val localNewsFeeds = news.folder.folders.first().feeds.map { it.title }

        assertEquals(
            expected = listOf("Daring Fireball", "News", "Julia Evans"),
            actual = topLevelOutlines
        )
        assertEquals(expected = listOf("BBC News - World", "NetNewsWire"), actual = newsFeeds)
        assertEquals(expected = listOf("Block Club Chicago"), actual = localNewsFeeds)
    }

    @Test
    fun `it handles invalid characters like ampersands`() {
        val inputStream = testFile("local_with_invalid_characters.xml").inputStream()

        val outlines = OPMLHandler.parse(inputStream)

        assertTrue(outlines.size == 3)
        assertNotNull(outlines.find { it.title == "R&A Enterprise Architecture" })
    }
}
