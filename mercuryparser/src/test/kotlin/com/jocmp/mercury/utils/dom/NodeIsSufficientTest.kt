package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class NodeIsSufficientTest {
    @Test
    fun `returns false if node text length less than 100 chars`() {
        val doc =
            Doc.load(
                """
            <div class="foo bar">
              <p>This is too short</p>
            </div>
            """,
                isDocument = false,
            )

        assertEquals(false, nodeIsSufficient(doc.root()))
    }

    @Test
    fun `returns true if node text length greater than 100 chars`() {
        val doc =
            Doc.load(
                """
            <div class="foo bar">
              <p>
                Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean m
              </p>
            </div>
            """,
                isDocument = false,
            )

        assertEquals(true, nodeIsSufficient(doc.root()))
    }
}
