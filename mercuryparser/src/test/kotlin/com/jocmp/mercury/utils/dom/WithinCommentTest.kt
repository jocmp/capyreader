package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class WithinCommentTest {
    @Test
    fun `returns false if its parent is not a comment`() {
        val doc =
            Doc.load(
                """
            <div>
              <div>
                <div class="author">Adam</div>
              </div>
            </div>
            """,
                isDocument = false,
            )
        assertEquals(false, withinComment(doc(".author").first()))
    }

    @Test
    fun `returns true if its parent has a class of comment`() {
        val doc =
            Doc.load(
                """
            <div class="comments">
              <div>
                <div class="author">Adam</div>
              </div>
            </div>
            """,
                isDocument = false,
            )
        assertEquals(true, withinComment(doc(".author").first()))
    }

    @Test
    fun `returns true if its parent has an id of comment`() {
        val doc =
            Doc.load(
                """
            <div id="comment">
              <div>
                <div class="author">Adam</div>
              </div>
            </div>
            """,
                isDocument = false,
            )
        assertEquals(true, withinComment(doc(".author").first()))
    }
}
