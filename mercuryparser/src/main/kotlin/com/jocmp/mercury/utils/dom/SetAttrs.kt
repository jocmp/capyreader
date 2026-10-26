package com.jocmp.mercury.utils.dom

import org.jsoup.nodes.Element

fun setAttrs(
    node: Element,
    attrs: Map<String, String>,
): Element {
    val existing = node.attributes().asList().map { it.key }
    existing.forEach { node.removeAttr(it) }
    attrs.forEach { (k, v) -> node.attr(k, v) }
    return node
}
