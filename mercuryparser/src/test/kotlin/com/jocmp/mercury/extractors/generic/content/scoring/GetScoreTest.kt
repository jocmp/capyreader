package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetScoreTest {
    @Test
    fun `returns null if the node has no score set`() {
        val doc = Doc.load("<p>Foo</p>")
        assertNull(getScore(doc("p").first()))
    }

    @Test
    fun `returns 25 if the node has a score attr of 25`() {
        val doc = Doc.load("<p score=\"25\">Foo</p>")
        val score = getScore(doc("p").first())
        assertEquals(25.0, score)
    }
}
