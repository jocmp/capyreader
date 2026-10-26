package com.jocmp.mercury.utils.text

fun pageNumFromUrl(url: String): Int? {
    val matches = PAGE_IN_HREF_RE.find(url) ?: return null
    val pageNum = matches.groupValues[6].toIntOrNull() ?: return null

    // Return pageNum < 100, otherwise
    // return null
    return if (pageNum < 100) pageNum else null
}
