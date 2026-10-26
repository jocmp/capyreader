package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class FindTopCandidateTest {
    @Test
    fun `finds the top candidate from simple case`() {
        val doc =
            Doc.load(
                """
            <div score="100">
              <p score="1">Lorem ipsum etc</p>
            </div>
            """,
            )

        val topCandidate = findTopCandidate(doc)
        assertEquals(100.0, getScore(topCandidate))
    }

    @Test
    fun `finds the top candidate from a nested case`() {
        val doc =
            Doc.load(
                """
            <div score="10">
              <article score="50">
                <p score="1">Lorem ipsum etc</p>
              </article>
            </div>
            """,
            )

        val topCandidate = findTopCandidate(doc)
        // this is wrapped in a div so checking the score of the first child
        assertEquals(50.0, getScore(topCandidate.first()))
    }

    @Test
    fun `ignores tags like BR`() {
        val doc =
            Doc.load(
                """
            <article score="50">
              <p score="1">Lorem ipsum br</p>
              <br score="1000" />
            </article>
            """,
            )

        val topCandidate = findTopCandidate(doc)
        assertEquals(50.0, getScore(topCandidate))
    }

    @Test
    fun `returns BODY if no candidates found`() {
        val doc =
            Doc.load(
                """
            <body>
              <article>
                <p>Lorem ipsum etc</p>
                <br />
              </article>
            <body>
            """,
            )

        val topCandidate = findTopCandidate(doc)
        assertEquals("body", topCandidate.elements.firstOrNull()?.tagName())
    }
}
