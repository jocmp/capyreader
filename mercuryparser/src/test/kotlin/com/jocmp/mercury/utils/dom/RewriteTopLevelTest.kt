package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.assertClean
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class RewriteTopLevelTest {
    @Test
    fun `turns html and body tags into divs`() {
        val doc =
            Doc.load(
                """<html><body><div><p><a href="">Wow how about that</a></p></div></body></html>""",
            )
        rewriteTopLevel(doc("html").first(), doc)

        assertEquals(0, doc("html").length)
        assertEquals(0, doc("body").length)

        // Jsoup synthesizes a <head> element for parsed documents; the in-place
        // tagName rewrite preserves it. Upstream cheerio fragment mode has no
        // synthesized head, so it doesn't appear in their expected string.
        assertClean(
            doc.html(),
            """<div><head></head><div><div><p><a href="">Wow how about that</a></p></div></div></div>""",
        )
    }
}
