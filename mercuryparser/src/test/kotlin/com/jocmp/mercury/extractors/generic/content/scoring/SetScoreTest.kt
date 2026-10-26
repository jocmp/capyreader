package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class SetScoreTest {
    @Test
    fun `sets the specified amount as the node's score`() {
        val doc = Doc.load("<p>Foo</p>")
        val node = doc("p").first()
        val newScore = 25.0
        setScore(node, doc, newScore)

        val score = getScore(node)
        assertEquals(newScore, score)
    }
}
