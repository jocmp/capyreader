package com.jocmp.mercury.resource.utils.dom

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class CleanTest {
    @Test
    fun `removes script elements`() {
        val html = "<div><script>alert('hi')</script></div>"
        val doc = Doc.load(html)

        assertEquals("<div></div>", clean(doc)("body").html())
    }

    @Test
    fun `removes style elements`() {
        val html = "<div><style>foo: {color: red;}</style></div>"
        val doc = Doc.load(html)

        assertEquals("<div></div>", clean(doc)("body").html())
    }

    @Test
    fun `removes comments`() {
        val html = "<div>HI <!-- This is a comment --></div>"
        val doc = Doc.load(html)

        assertEquals("<div>HI </div>", clean(doc)("body").html())
    }
}
