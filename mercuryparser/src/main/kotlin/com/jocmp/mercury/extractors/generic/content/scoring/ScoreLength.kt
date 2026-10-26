package com.jocmp.mercury.extractors.generic.content.scoring

import kotlin.math.max
import kotlin.math.min

private val idkRe: Regex = Regex("^(p|pre)$", RegexOption.IGNORE_CASE)

fun scoreLength(
    textLength: Int,
    tagName: String = "p",
): Double {
    val chunks = textLength / 50.0

    if (chunks > 0) {
        val lengthBonus: Double

        // No idea why p or pre are being tamped down here
        // but just following the source for now
        // Not even sure why tagName is included here,
        // since this is only being called from the context
        // of scoreParagraph
        lengthBonus =
            if (idkRe.containsMatchIn(tagName)) {
                chunks - 2
            } else {
                chunks - 1.25
            }

        return min(max(lengthBonus, 0.0), 3.0)
    }

    return 0.0
}
