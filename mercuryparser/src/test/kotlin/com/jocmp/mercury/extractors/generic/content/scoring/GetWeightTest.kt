package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class GetWeightTest {
    @Test
    fun `returns a score of 25 if node has positive id`() {
        val doc = Doc.load("<div id=\"entry\"><p>Ooo good one</p></div>")
        assertEquals(25, getWeight(doc("div").first()))
    }

    @Test
    fun `returns a score of -25 if node has negative id`() {
        val doc = Doc.load("<div id=\"adbox\"><p>Ooo good one</p></div>")
        assertEquals(-25, getWeight(doc("div").first()))
    }

    @Test
    fun `returns a score of 25 if node has positive class`() {
        val doc = Doc.load("<div class=\"entry\"><p>Ooo good one</p></div>")
        assertEquals(25, getWeight(doc("div").first()))
    }

    @Test
    fun `returns a score of -25 if node has negative class`() {
        val doc = Doc.load("<div id=\"comment ad\"><p>Ooo good one</p></div>")
        assertEquals(-25, getWeight(doc("div").first()))
    }

    @Test
    fun `returns a score of 25 if node has both positive id and class`() {
        val doc = Doc.load("<div id=\"article\" class=\"entry\"><p>Ooo good one</p></div>")
        assertEquals(25, getWeight(doc("div").first()))
    }

    @Test
    fun `returns a score of 25 if node has pos id and neg class`() {
        // is this really wanted? id="entry" class="adbox"
        // should get positive score?
        val doc = Doc.load("<div id=\"article\" class=\"adbox\"><p>Ooo good one</p></div>")
        assertEquals(25, getWeight(doc("div").first()))
    }

    @Test
    fun `returns a score of 10 if node has pos img class`() {
        val doc = Doc.load("<div class=\"figure\"><p>Ooo good one</p></div>")
        assertEquals(10, getWeight(doc("div").first()))
    }

    @Test
    fun `returns a score of 35 if node has pos id pos img class`() {
        val doc = Doc.load("<div id=\"article\" class=\"figure\"><p>Ooo good one</p></div>")
        assertEquals(35, getWeight(doc("div").first()))
    }

    @Test
    fun `adds an addl 25 (total 50) if node uses entry-content-asset class`() {
        val doc = Doc.load("<div id=\"foo\" class=\"entry-content-asset\"><p>Ooo good one</p></div>")
        assertEquals(50, getWeight(doc("div").first()))
    }
}
