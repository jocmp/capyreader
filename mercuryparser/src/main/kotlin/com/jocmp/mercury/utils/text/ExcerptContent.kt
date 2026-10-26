package com.jocmp.mercury.utils.text

fun excerptContent(
    content: String,
    words: Int = 10,
): String = content.trim().split(Regex("""\s+""")).take(words).joinToString(" ")
