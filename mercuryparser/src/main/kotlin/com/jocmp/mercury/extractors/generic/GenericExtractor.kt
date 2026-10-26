package com.jocmp.mercury.extractors.generic

import com.jocmp.mercury.ParseResult
import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.extractors.generic.author.extractGenericAuthor
import com.jocmp.mercury.extractors.generic.content.extractGenericContent
import com.jocmp.mercury.extractors.generic.datepublished.extractGenericDatePublished
import com.jocmp.mercury.extractors.generic.dek.extractGenericDek
import com.jocmp.mercury.extractors.generic.excerpt.extractGenericExcerpt
import com.jocmp.mercury.extractors.generic.leadimageurl.extractGenericLeadImageUrl
import com.jocmp.mercury.extractors.generic.nextpageurl.extractGenericNextPageUrl
import com.jocmp.mercury.extractors.generic.title.extractGenericTitle
import com.jocmp.mercury.extractors.generic.url.extractGenericUrl
import com.jocmp.mercury.extractors.generic.wordcount.extractGenericWordCount
import com.jocmp.mercury.utils.text.getDirection
import java.time.Instant

object GenericExtractor {
    // This extractor is the default for all domains
    const val DOMAIN: String = "*"

    fun extract(
        html: String?,
        docIn: Doc? = null,
        url: String,
    ): ParseResult {
        val doc =
            docIn ?: html?.let { Doc.load(it) } ?: return ParseResult(
                url = url,
                error = true,
                message = "No html or document provided",
            )
        val metaCache = collectMetaNames(doc)

        val title = extractGenericTitle(doc, url, metaCache)
        val datePublished = extractGenericDatePublished(doc, url, metaCache)
        val author = extractGenericAuthor(doc, metaCache)
        val content = extractGenericContent(doc, html, title, url)
        val leadImageUrl = extractGenericLeadImageUrl(doc, content, metaCache, html)
        val dek = extractGenericDek()
        val nextPageUrl = extractGenericNextPageUrl(doc, url)
        val excerpt = extractGenericExcerpt(doc, content, metaCache)
        val wordCount = content?.let { extractGenericWordCount(it) } ?: 0
        val direction = getDirection(title)
        val urlAndDomain = extractGenericUrl(doc, url, metaCache)

        return ParseResult(
            title = title,
            author = author,
            datePublished = datePublished?.let { runCatching { Instant.parse(it) }.getOrNull() },
            dek = dek,
            leadImageUrl = leadImageUrl,
            content = content,
            nextPageUrl = nextPageUrl,
            url = urlAndDomain.url,
            domain = urlAndDomain.domain ?: "",
            excerpt = excerpt,
            wordCount = wordCount,
            direction = direction,
        )
    }
}

private fun collectMetaNames(doc: Doc): List<String> = doc("meta[name]").elements.mapNotNull { it.attr("name").ifEmpty { null } }
