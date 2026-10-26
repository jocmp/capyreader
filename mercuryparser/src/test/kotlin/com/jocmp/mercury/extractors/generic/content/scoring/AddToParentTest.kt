package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class AddToParentTest {
    @Test
    fun `adds 1-4 of a node's score to its parent`() {
        val doc = Doc.load("<div score=\"25\"><p score=\"40\">Foo</p></div>")
        val node = addToParent(doc("p").first(), doc, 40.0)

        assertEquals(35.0, getScore(node.parent()))
        assertEquals(40.0, getScore(node))
    }
}
