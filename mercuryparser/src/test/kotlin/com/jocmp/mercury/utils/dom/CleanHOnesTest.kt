package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.assertClean
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test

class CleanHOnesTest {
    @Test
    fun `removes H1s if there are less than 3 of them`() {
        val doc =
            Doc.load(
                """
            <div>
              <h1>Look at this!</h1>
              <p>What do you think?</p>
              <h1>Can you believe it?!</h1>
            </div>
            """,
                isDocument = false,
            )

        val result = cleanHOnes(doc("*").first(), doc)
        assertClean(
            result("body").html(),
            """
            <div>
              <p>What do you think?</p>
            </div>
            """,
        )
    }

    @Test
    fun `converts H1s to H2s if there are 3 or more of them`() {
        val doc =
            Doc.load(
                """
            <div>
              <h1>Look at this!</h1>
              <p>What do you think?</p>
              <h1>Can you believe it?!</h1>
              <p>What do you think?</p>
              <h1>Can you believe it?!</h1>
            </div>
            """,
                isDocument = false,
            )

        val result = cleanHOnes(doc("*").first(), doc)
        assertClean(
            result("body").html(),
            """
            <div>
              <h2>Look at this!</h2>
              <p>What do you think?</p>
              <h2>Can you believe it?!</h2>
              <p>What do you think?</p>
              <h2>Can you believe it?!</h2>
            </div>
            """,
        )
    }
}
