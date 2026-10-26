package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.assertClean
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkToKeepTest {
    @Test
    fun `marks elements that should be kept`() {
        val doc =
            Doc.load(
                """
            <div>
              <p>What an article</p>
              <iframe src="https://www.youtube.com/embed/_2AqQV8wDvY" frameborder="0" allowfullscreen></iframe>
              <iframe src="foo" frameborder="0" allowfullscreen></iframe>
              <iframe src="https://player.vimeo.com/video/57712615"></iframe>
            </div>
            """,
                isDocument = false,
            )

        markToKeep(doc("*").first(), doc, null)

        assertEquals(2, doc("iframe.mercury-parser-keep").length)

        assertClean(
            doc("body").html(),
            """
              <div>
                <p>What an article</p>
                <iframe src="https://www.youtube.com/embed/_2AqQV8wDvY" frameborder="0" allowfullscreen class="mercury-parser-keep"></iframe>
                <iframe src="foo" frameborder="0" allowfullscreen></iframe>
                <iframe src="https://player.vimeo.com/video/57712615" class="mercury-parser-keep"></iframe>
              </div>
            """,
        )
    }

    @Test
    fun `marks same-domain elements to keep`() {
        val doc =
            Doc.load(
                "<div><iframe src=\"https://medium.com/foo/bar\"></iframe></div>",
                isDocument = false,
            )
        markToKeep(doc("*").first(), doc, "https://medium.com/foo")

        val keptHtml = "<div><iframe src=\"https://medium.com/foo/bar\" class=\"$KEEP_CLASS\"></iframe></div>"
        assertClean(doc("body").html(), keptHtml)
    }
}
