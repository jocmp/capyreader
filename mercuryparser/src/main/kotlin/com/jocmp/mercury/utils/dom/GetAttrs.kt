package com.jocmp.mercury.utils.dom

import org.jsoup.nodes.Element

fun getAttrs(node: Element): Map<String, String> = node.attributes().associate { it.key to it.value }
