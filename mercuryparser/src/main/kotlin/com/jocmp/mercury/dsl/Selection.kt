package com.jocmp.mercury.dsl

import org.jsoup.nodes.Comment
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements

class Selection internal constructor(
    val doc: Doc,
    val elements: Elements,
) {
    val length: Int get() = elements.size

    fun first(): Selection = if (elements.isEmpty()) this else doc.wrap(elements.first()!!)

    fun get(index: Int): Element? = elements.getOrNull(index)

    // Cheerio's .text() concatenates descendant text nodes with no separator (unlike
    // Jsoup's text() which inserts spaces between block elements). Faithful port matters
    // for linkDensity/text-length comparisons. CRLF in the source HTML is normalized
    // to LF so downstream "collapse 2+ whitespace" rules don't treat the two-char
    // line ending as needing collapse.
    fun text(): String =
        buildString {
            elements.forEach { el ->
                collectText(el, this)
            }
        }.replace("\r\n", "\n")

    private fun collectText(
        el: Element,
        out: StringBuilder,
    ) {
        el.childNodes().forEach { node ->
            when (node) {
                is org.jsoup.nodes.TextNode -> out.append(node.wholeText)
                is Element -> collectText(node, out)
                else -> Unit
            }
        }
    }

    fun html(): String = elements.html()

    fun outerHtml(): String = elements.joinToString("") { it.outerHtml() }

    fun attr(name: String): String? = elements.firstOrNull()?.attr(name)?.ifEmpty { null }

    fun attr(
        name: String,
        value: String,
    ): Selection = apply { elements.attr(name, value) }

    fun removeAttr(name: String): Selection = apply { elements.removeAttr(name) }

    fun hasAttr(name: String): Boolean = elements.firstOrNull()?.hasAttr(name) == true

    fun children(): Selection = doc.wrap(elements.flatMap { it.children() }.let(::Elements))

    fun find(selector: String): Selection = doc.wrap(elements.select(selector))

    fun parent(): Selection = doc.wrap(elements.mapNotNull { it.parent() }.let(::Elements))

    /** Find the nearest ancestor matching [selector], like cheerio's `$node.parent(selector)`. */
    fun parent(selector: String): Selection {
        val matches = Elements()
        elements.forEach { el ->
            var p = el.parent()
            while (p != null) {
                if (p.`is`(selector)) {
                    matches.add(p)
                    break
                }
                p = p.parent()
            }
        }
        return doc.wrap(matches)
    }

    fun addClass(classes: String): Selection =
        apply {
            classes.split(Regex("""\s+""")).filter { it.isNotEmpty() }.forEach { c ->
                elements.forEach { it.addClass(c) }
            }
        }

    /** Insert [html] right after the current element(s), like cheerio's `$node.after(html)`. */
    fun after(html: String): Selection =
        apply {
            elements.forEach { el ->
                val parsed = org.jsoup.parser.Parser.parseBodyFragment(html, el.ownerDocument()?.baseUri() ?: "")
                val children = parsed.body().children().toList()
                var prev: Element = el
                children.forEach { child ->
                    prev.after(child)
                    prev = child
                }
            }
        }

    fun replaceWith(html: String): Selection =
        apply {
            elements.forEach { el ->
                val parsed = org.jsoup.parser.Parser.parseBodyFragment(html, el.ownerDocument()?.baseUri() ?: "")
                val children = parsed.body().children().toList()
                if (children.isNotEmpty()) {
                    el.replaceWith(children.first())
                    var prev = children.first()
                    for (i in 1 until children.size) {
                        prev.after(children[i])
                        prev = children[i]
                    }
                }
            }
        }

    fun remove(): Selection = apply { elements.remove() }

    fun each(block: (index: Int, element: Element) -> Unit): Selection {
        elements.forEachIndexed { index, el -> block(index, el) }
        return this
    }

    fun contents(): List<Node> = elements.flatMap { it.childNodes() }

    fun removeComments(): Selection =
        apply {
            elements.forEach { el ->
                el.childNodes().filterIsInstance<Comment>().forEach { it.remove() }
                el.select("*").forEach { child ->
                    child.childNodes().filterIsInstance<Comment>().forEach { it.remove() }
                }
            }
        }
}
