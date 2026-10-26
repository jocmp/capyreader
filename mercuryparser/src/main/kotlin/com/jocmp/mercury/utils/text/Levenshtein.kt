package com.jocmp.mercury.utils.text

import kotlin.math.max
import kotlin.math.min

// Levenshtein similarity ratio in the style of wuzzy.levenshtein(a, b):
// returns 1 - (editDistance / max(a.length, b.length)), clamped to [0, 1].
fun levenshteinRatio(
    a: String,
    b: String,
): Double {
    val maxLen = max(a.length, b.length)
    if (maxLen == 0) return 1.0
    val distance = levenshteinDistance(a, b)
    return 1.0 - distance.toDouble() / maxLen.toDouble()
}

private fun levenshteinDistance(
    a: String,
    b: String,
): Int {
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    val prev = IntArray(b.length + 1) { it }
    val curr = IntArray(b.length + 1)
    for (i in 1..a.length) {
        curr[0] = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            curr[j] = min(min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost)
        }
        for (j in 0..b.length) prev[j] = curr[j]
    }
    return curr[b.length]
}
