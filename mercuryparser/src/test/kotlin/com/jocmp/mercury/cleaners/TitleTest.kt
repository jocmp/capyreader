package com.jocmp.mercury.cleaners

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class TitleTest {
    @Test
    fun `only uses h1 if there is only one on the page`() {
        val title = "Too Short"
        val doc =
            Doc.load(
                """
            <div>
              <h1>This Is the Real Title</h1>
              <h1>This Is the Real Title</h1>
            </div>
            """,
            )

        assertEquals(title, cleanTitle(title, doc))
    }

    @Test
    fun `removes HTML tags from titles`() {
        val doc = Doc.load("<div><h1>This Is the <em>Real</em> Title</h1></div>")
        val title = doc("h1").html()

        assertEquals("This Is the Real Title", cleanTitle(title, doc))
    }

    @Test
    fun `trims extraneous spaces`() {
        val title = " This Is a Great Title That You'll Love "
        val doc = Doc.load("<div><h1>This Is the <em>Real</em> Title</h1></div>")

        assertEquals(title.trim(), cleanTitle(title, doc))
    }
}
