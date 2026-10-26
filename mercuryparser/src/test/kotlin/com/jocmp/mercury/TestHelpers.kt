package com.jocmp.mercury

import kotlin.test.assertEquals

fun clean(s: String): String =
    s.trim()
        .replace(Regex("""\r?\n|\r"""), "")
        .replace(Regex("""\s+"""), " ")

fun assertClean(
    a: String,
    b: String,
) {
    assertEquals(clean(b), clean(a))
}
