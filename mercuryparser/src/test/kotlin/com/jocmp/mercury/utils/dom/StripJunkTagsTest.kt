package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.assertClean
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class StripJunkTagsTest {
    @Test
    fun `strips script and other junk tags`() {
        val doc =
            Doc.load(
                """
            <div>
              <style>.red { color: 'red'; }</style>
              <title>WOW</title>
              <link rel="asdflkjawef" />
              <p>What an article</p>
              <script type="text/javascript">alert('hi!');</script>
              <noscript>Don't got it</noscript>
              <hr />
            </div>
            """,
                isDocument = false,
            )

        stripJunkTags(doc("*").first(), doc)
        assertClean(
            doc("body").html(),
            """
            <div>
              <p>What an article</p>
            </div>
            """,
        )
    }

    @Test
    fun `keeps youtube embeds`() {
        val doc =
            Doc.load(
                """
            <div>
              <style>.red { color: 'red'; }</style>
              <title>WOW</title>
              <link rel="asdflkjawef" />
              <p>What an article</p>
              <iframe class="mercury-parser-keep" src="https://www.youtube.com/embed/_2AqQV8wDvY" frameborder="0" allowfullscreen></iframe>
              <hr />
            </div>
            """,
                isDocument = false,
            )

        stripJunkTags(doc("*").first(), doc)
        assertEquals(1, doc("iframe[src^=\"https://www.youtube.com\"]").length)
    }
}
