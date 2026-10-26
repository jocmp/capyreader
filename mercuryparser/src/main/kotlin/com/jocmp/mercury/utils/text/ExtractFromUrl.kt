package com.jocmp.mercury.utils.text

// Given a node type to search for, and a list of regular expressions,
// look to see if this extraction can be found in the URL. Expects
// that each expression in r_list will return group(1) as the proper
// string to be cleaned.
// Only used for date_published currently.
fun extractFromUrl(
    url: String,
    regexList: List<Regex>,
): String? {
    val matchRe = regexList.find { it.containsMatchIn(url) } ?: return null
    return matchRe.find(url)?.groupValues?.getOrNull(1)
}
