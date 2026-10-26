package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.assertClean
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test

class CleanAttributesTest {
    @Test
    fun `removes style attributes from nodes`() {
        val doc =
            Doc.load(
                """
            <div>
              <p style="color: red;">What do you think?</p>
            </div>
            """,
                isDocument = false,
            )

        val result = cleanAttributes(doc("body").children().first(), doc)
        assertClean(
            result.outerHtml(),
            """
            <div>
              <p>What do you think?</p>
            </div>
            """,
        )
    }

    @Test
    fun `removes align attributes from nodes`() {
        val doc =
            Doc.load(
                """
            <div>
              <p style="color: red;" align="center">What do you think?</p>
            </div>
            """,
                isDocument = false,
            )

        val result = cleanAttributes(doc("body").children().first(), doc)
        assertClean(
            result.outerHtml(),
            """
            <div>
              <p>What do you think?</p>
            </div>
            """,
        )
    }
}
