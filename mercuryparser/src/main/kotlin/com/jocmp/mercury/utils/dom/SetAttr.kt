package com.jocmp.mercury.utils.dom

import org.jsoup.nodes.Element

fun setAttr(
    node: Element,
    attr: String,
    value: String,
): Element {
    node.attr(attr, value)
    return node
}
