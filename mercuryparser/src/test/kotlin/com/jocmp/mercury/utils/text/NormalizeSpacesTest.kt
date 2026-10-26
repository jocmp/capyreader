package com.jocmp.mercury.utils.text

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class NormalizeSpacesTest {
    @Test
    fun `normalizes spaces from text`() {
        val doc =
            Doc.load(
                """
              <div>
                <p>What do you think?</p>
              </div>
            """,
            )

        val result = normalizeSpaces(doc("*").first().text())
        assertEquals("What do you think?", result)
    }

    @Test
    fun `preserves spaces in preformatted text blocks`() {
        val doc =
            Doc.load(
                """
              <div>
                <p>What   do  you    think?</p>
                <pre>  What     happens to        spaces?    </pre>
              </div>
            """,
                isDocument = false,
            )

        val result = normalizeSpaces(doc.html())
        assertEquals(
            "<div> <p>What do you think?</p> <pre>  What     happens to        spaces?    </pre> </div>",
            result,
        )
    }
}
