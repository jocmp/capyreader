package com.jocmp.mercury.cleaners

import com.jocmp.mercury.utils.text.levenshteinRatio
import java.net.URI

private fun extractBreadcrumbTitle(
    splitTitleIn: List<String>,
    text: String,
): String? {
    var splitTitle = splitTitleIn
    // This must be a very breadcrumbed title, like:
    // The Best Gadgets on Earth : Bits : Blogs : NYTimes.com
    // NYTimes - Blogs - Bits - The Best Gadgets on Earth
    if (splitTitle.size >= 6) {
        // Look to see if we can find a breadcrumb splitter that happens
        // more than once. If we can, we'll be able to better pull out
        // the title.
        val termCounts = mutableMapOf<String, Int>()
        for (titleText in splitTitle) {
            termCounts[titleText] = (termCounts[titleText] ?: 0) + 1
        }

        var maxTerm = ""
        var termCount = 0
        for ((key, count) in termCounts) {
            if (termCount < count) {
                maxTerm = key
                termCount = count
            }
        }

        // We found a splitter that was used more than once, so it
        // is probably the breadcrumber. Split our title on that instead.
        // Note: max_term should be <= 4 characters, so that " >> "
        // will match, but nothing longer than that.
        if (termCount >= 2 && maxTerm.length <= 4) {
            splitTitle = text.split(maxTerm)
        }

        val splitEnds = listOf(splitTitle.first(), splitTitle.last())
        val longestEnd = splitEnds.fold("") { acc, end -> if (acc.length > end.length) acc else end }

        if (longestEnd.length > 10) {
            return longestEnd
        }

        return text
    }

    return null
}

private fun cleanDomainFromTitle(
    splitTitle: List<String>,
    url: String,
): String? {
    // Search the ends of the title, looking for bits that fuzzy match
    // the URL too closely. If one is found, discard it and return the
    // rest.
    //
    // Strip out the big TLDs - it just makes the matching a bit more
    // accurate. Not the end of the world if it doesn't strip right.
    val host =
        try {
            URI.create(url).host ?: return null
        } catch (_: Throwable) {
            return null
        }
    val nakedDomain = host.replace(DOMAIN_ENDINGS_RE, "")

    val startSlug = splitTitle.first().lowercase().replace(" ", "")
    val startSlugRatio = levenshteinRatio(startSlug, nakedDomain)

    if (startSlugRatio > 0.4 && startSlug.length > 5) {
        return splitTitle.drop(2).joinToString("")
    }

    val endSlug = splitTitle.last().lowercase().replace(" ", "")
    val endSlugRatio = levenshteinRatio(endSlug, nakedDomain)

    if (endSlugRatio > 0.4 && endSlug.length >= 5) {
        return splitTitle.dropLast(2).joinToString("")
    }

    return null
}

// Given a title with separators in it (colons, dashes, etc),
// resolve whether any of the segments should be removed.
fun resolveSplitTitle(
    title: String,
    url: String = "",
): String {
    // Splits while preserving splitters, like:
    // ['The New New York', ' - ', 'The Washington Post']
    val splitTitle = splitWithDelimiters(title, TITLE_SPLITTERS_RE)
    if (splitTitle.size == 1) {
        return title
    }

    extractBreadcrumbTitle(splitTitle, title)?.let { return it }
    cleanDomainFromTitle(splitTitle, url)?.let { return it }

    // Fuzzy ratio didn't find anything, so this title is probably legit.
    // Just return it all.
    return title
}

// JS's `String.split(regex)` with capturing groups preserves the delimiters
// in the result. Kotlin's split drops them. Reimplement that behavior.
private fun splitWithDelimiters(
    input: String,
    re: Regex,
): List<String> {
    val result = mutableListOf<String>()
    var last = 0
    for (match in re.findAll(input)) {
        if (match.range.first > last) {
            result.add(input.substring(last, match.range.first))
        }
        result.add(match.value)
        last = match.range.last + 1
    }
    if (last < input.length) {
        result.add(input.substring(last))
    }
    if (result.isEmpty()) result.add(input)
    return result
}
