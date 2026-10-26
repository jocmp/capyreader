package com.jocmp.mercury.utils.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PageNumFromUrlTest {
    @Test
    fun `returns null if there is no page num in the url`() {
        assertNull(pageNumFromUrl("http://example.com"))
        assertNull(pageNumFromUrl("http://example.com/?pg=102"))
        assertNull(pageNumFromUrl("http://example.com/?page:102"))
    }

    @Test
    fun `returns a page num if one matches the url`() {
        assertEquals(1, pageNumFromUrl("http://example.com/foo?page=1"))
        assertEquals(1, pageNumFromUrl("http://example.com/foo?pg=1"))
        assertEquals(1, pageNumFromUrl("http://example.com/foo?p=1"))
        assertEquals(1, pageNumFromUrl("http://example.com/foo?paging=1"))
        assertEquals(1, pageNumFromUrl("http://example.com/foo?pag=1"))
        assertEquals(1, pageNumFromUrl("http://example.com/foo?pagination/1"))
        assertEquals(99, pageNumFromUrl("http://example.com/foo?paging/99"))
        assertEquals(99, pageNumFromUrl("http://example.com/foo?pa/99"))
        assertEquals(99, pageNumFromUrl("http://example.com/foo?p/99"))
    }
}
