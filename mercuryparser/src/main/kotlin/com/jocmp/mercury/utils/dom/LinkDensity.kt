package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Selection

fun textLength(text: String): Int = text.trim().replace(Regex("""\s+"""), " ").length

// Determines what percentage of the text
// in a node is link text
// Takes a node, returns a float
fun linkDensity(node: Selection): Double {
    val totalTextLength = textLength(node.text())

    val linkText = node.find("a").text()
    val linkLength = textLength(linkText)

    if (totalTextLength > 0) {
        return linkLength.toDouble() / totalTextLength.toDouble()
    }
    if (totalTextLength == 0 && linkLength > 0) {
        return 1.0
    }

    return 0.0
}
