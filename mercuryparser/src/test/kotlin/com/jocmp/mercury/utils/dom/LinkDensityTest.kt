package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class LinkDensityTest {
    @Test
    fun `returns half if half of the text is a link`() {
        val doc =
            Doc.load(
                """
            <div><p>Some text!</p><p><a href="">Some text!</a></p> </div>
            """,
                isDocument = false,
            )

        val density = linkDensity(doc("div").first())
        assertEquals(0.5, density)
    }

    @Test
    fun `returns 1 if all of the text is a link`() {
        val doc =
            Doc.load(
                """
            <div><p><a href="">Some text!</a></p></div>
            """,
                isDocument = false,
            )

        val density = linkDensity(doc("div").first())
        assertEquals(1.0, density)
    }

    @Test
    fun `returns 0 if there's no text`() {
        val doc =
            Doc.load(
                """
            <div><p><a href=""></a></p></div>
            """,
                isDocument = false,
            )

        val density = linkDensity(doc("div").first())
        assertEquals(0.0, density)
    }
}
