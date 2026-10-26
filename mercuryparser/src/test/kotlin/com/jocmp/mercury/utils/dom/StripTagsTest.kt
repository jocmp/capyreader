package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class StripTagsTest {
    @Test
    fun `strips tags from a string of text`() {
        val doc = Doc.load("<div></div>", isDocument = false)

        val result = stripTags("What a <em>Wonderful</em> Day", doc)
        assertEquals("What a Wonderful Day", result)
    }

    @Test
    fun `returns the original text if no tags found`() {
        val doc = Doc.load("<div></div>", isDocument = false)

        val result = stripTags("What a Wonderful Day", doc)
        assertEquals("What a Wonderful Day", result)
    }
}
