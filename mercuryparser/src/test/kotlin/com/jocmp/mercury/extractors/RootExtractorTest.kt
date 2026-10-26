package com.jocmp.mercury.extractors

import com.jocmp.mercury.ParseResult
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class RootExtractorTest {
    @Test
    fun `makes custom content links absolute`() {
        val content = extract("""<article><p><a href="/foo/bar">Link</a><img src="/a.jpg"></p></article>""")

        assertContains(content, """href="https://example.com/foo/bar"""")
        assertContains(content, """src="https://example.com/a.jpg"""")
    }

    @Test
    fun `does not pretty print custom content`() {
        val content = extract("""<article><div><p>First <em>emphasis</em> text.</p></div></article>""")

        assertFalse(content.contains("\n"), content)
    }

    private fun extract(body: String): String {
        val html = "<html><head></head><body>$body</body></html>"
        val doc = Doc.load(html, URL)
        val result: ParseResult =
            RootExtractor.extract(EXTRACTOR, doc, URL, html, emptyList(), fallback = false)

        return result.content.orEmpty()
    }

    companion object {
        private const val URL = "https://example.com/posts/1"

        private val EXTRACTOR =
            extractor(domain = "example.com") {
                content {
                    selectors("article")
                    defaultCleaner = false
                }
            }
    }
}
