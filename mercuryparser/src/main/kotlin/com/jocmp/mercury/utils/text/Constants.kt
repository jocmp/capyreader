package com.jocmp.mercury.utils.text

// An expression that looks to try to find the page digit within a URL, if
// it exists.
// Matches:
//  page=1
//  pg=1
//  p=1
//  paging=12
//  pag=7
//  pagination/1
//  paging/88
//  pa/83
//  p/11
//
// Does not match:
//  pg=102
//  page:2
val PAGE_IN_HREF_RE: Regex =
    Regex(
        "(page|paging|(p(a|g|ag)?(e|enum|ewanted|ing|ination)))?(=|/)([0-9]{1,3})",
        RegexOption.IGNORE_CASE,
    )

val HAS_ALPHA_RE: Regex = Regex("[a-z]", RegexOption.IGNORE_CASE)

val IS_ALPHA_RE: Regex = Regex("^[a-z]+$", RegexOption.IGNORE_CASE)
val IS_DIGIT_RE: Regex = Regex("^[0-9]+$", RegexOption.IGNORE_CASE)

val ENCODING_RE: Regex = Regex("""charset=([\w-]+)\b""")
const val DEFAULT_ENCODING: String = "utf-8"
