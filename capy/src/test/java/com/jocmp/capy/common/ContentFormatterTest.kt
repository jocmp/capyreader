package com.jocmp.capy.common

import org.junit.Test
import kotlin.test.assertEquals

class ContentFormatterTest {
    @Test
    fun `returns empty string for null content`() {
        assertEquals("", ContentFormatter.summary(null))
    }

    @Test
    fun `returns empty string for blank content`() {
        assertEquals("", ContentFormatter.summary(""))
        assertEquals("", ContentFormatter.summary("   "))
    }

    @Test
    fun `strips HTML tags`() {
        val html = "<p>Hello <strong>world</strong></p>"

        assertEquals("Hello world", ContentFormatter.summary(html))
    }

    @Test
    fun `strips script and style tags`() {
        val html = """
            <style>.foo { color: red; }</style>
            <script>alert('hi')</script>
            <p>Visible text</p>
        """.trimIndent()

        assertEquals("Visible text", ContentFormatter.summary(html))
    }

    @Test
    fun `normalizes whitespace`() {
        val html = "<p>Hello    world</p><p>Another   paragraph</p>"

        assertEquals("Hello world Another paragraph", ContentFormatter.summary(html))
    }

    @Test
    fun `trims leading and trailing whitespace`() {
        val html = "  <p>  Hello world  </p>  "

        assertEquals("Hello world", ContentFormatter.summary(html))
    }

    @Test
    fun `truncates long content to 256 characters on word boundary`() {
        val word = "word "
        val html = "<p>${word.repeat(100)}</p>"
        val result = ContentFormatter.summary(html)

        assert(result.length <= 256) { "Expected length <= 256, got ${result.length}" }
        assert(!result.endsWith(" ")) { "Should not end with a space" }
    }

    @Test
    fun `truncates on word boundary without partial words`() {
        val words = (1..100).joinToString(" ") { "word$it" }
        val html = "<p>$words</p>"
        val result = ContentFormatter.summary(html)

        assert(result.length <= 256) { "Expected length <= 256, got ${result.length}" }

        val lastWord = result.split(" ").last()
        assert(lastWord.startsWith("word")) { "Last word should be complete: $lastWord" }
    }

    @Test
    fun `returns short content unchanged`() {
        val html = "<p>Short text</p>"

        assertEquals("Short text", ContentFormatter.summary(html))
    }

    @Test
    fun `handles HTML entities`() {
        val html = "<p>Tom &amp; Jerry &lt;3</p>"

        assertEquals("Tom & Jerry <3", ContentFormatter.summary(html))
    }

    @Test
    fun `handles nested HTML`() {
        val html = """
            <div>
                <h1>Title</h1>
                <p>First <a href="https://example.com">link</a> paragraph.</p>
                <ul>
                    <li>Item 1</li>
                    <li>Item 2</li>
                </ul>
            </div>
        """.trimIndent()
        val result = ContentFormatter.summary(html)

        assert(result.contains("Title"))
        assert(result.contains("link"))
        assert(result.contains("Item 1"))
    }

    @Test
    fun `no trailing omission characters`() {
        val longText = "a".repeat(300)
        val html = "<p>$longText</p>"
        val result = ContentFormatter.summary(html)

        assert(!result.endsWith("..."))
        assert(!result.endsWith("…"))
    }

    @Test
    fun `handles content with only whitespace after stripping HTML`() {
        val html = "<div>   <span>  </span>  </div>"

        assertEquals("", ContentFormatter.summary(html))
    }
}
