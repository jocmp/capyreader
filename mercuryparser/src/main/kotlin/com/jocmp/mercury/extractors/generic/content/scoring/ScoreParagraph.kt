package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Selection

// Score a paragraph using various methods. Things like number of
// commas, etc. Higher is better.
fun scoreParagraph(node: Selection): Double {
    var score = 1.0
    val text = node.text().trim()
    val textLength = text.length

    // If this paragraph is less than 25 characters, don't count it.
    if (textLength < 25) {
        return 0.0
    }

    // Add points for any commas within this paragraph
    score += scoreCommas(text).toDouble()

    // For every 50 characters in this paragraph, add another point. Up
    // to 3 points.
    score += scoreLength(textLength)

    // Articles can end with short paragraphs when people are being clever
    // but they can also end with short paragraphs setting up lists of junk
    // that we strip. This negative tweaks junk setup paragraphs just below
    // the cutoff threshold.
    if (text.takeLast(1) == ":") {
        score -= 1
    }

    return score
}
