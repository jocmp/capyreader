package com.jocmp.mercury.utils.text

private val NORMALIZE_RE: Regex = Regex("""\s{2,}(?![^<>]*</(pre|code|textarea)>)""")

fun normalizeSpaces(text: String): String = NORMALIZE_RE.replace(text, " ").trim()
