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

    @Test
    fun `transforms skip the content root`() {
        val extractor =
            extractor(domain = "example.com") {
                content {
                    selectors("article")
                    defaultCleaner = false
                    transform("div") { node, _ ->
                        if (node.text().startsWith("Disclaimer")) {
                            node.remove()
                        }
                        TransformResult.NoChange
                    }
                }
            }

        val content = extract("""<article><div><p>Disclaimer</p></div><p>Body text.</p></article>""", extractor)

        assertContains(content, "Body text.")
        assertFalse(content.contains("Disclaimer"), content)
        assertFalse(content.contains("\n"), content)
    }

    @Test
    fun `clean selectors skip the content root`() {
        val extractor =
            extractor(domain = "example.com") {
                content {
                    selectors("article")
                    defaultCleaner = false
                    clean("div:has(> article)")
                }
            }

        val content = extract("""<article><p>Body text.</p></article>""", extractor)

        assertContains(content, "Body text.")
        assertFalse(content.contains("\n"), content)
    }

    private fun extract(
        body: String,
        extractor: CustomExtractor = EXTRACTOR,
    ): String {
        val html = "<html><head></head><body>$body</body></html>"
        val doc = Doc.load(html, URL)
        val result: ParseResult =
            RootExtractor.extract(extractor, doc, URL, html, emptyList(), fallback = false)

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
