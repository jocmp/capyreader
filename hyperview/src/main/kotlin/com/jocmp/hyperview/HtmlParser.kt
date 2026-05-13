package com.jocmp.hyperview

import org.jsoup.Jsoup
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser

object HtmlParser {

    fun parse(html: String): HtmlDocument {
        val body = Jsoup.parse(html, "", Parser.htmlParser()).body()
        val blocks = collectBlocks(body.childNodes())
        return HtmlDocument(blocks)
    }

    private fun collectBlocks(children: List<Node>): List<HtmlNode.Block> {
        val out = mutableListOf<HtmlNode.Block>()
        val inlineBuffer = mutableListOf<HtmlNode.Inline>()

        fun flushInline() {
            if (inlineBuffer.isNotEmpty()) {
                val trimmed = trim(inlineBuffer)
                if (trimmed.isNotEmpty()) {
                    out += HtmlNode.Block.Paragraph(trimmed)
                }
                inlineBuffer.clear()
            }
        }

        for (node in children) {
            when (node) {
                is TextNode -> {
                    val text = node.text()
                    if (text.isNotBlank() || inlineBuffer.isNotEmpty()) {
                        inlineBuffer += HtmlNode.Inline.Text(text)
                    }
                }
                is Element -> {
                    val tag = node.tagName().lowercase()
                    when {
                        tag in SKIP_TAGS -> Unit
                        tag in CONTAINER_TAGS -> {
                            flushInline()
                            out += collectBlocks(node.childNodes())
                        }
                        // A `<p>` that wraps block-level media (very common: `<p><img></p>`,
                        // `<p>before<img>after</p>`) must be flattened — otherwise the inline
                        // collector swallows the image. Descend like a container; surrounding
                        // text becomes implicit paragraphs around the media.
                        tag == "p" && node.select(MEDIA_SELECTOR).isNotEmpty() -> {
                            flushInline()
                            out += collectBlocks(node.childNodes())
                        }
                        tag in BLOCK_TAGS || tag in MEDIA_TAGS -> {
                            flushInline()
                            parseBlock(node, tag)?.let { out += it }
                        }
                        // An inline element (anchor, span, em…) that wraps a block-level
                        // image/video/etc. can't render inline. Descend so the media
                        // appears as a sibling block; the wrapping anchor is dropped.
                        node.select(MEDIA_SELECTOR).isNotEmpty() -> {
                            flushInline()
                            out += collectBlocks(node.childNodes())
                        }
                        else -> inlineBuffer += parseInline(node)
                    }
                }
                is Comment -> Unit
                else -> Unit
            }
        }

        flushInline()
        return out
    }

    private fun parseBlock(element: Element, tag: String): HtmlNode.Block? {
        return when (tag) {
            "p" -> HtmlNode.Block.Paragraph(collectInline(element))
            "h1", "h2", "h3", "h4", "h5", "h6" ->
                HtmlNode.Block.Heading(level = tag.substring(1).toInt(), inlines = collectInline(element))
            "blockquote" -> HtmlNode.Block.Blockquote(collectBlocks(element.childNodes()))
            "ul" -> HtmlNode.Block.UnorderedList(parseListItems(element))
            "ol" -> {
                val start = element.attr("start").toIntOrNull() ?: 1
                HtmlNode.Block.OrderedList(parseListItems(element), start)
            }
            "pre" -> parsePre(element)
            "hr" -> HtmlNode.Block.HorizontalRule
            "img" -> parseImage(element)
            "picture" -> parsePicture(element)
            "video" -> parseVideo(element)
            "audio" -> parseAudio(element)
            "iframe" -> parseIframe(element)
            "figure" -> parseFigure(element)
            "table" -> parseTable(element)
            "details" -> parseDetails(element)
            "br" -> null
            else -> null
        }
    }

    private fun parseListItems(element: Element): List<HtmlNode.Block.ListItem> =
        element.children()
            .filter { it.tagName().equals("li", ignoreCase = true) }
            .map { li -> HtmlNode.Block.ListItem(collectBlocks(li.childNodes())) }

    private fun parsePre(element: Element): HtmlNode.Block.CodeBlock {
        val codeChild = element.children().firstOrNull { it.tagName().equals("code", ignoreCase = true) }
        val text = (codeChild ?: element).wholeText()
        val language = codeChild?.classNames()
            ?.firstOrNull { it.startsWith("language-") }
            ?.removePrefix("language-")
        return HtmlNode.Block.CodeBlock(text, language)
    }

    private fun parseImage(element: Element): HtmlNode.Block.Image {
        val src = LAZY_SRC_ATTRS
            .firstNotNullOfOrNull { attr -> element.attr(attr).takeIf { it.isNotBlank() } }
            .orEmpty()
        val srcsetCandidates = parseSrcset(
            LAZY_SRCSET_ATTRS
                .firstNotNullOfOrNull { attr -> element.attr(attr).takeIf { it.isNotBlank() } }
                .orEmpty()
        )
        return HtmlNode.Block.Image(
            src = src,
            alt = element.attr("alt").ifBlank { null },
            title = element.attr("title").ifBlank { null },
            width = element.attr("width").toIntOrNull(),
            height = element.attr("height").toIntOrNull(),
            sources = srcsetCandidates,
            sizes = element.attr("sizes").ifBlank { null },
        )
    }

    private fun parsePicture(element: Element): HtmlNode.Block.Image? {
        val img = element.children().firstOrNull { it.tagName().equals("img", ignoreCase = true) }
            ?: return null
        val base = parseImage(img)
        val sourceCandidates = element.children()
            .filter { it.tagName().equals("source", ignoreCase = true) }
            .flatMap { source ->
                parseSrcset(source.attr("srcset")).map { candidate ->
                    candidate.copy(
                        mediaType = source.attr("type").ifBlank { null },
                        media = source.attr("media").ifBlank { null },
                    )
                }
            }
        return base.copy(sources = sourceCandidates + base.sources)
    }

    private fun parseSrcset(raw: String): List<HtmlNode.Block.ImageSource> {
        if (raw.isBlank()) return emptyList()
        return raw.split(',').mapNotNull { entry ->
            val tokens = entry.trim().split(Regex("\\s+"))
            if (tokens.isEmpty() || tokens.first().isBlank()) return@mapNotNull null
            val url = tokens.first()
            val descriptor = tokens.getOrNull(1)
            val width = descriptor?.takeIf { it.endsWith("w") }?.dropLast(1)?.toIntOrNull()
            val density = descriptor?.takeIf { it.endsWith("x") }?.dropLast(1)?.toFloatOrNull()
            HtmlNode.Block.ImageSource(url = url, width = width, density = density)
        }
    }

    private fun parseVideo(element: Element): HtmlNode.Block.Video {
        val sources = element.select("source").map { source ->
            HtmlNode.Block.Video.Source(
                src = source.attr("src"),
                type = source.attr("type").ifBlank { null },
            )
        }
        return HtmlNode.Block.Video(
            src = element.attr("src").ifBlank { null },
            poster = element.attr("poster").ifBlank { null },
            sources = sources,
        )
    }

    private fun parseAudio(element: Element): HtmlNode.Block.Audio {
        val sources = element.select("source").map { source ->
            HtmlNode.Block.Video.Source(
                src = source.attr("src"),
                type = source.attr("type").ifBlank { null },
            )
        }
        return HtmlNode.Block.Audio(
            src = element.attr("src").ifBlank { null },
            sources = sources,
        )
    }

    private fun parseIframe(element: Element): HtmlNode.Block.Iframe? {
        val src = element.attr("src")
        if (src.isBlank()) return null
        return HtmlNode.Block.Iframe(src, element.attr("title").ifBlank { null })
    }

    private fun parseFigure(element: Element): HtmlNode.Block.Figure {
        val caption = element.children()
            .firstOrNull { it.tagName().equals("figcaption", ignoreCase = true) }
            ?.let { collectInline(it) }
        val children = element.children()
            .filterNot { it.tagName().equals("figcaption", ignoreCase = true) }
            .flatMap { collectBlocks(listOf(it)) }
        return HtmlNode.Block.Figure(children, caption)
    }

    private fun parseTable(element: Element): HtmlNode.Block.Table {
        val rows = element.select("tr").map { row ->
            val cells = row.children()
                .filter { it.tagName().equals("td", ignoreCase = true) || it.tagName().equals("th", ignoreCase = true) }
                .map { cell ->
                    HtmlNode.Block.Table.Cell(
                        children = collectBlocks(cell.childNodes()),
                        header = cell.tagName().equals("th", ignoreCase = true),
                    )
                }
            val isHeader = cells.isNotEmpty() && cells.all { it.header }
            HtmlNode.Block.Table.Row(cells, header = isHeader)
        }
        return HtmlNode.Block.Table(rows)
    }

    private fun parseDetails(element: Element): HtmlNode.Block.Details {
        val summary = element.children()
            .firstOrNull { it.tagName().equals("summary", ignoreCase = true) }
            ?.let { collectInline(it) }
            .orEmpty()
        val children = element.childNodes()
            .filter { it !is Element || !it.tagName().equals("summary", ignoreCase = true) }
        return HtmlNode.Block.Details(summary, collectBlocks(children))
    }

    private fun collectInline(element: Element): List<HtmlNode.Inline> =
        flattenInlines(element.childNodes(), InlineStyle.NONE).let(::trim)

    private fun parseInline(element: Element): List<HtmlNode.Inline> =
        flattenInlines(listOf(element), InlineStyle.NONE)

    private fun flattenInlines(nodes: List<Node>, style: InlineStyle): List<HtmlNode.Inline> {
        val out = mutableListOf<HtmlNode.Inline>()
        for (node in nodes) {
            when (node) {
                is TextNode -> {
                    val text = node.text()
                    if (text.isNotEmpty()) {
                        out += HtmlNode.Inline.Text(text, style)
                    }
                }
                is Element -> {
                    when (val tag = node.tagName().lowercase()) {
                        "br" -> out += HtmlNode.Inline.LineBreak
                        "a" -> out += HtmlNode.Inline.Link(
                            href = node.attr("href"),
                            children = flattenInlines(node.childNodes(), style),
                            title = node.attr("title").ifBlank { null },
                        )
                        "code" -> out += HtmlNode.Inline.Code(node.wholeText())
                        in SKIP_TAGS -> Unit
                        else -> {
                            val nestedStyle = INLINE_STYLE_TAGS[tag]?.let { style + it } ?: style
                            out += flattenInlines(node.childNodes(), nestedStyle)
                        }
                    }
                }
                is Comment -> Unit
                else -> Unit
            }
        }
        return out
    }

    private fun trim(inlines: List<HtmlNode.Inline>): List<HtmlNode.Inline> {
        if (inlines.isEmpty()) return inlines
        val list = inlines.toMutableList()
        while (list.isNotEmpty()) {
            val first = list.first()
            if (first is HtmlNode.Inline.Text && first.text.isBlank()) {
                list.removeAt(0)
            } else break
        }
        while (list.isNotEmpty()) {
            val last = list.last()
            if (last is HtmlNode.Inline.Text && last.text.isBlank()) {
                list.removeAt(list.size - 1)
            } else break
        }
        return list
    }

    private val BLOCK_TAGS = setOf(
        "p", "h1", "h2", "h3", "h4", "h5", "h6",
        "blockquote", "ul", "ol", "pre", "hr",
        "figure", "table", "details",
    )

    private val MEDIA_TAGS = setOf("img", "video", "audio", "iframe", "picture")

    private const val MEDIA_SELECTOR = "img,video,audio,iframe,picture"

    private val LAZY_SRC_ATTRS = listOf(
        "src", "data-src", "data-original", "data-lazy-src",
        "data-actual-src", "data-image-src", "data-hi-res-src",
    )

    private val LAZY_SRCSET_ATTRS = listOf(
        "srcset", "data-srcset", "data-lazy-srcset",
    )

    private val CONTAINER_TAGS = setOf(
        "div", "article", "section", "main", "aside",
        "header", "footer", "nav", "body", "html",
    )

    private val SKIP_TAGS = setOf("script", "style", "noscript", "head", "title", "meta", "link")

    private val INLINE_STYLE_TAGS = mapOf(
        "b" to InlineStyle.BOLD,
        "strong" to InlineStyle.BOLD,
        "i" to InlineStyle.ITALIC,
        "em" to InlineStyle.ITALIC,
        "cite" to InlineStyle.ITALIC,
        "u" to InlineStyle.UNDERLINE,
        "ins" to InlineStyle.UNDERLINE,
        "s" to InlineStyle.STRIKETHROUGH,
        "del" to InlineStyle.STRIKETHROUGH,
        "strike" to InlineStyle.STRIKETHROUGH,
        "sub" to InlineStyle.SUBSCRIPT,
        "sup" to InlineStyle.SUPERSCRIPT,
        "mark" to InlineStyle.MARK,
        "small" to InlineStyle.SMALL,
    )
}
