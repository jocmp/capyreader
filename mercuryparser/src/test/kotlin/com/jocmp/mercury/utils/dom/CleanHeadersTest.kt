package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.assertClean
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test

class CleanHeadersTest {
    @Test
    fun `parses html and returns the article`() {
        val doc =
            Doc.load(
                """
            <div>
              <h2>Lose me</h2>
              <p>What do you think?</p>
              <h2>Keep me</h2>
              <p>What do you think?</p>
            </div>
            """,
                isDocument = false,
            )

        val result = cleanHeaders(doc("*").first(), doc)
        assertClean(
            result("body").html(),
            """
            <div>
              <p>What do you think?</p>
              <h2>Keep me</h2>
              <p>What do you think?</p>
            </div>
            """,
        )
    }

    @Test
    fun `removes headers when the header text matches the title`() {
        val doc =
            Doc.load(
                """
            <div>
              <p>What do you think?</p>
              <h2>Title Match</h2>
              <p>What do you think?</p>
            </div>
            """,
                isDocument = false,
            )

        val result = cleanHeaders(doc("*").first(), doc, "Title Match")
        assertClean(
            result("body").html(),
            """
            <div>
              <p>What do you think?</p>
              <p>What do you think?</p>
            </div>
            """,
        )
    }

    @Test
    fun `removes headers with a negative weight`() {
        val doc =
            Doc.load(
                """
            <div>
              <p>What do you think?</p>
              <h2 class="advert">Bad Class, Bad Weight</h2>
              <p>What do you think?</p>
            </div>
            """,
                isDocument = false,
            )

        val result = cleanHeaders(doc("*").first(), doc)
        assertClean(
            result("body").html(),
            """
            <div>
              <p>What do you think?</p>
              <p>What do you think?</p>
            </div>
            """,
        )
    }

    @Test
    fun `keeps headers with keep class`() {
        val doc =
            Doc.load(
                """
            <div>
              <h3 class="mercury-parser-keep">Keep me</h3>
              <p>What do you think?</p>
            </div>
            """,
                isDocument = false,
            )

        val result = cleanHeaders(doc("*").first(), doc)
        assertClean(
            result("body").html(),
            """
            <div>
              <h3 class="mercury-parser-keep">Keep me</h3>
              <p>What do you think?</p>
            </div>
            """,
        )
    }
}
