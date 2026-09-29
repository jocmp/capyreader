package com.capyreader.app.ui.articles.reader

import org.junit.Assert.assertEquals
import org.junit.Test

class PageFooterTitleTest {
    @Test
    fun `keeps short titles`() {
        assertEquals("Oura is delaying its IPO.", PageFooterTitle.truncate("Oura is delaying its IPO."))
    }

    @Test
    fun `cuts long titles at the last space`() {
        assertEquals("Will Chinese AI…", PageFooterTitle.truncate("Will Chinese AI companies", limit = 18))
    }

    @Test
    fun `cuts long titles without spaces at the limit`() {
        assertEquals("abcde…", PageFooterTitle.truncate("abcdefghij", limit = 5))
    }
}
