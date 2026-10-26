package com.jocmp.mercury.utils

private const val DEFAULT_ELLIPSE: String = "…"
private val DEFAULT_CHARS: List<Char> = listOf(' ', '-')

private fun ellipsizeAtBreak(
    str: String,
    max: Int,
    ellipse: String,
    chars: List<Char>,
    truncate: Boolean,
): String {
    if (str.length <= max) return str

    val maxLen = max - ellipse.length
    var end = maxLen
    var breakpointFound = false

    for (i in 0..maxLen) {
        val ch = str[i]
        if (chars.contains(ch)) {
            end = i
            breakpointFound = true
        }
    }

    if (!truncate && !breakpointFound) return ""

    return str.substring(0, end) + ellipse
}

fun ellipsize(
    str: String?,
    max: Int,
    ellipse: String = DEFAULT_ELLIPSE,
    chars: List<Char> = DEFAULT_CHARS,
    truncate: Boolean = true,
): String {
    if (str.isNullOrEmpty()) return ""
    if (max == 0) return ""
    return ellipsizeAtBreak(str, max, ellipse, chars, truncate)
}
