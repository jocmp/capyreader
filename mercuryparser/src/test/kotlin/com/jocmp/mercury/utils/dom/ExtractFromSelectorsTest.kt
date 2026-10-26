package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExtractFromSelectorsTest {
    @Test
    fun `extracts an arbitrary node by selector`() {
        val doc =
            Doc.load(
                """
            <html>
              <div class="author">Adam</div>
            </html>
            """,
                isDocument = false,
            )
        assertEquals("Adam", extractFromSelectors(doc, listOf(".author")))
    }

    @Test
    fun `ignores comments`() {
        val doc =
            Doc.load(
                """
            <html>
              <div class="comments-section">
                <div class="author">Adam</div>
              </div>
            </html>
            """,
                isDocument = false,
            )
        assertNull(extractFromSelectors(doc, listOf(".author")))
    }

    @Test
    fun `skips a selector if it matches multiple nodes`() {
        val doc =
            Doc.load(
                """
            <html>
              <div>
                <div class="author">Adam</div>
                <div class="author">Adam</div>
              </div>
            </html>
            """,
                isDocument = false,
            )
        assertNull(extractFromSelectors(doc, listOf(".author")))
    }

    @Test
    fun `skips a node with too many children`() {
        val doc =
            Doc.load(
                """
            <html>
              <div>
                <div class="author">
                  <span>Adam</span>
                  <span>Pash</span>
                </div>
              </div>
            </html>
            """,
                isDocument = false,
            )
        assertNull(extractFromSelectors(doc, listOf(".author")))
    }
}
