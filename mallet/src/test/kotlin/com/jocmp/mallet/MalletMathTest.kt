package com.jocmp.mallet

import kotlin.test.Test
import kotlin.test.assertEquals

class MalletMathTest {
    private val baseUrl = "https://example.com"

    @Test
    fun `inline KaTeX keeps only its TeX source`() {
        val html = "<p>Let ${katex("F_0")} be zero.</p>"

        val result = Mallet.flatten(html, baseUrl).getOrThrow().elements

        assertEquals(
            listOf(
                LinearText(
                    ids = emptySet(),
                    "Let F_0 be zero.",
                    LinearTextBlockStyle.TEXT,
                    LinearTextAnnotation(LinearTextAnnotationMath("F_0"), 4, 6),
                ),
            ),
            result,
        )
    }

    @Test
    fun `inline source collapses whitespace but annotation keeps it`() {
        val tex = "a +\n  b"
        val html = "<p>${katex(tex)}</p>"

        val text = Mallet.flatten(html, baseUrl).getOrThrow().elements.single() as LinearText

        assertEquals("a + b", text.text)
        assertEquals(LinearTextAnnotationMath(tex), text.annotations.single().data)
    }

    @Test
    fun `display KaTeX becomes a math element`() {
        val html = "<p>Before</p><span class=\"katex-display\">${katex("\\frac{1}{2}")}</span><p>After</p>"

        val result = Mallet.flatten(html, baseUrl).getOrThrow().elements

        assertEquals(
            listOf(
                LinearText(ids = emptySet(), "Before", LinearTextBlockStyle.TEXT),
                LinearMath(ids = emptySet(), tex = "\\frac{1}{2}"),
                LinearText(ids = emptySet(), "After", LinearTextBlockStyle.TEXT),
            ),
            result,
        )
    }

    @Test
    fun `display KaTeX splits the paragraph around it`() {
        val html = "<p>Before <span class=\"katex-display\">${katex("x^2")}</span> after</p>"

        val result = Mallet.flatten(html, baseUrl).getOrThrow().elements

        assertEquals(3, result.size)
        assertEquals(LinearMath(ids = emptySet(), tex = "x^2"), result[1])
    }

    @Test
    fun `KaTeX without an annotation falls through to its text`() {
        val html = "<p><span class=\"katex\"><span class=\"katex-html\">x</span></span></p>"

        val text = Mallet.flatten(html, baseUrl).getOrThrow().elements.single() as LinearText

        assertEquals("x", text.text)
        assertEquals(emptyList(), text.annotations)
    }

    @Test
    fun `bare MathML block with TeX annotation`() {
        val html =
            """
            <math display="block"><semantics><mrow><mi>a</mi><mo>+</mo><mi>b</mi></mrow>
            <annotation encoding="application/x-tex">a+b</annotation></semantics></math>
            """.trimIndent()

        val result = Mallet.flatten(html, baseUrl).getOrThrow().elements

        assertEquals(listOf(LinearMath(ids = emptySet(), tex = "a+b")), result)
    }

    @Test
    fun `pandoc math spans drop their delimiters`() {
        val html = """<p>Area <span class="math inline">\(\pi r^2\)</span></p><p><span class="math display">\[E = mc^2\]</span></p>"""

        val result = Mallet.flatten(html, baseUrl).getOrThrow().elements

        val inline = result[0] as LinearText
        assertEquals("Area \\pi r^2", inline.text)
        assertEquals(LinearTextAnnotationMath("\\pi r^2"), inline.annotations.single().data)
        assertEquals(LinearMath(ids = emptySet(), tex = "E = mc^2"), result[1])
    }

    @Test
    fun `pandoc span wrapping KaTeX uses the annotation`() {
        val html = """<p><span class="math inline">${katex("F_0")}</span></p>"""

        val text = Mallet.flatten(html, baseUrl).getOrThrow().elements.single() as LinearText

        assertEquals("F_0", text.text)
    }

    @Test
    fun `MathJax 2 script source`() {
        val html = """<p>Energy</p><script type="math/tex; mode=display">E = mc^2</script>"""

        val result = Mallet.flatten(html, baseUrl).getOrThrow().elements

        assertEquals(LinearMath(ids = emptySet(), tex = "E = mc^2"), result.last())
    }

    private fun katex(tex: String): String =
        """<span class="katex"><span class="katex-mathml"><math xmlns="http://www.w3.org/1998/Math/MathML"><semantics><mrow><mi>x</mi></mrow><annotation encoding="application/x-tex">$tex</annotation></semantics></math></span><span class="katex-html" aria-hidden="true"><span class="base"><span class="mord mathnormal">x</span></span></span></span>"""
}
