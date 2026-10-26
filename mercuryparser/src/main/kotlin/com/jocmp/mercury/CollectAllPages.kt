package com.jocmp.mercury

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.extractors.CustomExtractor
import com.jocmp.mercury.extractors.RootExtractor
import com.jocmp.mercury.extractors.generic.wordcount.extractGenericWordCount
import com.jocmp.mercury.resource.Resource
import java.net.URI

private const val MAX_PAGES_TO_FETCH = 26

internal suspend fun collectAllPages(
    initial: ParseResult,
    initialUrl: String,
    extractor: CustomExtractor?,
    options: ParseOptions,
): ParseResult {
    val seen = mutableSetOf(removeAnchor(initialUrl))
    var current = initial
    var nextUrl = initial.nextPageUrl
    var rendered = 1

    while (nextUrl != null && rendered < MAX_PAGES_TO_FETCH) {
        val canonical = removeAnchor(nextUrl)
        if (!seen.add(canonical)) break

        val pageResult = parsePage(nextUrl, extractor, options) ?: break

        rendered += 1
        current =
            current.copy(
                content = mergeContent(current.content, pageResult.content, rendered),
            )
        nextUrl = pageResult.nextPageUrl?.takeIf { removeAnchor(it) !in seen }
    }

    val mergedContent = current.content
    return current.copy(
        nextPageUrl = null,
        totalPages = rendered,
        renderedPages = rendered,
        wordCount = mergedContent?.let { extractGenericWordCount(it) } ?: current.wordCount,
    )
}

private suspend fun parsePage(
    url: String,
    extractor: CustomExtractor?,
    options: ParseOptions,
): ParseResult? {
    val parsedUrl = runCatching { URI.create(url) }.getOrNull() ?: return null

    val doc: Doc =
        try {
            Resource.create(
                url = url,
                parsedUrl = parsedUrl,
                headers = options.headers,
                client = options.httpClient,
            )
        } catch (_: Throwable) {
            return null
        }

    val html = doc.html()
    val metaCache = doc("meta[name]").elements.mapNotNull { it.attr("name").ifEmpty { null } }
    return RootExtractor.extract(extractor, doc, url, html, metaCache, fallback = options.fallback)
}

private fun mergeContent(
    previous: String?,
    next: String?,
    pageNumber: Int,
): String? {
    if (next.isNullOrBlank()) return previous
    if (previous.isNullOrBlank()) return next
    return "$previous<hr><h4>Page $pageNumber</h4>$next"
}

private fun removeAnchor(url: String): String = url.substringBefore('#').trimEnd('/')
