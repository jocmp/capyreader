package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class AddScoreTest {
    @Test
    fun `adds the specified amount to a node's score`() {
        val doc = Doc.load("<p score=\"25\">Foo</p>")
        val node = doc("p").first()
        addScore(node, doc, 25.0)
        assertEquals(50.0, getScore(node))
    }

    @Test
    fun `adds score if score not yet set (assumes score is 0)`() {
        val doc = Doc.load("<p>Foo</p>")
        val node = doc("p").first()
        addScore(node, doc, 25.0)
        assertEquals(25.0, getScore(node))
    }
}
