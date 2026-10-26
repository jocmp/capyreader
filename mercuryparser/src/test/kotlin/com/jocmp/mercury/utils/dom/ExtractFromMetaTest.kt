package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExtractFromMetaTest {
    @Test
    fun `extracts an arbitrary meta tag by name`() {
        val doc =
            Doc.load(
                """
            <html>
              <meta name="foo" value="bar" />
            </html>
            """,
                isDocument = false,
            )
        val result = extractFromMeta(doc, listOf("foo", "baz"), listOf("foo", "bat"))
        assertEquals("bar", result)
    }

    @Test
    fun `returns nothing if a meta name is duplicated`() {
        val doc =
            Doc.load(
                """
            <html>
              <meta name="foo" value="bar" />
              <meta name="foo" value="baz" />
            </html>
            """,
                isDocument = false,
            )
        val result = extractFromMeta(doc, listOf("foo", "baz"), listOf("foo", "bat"))
        assertNull(result)
    }

    @Test
    fun `ignores duplicate meta names with empty values`() {
        val doc =
            Doc.load(
                """
            <html>
              <meta name="foo" value="bar" />
              <meta name="foo" value="" />
            </html>
            """,
                isDocument = false,
            )
        val result = extractFromMeta(doc, listOf("foo", "baz"), listOf("foo", "bat"))
        assertEquals("bar", result)
    }
}
