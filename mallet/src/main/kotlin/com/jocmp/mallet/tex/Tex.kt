package com.jocmp.mallet.tex

/** A typeset formula and, for display math, its `\tag` laid out separately. */
class TexRender(
    val box: TexBox,
    val tag: TexBox?,
)

/**
 * Typesets TeX the way KaTeX does: KaTeX's parser and HTML builder, with [TexLayout] standing in
 * for the browser. Fails on anything KaTeX itself would reject.
 */
object Tex {
    /** KaTeX's `.katex { font-size: 1.21em }`: formulas are set larger than surrounding text. */
    const val FONT_SCALE = 1.21

    /** KaTeX's `maxSize` for untrusted input: no single rule, kern, or box exceeds this many ems. */
    private const val MAX_SIZE = 100.0

    /** Formulas larger than this many ems on either axis fail rather than make an unbounded view. */
    private const val MAX_EXTENT = 1000.0

    fun render(
        tex: String,
        displayMode: Boolean,
        measurer: TexTextMeasurer? = null,
    ): Result<TexRender> =
        runCatching {
            val settings = Settings(displayMode = displayMode, maxSize = MAX_SIZE)
            val tree = parseTree(tex, settings)
            val options =
                Options(
                    style = if (displayMode) Style.DISPLAY else Style.TEXT,
                    maxSize = settings.maxSize,
                    minRuleThickness = settings.minRuleThickness,
                )
            val html = buildHTML(tree, options)
            val layout = if (measurer != null) TexLayout(measurer, displayMode) else TexLayout(displayMode = displayMode)
            val (box, tag) = layout.layout(html)
            if (box.width > MAX_EXTENT || box.height + box.depth > MAX_EXTENT) {
                throw ParseError("Formula exceeds $MAX_EXTENT em")
            }
            TexRender(box, tag)
        }

    fun parseTree(tex: String, settings: Settings): List<ParseNode> {
        val parser = Parser(tex, settings)
        var tree = parser.parse()
        if (parser.gullet.macros.get("\\df@tag") != null) {
            if (!settings.displayMode) {
                throw ParseError("\\tag works only in display equations")
            }
            tree = listOf(ParseNode.Tag(Mode.TEXT, body = tree, tag = parser.subparse(listOf(Token("\\df@tag")))))
        }
        return tree
    }
}
