package com.jocmp.capy.accounts

import com.jocmp.rssparser.model.RssItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParsedItemTest {
    @Test
    fun title_whenPresent() {
        val title = "My Plain Title"
        val item = RssItem.Builder().title(title).build()
        val parsedItem = ParsedItem(item, siteURL = "")

        assertEquals(expected = title, actual = parsedItem.title)
    }

    @Test
    fun title_whenPresentAndHTML() {
        val title = "My <i>Fancy</i> Title"
        val item = RssItem.Builder().title(title).build()
        val parsedItem = ParsedItem(item, siteURL = "")

        assertEquals(expected = "My Fancy Title", actual = parsedItem.title)
    }

    @Test
    fun title_whenNull() {
        val item = RssItem.Builder().title(null).build()
        val parsedItem = ParsedItem(item, siteURL = "")

        assertEquals(expected = "", actual = parsedItem.title)
    }

    @Test
    fun id_whenUrlIsPresent() {
        val url = "https://example.com/article"
        val item = RssItem.Builder().link(url).build()
        val parsedItem = ParsedItem(item, siteURL = "")

        assertEquals(expected = url, actual = parsedItem.id)
    }

    @Test
    fun id_whenUrlIsMissing() {
        val id = "https://example.com/article"
        val item = RssItem.Builder().guid(id).build()
        val parsedItem = ParsedItem(item, siteURL = "")

        assertEquals(expected = id, actual = parsedItem.id)
    }

    @Test
    fun id_whenURLAndGuidAreMissing() {
        val item = RssItem.Builder().build()
        val parsedItem = ParsedItem(item, siteURL = "")

        assertNull(parsedItem.id)
    }

    @Test
    fun url_whenPresent() {
        val url = "https://example.com/article"
        val item = RssItem.Builder().link(url).build()
        val parsedItem = ParsedItem(item, siteURL = "")

        assertEquals(expected = url, actual = parsedItem.url)
    }

    @Test
    fun url_whenNull() {
        val item = RssItem.Builder().link(null).build()
        val parsedItem = ParsedItem(item, siteURL = "")

        assertEquals(expected = null, actual = parsedItem.url)
    }

    @Test
    fun url_whenBlank() {
        val item = RssItem.Builder().link("").build()
        val parsedItem = ParsedItem(item, siteURL = "")

        assertEquals(expected = null, actual = parsedItem.url)
    }

    @Test
    fun url_withRelativePathMissingSiteURL() {
        val item = RssItem.Builder().link("/article").build()
        val parsedItem = ParsedItem(item, siteURL = "")

        assertEquals(expected = null, actual = parsedItem.url)
    }

    @Test
    fun url_withRelativePathAndInvalidSiteURL() {
        val item = RssItem.Builder().link("/article").build()
        val parsedItem = ParsedItem(item, siteURL = "wrong")

        assertEquals(expected = null, actual = parsedItem.url)
    }

    @Test
    fun url_withRelativePathAndValidSiteURL() {
        val item = RssItem.Builder().link("/article").build()
        val parsedItem = ParsedItem(item, siteURL = "https://example.com")

        assertEquals(expected = "https://example.com/article", actual = parsedItem.url)
    }
}
