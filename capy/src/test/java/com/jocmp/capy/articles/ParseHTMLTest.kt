package com.jocmp.capy.articles

import com.jocmp.capy.fixtures.ArticleFixture
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class ParseHTMLTest {

    private val articleFixture by lazy { ArticleFixture() }

    @Test
    fun `escapes closing script tags in content`() {
        val html = """<p>some text</p><script>alert("hi")</script><p>more</p>"""
        val article = articleFixture.create().copy(
            contentHTML = html,
            content = html,
        )

        val result = parseHtml(article, hideImages = false)

        assertFalse(result.contains("</script><p>more</p>"))
        assertContains(result, "<\\/script>")
    }
}
