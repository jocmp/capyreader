package com.jocmp.mercury

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.extractors.Extractors
import com.jocmp.mercury.extractors.RootExtractor
import com.jocmp.mercury.resource.Resource
import com.jocmp.mercury.utils.validateUrl
import java.net.URI

object Mercury {
    suspend fun parse(
        url: String,
        options: ParseOptions = ParseOptions(),
    ): ParseResult {
        if (options.contentType == ContentType.MARKDOWN || options.contentType == ContentType.TEXT) {
            throw NotImplementedError("Only ContentType.HTML is currently supported")
        }

        val parsedUrl =
            try {
                URI.create(url)
            } catch (_: Throwable) {
                return invalidUrl(url)
            }

        if (!validateUrl(parsedUrl)) return invalidUrl(url)

        val doc: Doc =
            try {
                Resource.create(
                    url = url,
                    preparedResponse = options.html,
                    parsedUrl = parsedUrl,
                    headers = options.headers,
                    client = options.httpClient,
                )
            } catch (e: Throwable) {
                return ParseResult(url = url, error = true, message = e.message)
            }

        val html = options.html ?: doc.html()
        val metaCache = collectMetaNames(doc)
        val extractor = Extractors.get(url)
        val result = RootExtractor.extract(extractor, doc, url, html, metaCache, fallback = options.fallback)

        if (options.fetchAllPages && options.html == null && result.nextPageUrl != null) {
            return collectAllPages(result, url, extractor, options)
        }

        return result
    }
}

private fun invalidUrl(url: String): ParseResult =
    ParseResult(
        url = url,
        error = true,
        message = "The url parameter passed does not look like a valid URL. Please check your URL and try again.",
    )

private fun collectMetaNames(doc: Doc): List<String> = doc("meta[name]").elements.mapNotNull { it.attr("name").ifEmpty { null } }
