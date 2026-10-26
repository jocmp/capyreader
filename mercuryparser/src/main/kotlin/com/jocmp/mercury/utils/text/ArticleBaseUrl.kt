package com.jocmp.mercury.utils.text

import java.net.URI

private fun isGoodSegment(
    segment: String,
    index: Int,
    firstSegmentHasLetters: Boolean,
): Boolean {
    var goodSegment = true

    // If this is purely a number, and it's the first or second
    // url_segment, it's probably a page number. Remove it.
    if (index < 2 && IS_DIGIT_RE.matches(segment) && segment.length < 3) {
        goodSegment = true
    }

    // If this is the first url_segment and it's just "index",
    // remove it
    if (index == 0 && segment.lowercase() == "index") {
        goodSegment = false
    }

    // If our first or second url_segment is smaller than 3 characters,
    // and the first url_segment had no alphas, remove it.
    if (index < 2 && segment.length < 3 && !firstSegmentHasLetters) {
        goodSegment = false
    }

    return goodSegment
}

// Take a URL, and return the article base of said URL. That is, no
// pagination data exists in it. Useful for comparing to other links
// that might have pagination data within them.
fun articleBaseUrl(
    url: String,
    parsed: URI? = null,
): String {
    val parsedUrl = parsed ?: URI.create(url)
    val protocol = parsedUrl.scheme
    val host = parsedUrl.host
    val path = parsedUrl.rawPath ?: ""

    var firstSegmentHasLetters = false
    val cleanedSegments = mutableListOf<String>()
    val reversed = path.split("/").reversed()
    reversed.forEachIndexed { index, rawSegment ->
        var segment = rawSegment

        // Split off and save anything that looks like a file type.
        if (segment.contains(".")) {
            val parts = segment.split(".", limit = 2)
            val possibleSegment = parts[0]
            val fileExt = parts[1]
            if (IS_ALPHA_RE.matches(fileExt)) {
                segment = possibleSegment
            }
        }

        // If our first or second segment has anything looking like a page
        // number, remove it.
        if (PAGE_IN_HREF_RE.containsMatchIn(segment) && index < 2) {
            segment = PAGE_IN_HREF_RE.replace(segment, "")
        }

        // If we're on the first segment, check to see if we have any
        // characters in it. The first segment is actually the last bit of
        // the URL, and this will be helpful to determine if we're on a URL
        // segment that looks like "/2/" for example.
        if (index == 0) {
            firstSegmentHasLetters = HAS_ALPHA_RE.containsMatchIn(segment)
        }

        // If it's not marked for deletion, push it to cleaned_segments.
        if (isGoodSegment(segment, index, firstSegmentHasLetters)) {
            cleanedSegments.add(segment)
        }
    }

    return "$protocol://$host${cleanedSegments.reversed().joinToString("/")}"
}
