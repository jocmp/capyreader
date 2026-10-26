package com.jocmp.mercury.resource.utils.dom

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class NormalizeMetaTagsTest {
    @Test
    fun `replaces content attributes with value`() {
        val expected =
            "<html><head><meta name=\"foo\" value=\"bar\"></head><body></body></html>"

        val doc = Doc.load("<html><meta name=\"foo\" content=\"bar\"></html>")
        val result = normalizeMetaTags(doc).html()

        assertEquals(expected, result)
    }

    @Test
    fun `replaces property attributes with name`() {
        val expected =
            "<html><head><meta value=\"bar\" name=\"foo\"></head><body></body></html>"

        val doc = Doc.load("<html><meta property=\"foo\" value=\"bar\"></html>")
        val result = normalizeMetaTags(doc).html()

        assertEquals(expected, result)
    }
}
