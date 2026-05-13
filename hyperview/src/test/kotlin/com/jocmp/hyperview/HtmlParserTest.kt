package com.jocmp.hyperview

import com.jocmp.hyperview.HtmlNode.Block
import com.jocmp.hyperview.HtmlNode.Inline
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlParserTest {

    @Test
    fun `parses a single paragraph`() {
        val doc = HtmlParser.parse("<p>Hello world</p>")
        assertEquals(
            listOf(Block.Paragraph(listOf(Inline.Text("Hello world")))),
            doc.blocks,
        )
    }

    @Test
    fun `wraps bare text in an implicit paragraph`() {
        val doc = HtmlParser.parse("Bare text outside any block")
        assertEquals(1, doc.blocks.size)
        val paragraph = doc.blocks.single() as Block.Paragraph
        assertEquals("Bare text outside any block", (paragraph.inlines.single() as Inline.Text).text)
    }

    @Test
    fun `bold and strong both produce BOLD style`() {
        val doc = HtmlParser.parse("<p><b>hard</b> and <strong>strong</strong></p>")
        val paragraph = doc.blocks.single() as Block.Paragraph
        val bolds = paragraph.inlines.filterIsInstance<Inline.Text>().filter { it.style.has(InlineStyle.BOLD) }
        assertEquals(listOf("hard", "strong"), bolds.map { it.text })
    }

    @Test
    fun `italic and em both produce ITALIC style`() {
        val doc = HtmlParser.parse("<p><i>i</i><em>em</em></p>")
        val paragraph = doc.blocks.single() as Block.Paragraph
        val italics = paragraph.inlines.filterIsInstance<Inline.Text>().filter { it.style.has(InlineStyle.ITALIC) }
        assertEquals(listOf("i", "em"), italics.map { it.text })
    }

    @Test
    fun `nested bold inside italic combines styles`() {
        val doc = HtmlParser.parse("<p><em>soft <strong>shout</strong></em></p>")
        val paragraph = doc.blocks.single() as Block.Paragraph
        val texts = paragraph.inlines.filterIsInstance<Inline.Text>()
        val shout = texts.first { it.text == "shout" }
        assertTrue(shout.style.has(InlineStyle.BOLD))
        assertTrue(shout.style.has(InlineStyle.ITALIC))
    }

    @Test
    fun `headings preserve level`() {
        val doc = HtmlParser.parse("<h1>A</h1><h2>B</h2><h3>C</h3><h4>D</h4><h5>E</h5><h6>F</h6>")
        val levels = doc.blocks.map { (it as Block.Heading).level }
        assertEquals(listOf(1, 2, 3, 4, 5, 6), levels)
    }

    @Test
    fun `anchor becomes inline link`() {
        val doc = HtmlParser.parse("""<p>See <a href="https://x.test" title="x">x</a></p>""")
        val paragraph = doc.blocks.single() as Block.Paragraph
        val link = paragraph.inlines.last() as Inline.Link
        assertEquals("https://x.test", link.href)
        assertEquals("x", link.title)
        assertEquals("x", (link.children.single() as Inline.Text).text)
    }

    @Test
    fun `br becomes inline line break`() {
        val doc = HtmlParser.parse("<p>line one<br>line two</p>")
        val paragraph = doc.blocks.single() as Block.Paragraph
        assertTrue(paragraph.inlines.any { it is Inline.LineBreak })
    }

    @Test
    fun `unordered list collects li children`() {
        val doc = HtmlParser.parse("<ul><li>one</li><li>two</li></ul>")
        val list = doc.blocks.single() as Block.UnorderedList
        assertEquals(2, list.items.size)
        val firstParagraph = list.items.first().children.single() as Block.Paragraph
        assertEquals("one", (firstParagraph.inlines.single() as Inline.Text).text)
    }

    @Test
    fun `ordered list respects start attribute`() {
        val doc = HtmlParser.parse("""<ol start="5"><li>x</li></ol>""")
        val list = doc.blocks.single() as Block.OrderedList
        assertEquals(5, list.start)
    }

    @Test
    fun `blockquote collects nested blocks`() {
        val doc = HtmlParser.parse("<blockquote><p>quoted</p></blockquote>")
        val quote = doc.blocks.single() as Block.Blockquote
        val paragraph = quote.children.single() as Block.Paragraph
        assertEquals("quoted", (paragraph.inlines.single() as Inline.Text).text)
    }

    @Test
    fun `pre with code preserves text and language`() {
        val doc = HtmlParser.parse("""<pre><code class="language-kotlin">val x = 1</code></pre>""")
        val code = doc.blocks.single() as Block.CodeBlock
        assertEquals("val x = 1", code.text)
        assertEquals("kotlin", code.language)
    }

    @Test
    fun `pre without code still captures text`() {
        val doc = HtmlParser.parse("<pre>raw text</pre>")
        val code = doc.blocks.single() as Block.CodeBlock
        assertEquals("raw text", code.text)
        assertEquals(null, code.language)
    }

    @Test
    fun `inline code in paragraph stays inline`() {
        val doc = HtmlParser.parse("<p>use <code>map</code> for that</p>")
        val paragraph = doc.blocks.single() as Block.Paragraph
        val code = paragraph.inlines.filterIsInstance<Inline.Code>().single()
        assertEquals("map", code.text)
    }

    @Test
    fun `image preserves attributes`() {
        val doc = HtmlParser.parse("""<img src="a.jpg" alt="alt" title="t" width="120" height="80">""")
        val image = doc.blocks.single() as Block.Image
        assertEquals("a.jpg", image.src)
        assertEquals("alt", image.alt)
        assertEquals("t", image.title)
        assertEquals(120, image.width)
        assertEquals(80, image.height)
    }

    @Test
    fun `image falls back to data-src when src empty`() {
        val doc = HtmlParser.parse("""<img data-src="lazy.jpg">""")
        val image = doc.blocks.single() as Block.Image
        assertEquals("lazy.jpg", image.src)
    }

    @Test
    fun `image srcset with width descriptors parses candidates`() {
        val doc = HtmlParser.parse(
            """<img src="a.jpg" srcset="a-480.jpg 480w, a-960.jpg 960w, a-1440.jpg 1440w" sizes="(max-width: 600px) 480px, 960px">"""
        )
        val image = doc.blocks.single() as Block.Image
        assertEquals(listOf(480, 960, 1440), image.sources.map { it.width })
        assertEquals("(max-width: 600px) 480px, 960px", image.sizes)
    }

    @Test
    fun `image srcset with density descriptors parses candidates`() {
        val doc = HtmlParser.parse("""<img src="a.jpg" srcset="a.jpg 1x, a@2x.jpg 2x, a@3x.jpg 3x">""")
        val image = doc.blocks.single() as Block.Image
        assertEquals(listOf(1f, 2f, 3f), image.sources.map { it.density })
    }

    @Test
    fun `picture element collects sources with media and type`() {
        val doc = HtmlParser.parse(
            """<picture>
                |<source srcset="a.avif 1x, a@2x.avif 2x" type="image/avif">
                |<source srcset="a.webp 1x" type="image/webp" media="(min-width: 800px)">
                |<img src="a.jpg" alt="alt">
                |</picture>""".trimMargin()
        )
        val image = doc.blocks.single() as Block.Image
        assertEquals("a.jpg", image.src)
        assertEquals("alt", image.alt)
        assertEquals(3, image.sources.size)
        assertEquals("image/avif", image.sources[0].mediaType)
        assertEquals("image/avif", image.sources[1].mediaType)
        assertEquals("image/webp", image.sources[2].mediaType)
        assertEquals("(min-width: 800px)", image.sources[2].media)
    }

    @Test
    fun `bestUrl picks the smallest width candidate at or above the target`() {
        val doc = HtmlParser.parse(
            """<img src="a.jpg" srcset="a-480.jpg 480w, a-960.jpg 960w, a-1440.jpg 1440w">"""
        )
        val image = doc.blocks.single() as Block.Image
        assertEquals("a-960.jpg", image.bestUrl(targetWidthPx = 700))
        assertEquals("a-480.jpg", image.bestUrl(targetWidthPx = 200))
        assertEquals("a-1440.jpg", image.bestUrl(targetWidthPx = 5000))
    }

    @Test
    fun `bestUrl picks matching density when no width candidates`() {
        val doc = HtmlParser.parse("""<img src="a.jpg" srcset="a.jpg 1x, a@2x.jpg 2x">""")
        val image = doc.blocks.single() as Block.Image
        assertEquals("a@2x.jpg", image.bestUrl(pixelDensity = 2f))
        assertEquals("a.jpg", image.bestUrl(pixelDensity = 1f))
    }

    @Test
    fun `bestUrl falls back to src when no candidates`() {
        val doc = HtmlParser.parse("""<img src="only.jpg">""")
        val image = doc.blocks.single() as Block.Image
        assertEquals("only.jpg", image.bestUrl(targetWidthPx = 1000))
    }

    @Test
    fun `hr becomes a horizontal rule block`() {
        val doc = HtmlParser.parse("<hr>")
        assertEquals(Block.HorizontalRule, doc.blocks.single())
    }

    @Test
    fun `figure with figcaption extracts caption inlines`() {
        val doc = HtmlParser.parse(
            """<figure><img src="a.jpg"><figcaption>cap <em>text</em></figcaption></figure>"""
        )
        val figure = doc.blocks.single() as Block.Figure
        assertTrue(figure.children.single() is Block.Image)
        val captionTexts = figure.caption!!.filterIsInstance<Inline.Text>()
        assertEquals("cap ", captionTexts.first().text)
        assertEquals("text", captionTexts.last().text)
        assertTrue(captionTexts.last().style.has(InlineStyle.ITALIC))
    }

    @Test
    fun `video collects sources and poster`() {
        val doc = HtmlParser.parse(
            """<video poster="p.jpg"><source src="a.mp4" type="video/mp4"><source src="a.webm" type="video/webm"></video>"""
        )
        val video = doc.blocks.single() as Block.Video
        assertEquals("p.jpg", video.poster)
        assertEquals(listOf("a.mp4", "a.webm"), video.sources.map { it.src })
        assertEquals(listOf("video/mp4", "video/webm"), video.sources.map { it.type })
    }

    @Test
    fun `iframe captures src and title`() {
        val doc = HtmlParser.parse("""<iframe src="https://youtube.test/embed/x" title="t"></iframe>""")
        val iframe = doc.blocks.single() as Block.Iframe
        assertEquals("https://youtube.test/embed/x", iframe.src)
        assertEquals("t", iframe.title)
    }

    @Test
    fun `iframe without src is dropped`() {
        val doc = HtmlParser.parse("<iframe></iframe>")
        assertEquals(emptyList(), doc.blocks)
    }

    @Test
    fun `table extracts header rows and cells`() {
        val doc = HtmlParser.parse(
            "<table><thead><tr><th>h1</th><th>h2</th></tr></thead><tbody><tr><td>a</td><td>b</td></tr></tbody></table>"
        )
        val table = doc.blocks.single() as Block.Table
        assertEquals(2, table.rows.size)
        assertTrue(table.rows.first().header)
        assertEquals(false, table.rows.last().header)
        assertEquals(2, table.rows.last().cells.size)
    }

    @Test
    fun `details extracts summary and body`() {
        val doc = HtmlParser.parse("<details><summary>s</summary><p>body</p></details>")
        val details = doc.blocks.single() as Block.Details
        assertEquals("s", (details.summary.single() as Inline.Text).text)
        val paragraph = details.children.single() as Block.Paragraph
        assertEquals("body", (paragraph.inlines.single() as Inline.Text).text)
    }

    @Test
    fun `div container is transparent`() {
        val doc = HtmlParser.parse("<div><p>one</p><p>two</p></div>")
        assertEquals(2, doc.blocks.size)
        assertTrue(doc.blocks.all { it is Block.Paragraph })
    }

    @Test
    fun `article and section containers are transparent`() {
        val doc = HtmlParser.parse("<article><section><p>x</p></section></article>")
        assertEquals(1, doc.blocks.size)
        assertTrue(doc.blocks.single() is Block.Paragraph)
    }

    @Test
    fun `script and style content is stripped`() {
        val doc = HtmlParser.parse(
            "<p>before</p><script>var x = 1;</script><style>p { color: red; }</style><p>after</p>"
        )
        assertEquals(2, doc.blocks.size)
        assertTrue(doc.blocks.all { it is Block.Paragraph })
    }

    @Test
    fun `unknown inline tag falls through to its text`() {
        val doc = HtmlParser.parse("<p>see <custom>thing</custom> here</p>")
        val paragraph = doc.blocks.single() as Block.Paragraph
        val joined = paragraph.inlines.filterIsInstance<Inline.Text>().joinToString("") { it.text }
        assertEquals("see thing here", joined)
    }

    @Test
    fun `sub and sup are flagged`() {
        val doc = HtmlParser.parse("<p>H<sub>2</sub>O E=mc<sup>2</sup></p>")
        val paragraph = doc.blocks.single() as Block.Paragraph
        val texts = paragraph.inlines.filterIsInstance<Inline.Text>()
        assertTrue(texts.first { it.text == "2" }.style.has(InlineStyle.SUBSCRIPT))
        assertTrue(texts.last { it.text == "2" }.style.has(InlineStyle.SUPERSCRIPT))
    }

    @Test
    fun `mark and small are flagged`() {
        val doc = HtmlParser.parse("<p><mark>hi</mark> <small>note</small></p>")
        val paragraph = doc.blocks.single() as Block.Paragraph
        val texts = paragraph.inlines.filterIsInstance<Inline.Text>()
        assertTrue(texts.first { it.text == "hi" }.style.has(InlineStyle.MARK))
        assertTrue(texts.first { it.text == "note" }.style.has(InlineStyle.SMALL))
    }

    @Test
    fun `leading and trailing whitespace is trimmed inside paragraph`() {
        val doc = HtmlParser.parse("<p>   hello   </p>")
        val paragraph = doc.blocks.single() as Block.Paragraph
        assertEquals("hello", (paragraph.inlines.single() as Inline.Text).text.trim())
    }

    @Test
    fun `image inside a paragraph is promoted to a sibling block`() {
        val doc = HtmlParser.parse("""<p>before <img src="a.jpg"> after</p>""")
        assertEquals(3, doc.blocks.size)
        val first = doc.blocks[0] as Block.Paragraph
        val img = doc.blocks[1] as Block.Image
        val last = doc.blocks[2] as Block.Paragraph
        assertEquals("before ", (first.inlines.single() as Inline.Text).text)
        assertEquals("a.jpg", img.src)
        assertEquals(" after", (last.inlines.single() as Inline.Text).text)
    }

    @Test
    fun `image inside an anchor is rendered as a sibling block`() {
        val doc = HtmlParser.parse("""<a href="x"><img src="a.jpg"></a>""")
        val image = doc.blocks.single() as Block.Image
        assertEquals("a.jpg", image.src)
    }

    @Test
    fun `image inside a span inside a paragraph still renders`() {
        val doc = HtmlParser.parse("""<p><span><img src="a.jpg"></span></p>""")
        assertEquals(1, doc.blocks.size)
        assertTrue(doc.blocks.single() is Block.Image)
    }

    @Test
    fun `empty document yields no blocks`() {
        assertEquals(emptyList(), HtmlParser.parse("").blocks)
        assertEquals(emptyList(), HtmlParser.parse("   ").blocks)
        assertEquals(emptyList(), HtmlParser.parse("<!-- nothing -->").blocks)
    }
}
