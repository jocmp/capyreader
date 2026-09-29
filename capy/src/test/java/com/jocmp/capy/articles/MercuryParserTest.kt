package com.jocmp.capy.articles

import kotlinx.coroutines.test.runTest
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MercuryParserTest {
    private val url = URL("https://example.com/posts/hello-world")

    @Test
    fun `extracts the article body from fetched html`() = runTest {
        val html = """
            <html>
              <head><title>Hello World</title></head>
              <body>
                <nav><a href="/">Home</a> <a href="/about">About</a></nav>
                <article>
                  <h1>Hello World</h1>
                  <p>${"This is the first paragraph of the article body. ".repeat(8)}</p>
                  <p>${"This is the second paragraph of the article body. ".repeat(8)}</p>
                  <p>${"This is the third paragraph of the article body. ".repeat(8)}</p>
                </article>
                <footer>Copyright</footer>
              </body>
            </html>
        """.trimIndent()
        val parser = MercuryParser { Result.success(html) }

        val content = parser.fetch(url).getOrThrow()

        assertTrue(content.contains("first paragraph of the article body"))
        assertTrue(content.contains("third paragraph of the article body"))
    }

    @Test
    fun `prepends the lead image when the body has none`() = runTest {
        val html = """
            <html>
              <head>
                <meta property="og:image" content="https://example.com/lead.jpg">
              </head>
              <body>
                <article>
                  <p>${"Body text without any pictures at all. ".repeat(12)}</p>
                  <p>${"More body text without any pictures at all. ".repeat(12)}</p>
                </article>
              </body>
            </html>
        """.trimIndent()
        val parser = MercuryParser { Result.success(html) }

        val content = parser.fetch(url).getOrThrow()

        assertTrue(content.startsWith("""<img src="https://example.com/lead.jpg">"""))
    }

    @Test
    fun `fails without a url`() = runTest {
        val parser = MercuryParser { Result.success("<html></html>") }

        val error = parser.fetch(null).exceptionOrNull()

        assertIs<ArticleContent.MissingURLError>(error)
    }

    @Test
    fun `passes through fetch failures`() = runTest {
        val failure = ArticleContent.HttpError(code = 403)
        val parser = MercuryParser { Result.failure(failure) }

        val error = parser.fetch(url).exceptionOrNull()

        assertEquals(failure, error)
    }
}
