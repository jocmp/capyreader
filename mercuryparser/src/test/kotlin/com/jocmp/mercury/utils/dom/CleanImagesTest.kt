package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.assertClean
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test

class CleanImagesTest {
    @Test
    fun `removes images with small heights or widths`() {
        val doc =
            Doc.load(
                """
            <div>
              <img width="5" height="5" />
              <img width="50" />
            </div>
            """,
                isDocument = false,
            )

        val result = cleanImages(doc("*").first(), doc)
        assertClean(
            result("body").html(),
            """
            <div>
              <img width="50">
            </div>
            """,
        )
    }

    @Test
    fun `removes height attribute from images that remain`() {
        val doc =
            Doc.load(
                """
            <div>
              <img width="50" height="50" />
            </div>
            """,
                isDocument = false,
            )

        val result = cleanImages(doc("*").first(), doc)
        assertClean(
            result("body").html(),
            """
            <div>
              <img width="50">
            </div>
            """,
        )
    }

    @Test
    fun `removes spacer or transparent images`() {
        val doc =
            Doc.load(
                """
            <div>
              <img src="/foo/bar/baz/spacer.png" />
              <img src="/foo/bar/baz/normal.png" />
              <p>Some text</p>
            </div>
            """,
                isDocument = false,
            )

        val result = cleanImages(doc("*").first(), doc)
        assertClean(
            result("body").html(),
            """
            <div>
              <img src="/foo/bar/baz/normal.png">
              <p>Some text</p>
            </div>
            """,
        )
    }
}
