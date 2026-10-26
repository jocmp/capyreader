package com.jocmp.mercury.cleaners

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DekTest {
    @Test
    fun `returns null if the dek is less than 5 chars`() {
        val doc = Doc.load("<div></div>")
        assertNull(cleanDek("Hi", doc))
    }

    @Test
    fun `returns null if the dek is greater than 1000 chars`() {
        val doc = Doc.load("<div></div>")
        // generate a string that is 1,280 chars (10 * 2^7)
        var longDek = "0123456789"
        repeat(7) { longDek += longDek }
        assertNull(cleanDek(longDek, doc))
    }

    @Test
    fun `strip html tags from the dek`() {
        val doc = Doc.load("<div></div>")
        val dek = "This is a <em>very</em> important dek."
        assertEquals("This is a very important dek.", cleanDek(dek, doc))
    }

    @Test
    fun `returns null if dek contains plain text link`() {
        val doc = Doc.load("<div></div>")
        val dek = "This has this link http://example.com/foo/bar"
        assertNull(cleanDek(dek, doc))
    }

    @Test
    fun `returns a normal dek as is`() {
        val doc = Doc.load("<div></div>")
        val dek = "This is the dek"
        assertEquals(dek, cleanDek(dek, doc))
    }

    @Test
    fun `cleans extra whitespace`() {
        val doc = Doc.load("<div></div>")
        val dek = "    This is the dek   "
        assertEquals("This is the dek", cleanDek(dek, doc))
    }

    @Test
    fun `returns null if the dek is the same as the excerpt`() {
        val doc = Doc.load("<div></div>")
        val excerpt = "Hello to all of my friends"
        assertNull(cleanDek(excerpt, doc, excerpt = excerpt))
    }
}
