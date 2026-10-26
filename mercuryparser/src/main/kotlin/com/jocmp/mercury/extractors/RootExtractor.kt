package com.jocmp.mercury.extractors

import com.jocmp.mercury.ParseResult
import com.jocmp.mercury.cleaners.cleanAuthor
import com.jocmp.mercury.cleaners.cleanDatePublished
import com.jocmp.mercury.cleaners.cleanDek
import com.jocmp.mercury.cleaners.cleanLeadImageUrl
import com.jocmp.mercury.cleaners.cleanTitle
import com.jocmp.mercury.cleaners.extractCleanNode
import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.extractors.generic.GenericExtractor
import com.jocmp.mercury.extractors.generic.author.extractGenericAuthor
import com.jocmp.mercury.extractors.generic.datepublished.extractGenericDatePublished
import com.jocmp.mercury.extractors.generic.dek.extractGenericDek
import com.jocmp.mercury.extractors.generic.excerpt.extractGenericExcerpt
import com.jocmp.mercury.extractors.generic.leadimageurl.extractGenericLeadImageUrl
import com.jocmp.mercury.extractors.generic.nextpageurl.extractGenericNextPageUrl
import com.jocmp.mercury.extractors.generic.title.extractGenericTitle
import com.jocmp.mercury.extractors.generic.url.extractGenericUrl
import com.jocmp.mercury.extractors.generic.wordcount.extractGenericWordCount
import com.jocmp.mercury.utils.dom.convertNodeTo
import com.jocmp.mercury.utils.dom.makeLinksAbsolute
import com.jocmp.mercury.utils.text.getDirection
import java.time.Instant

// Remove elements by an array of selectors
internal fun cleanBySelectors(
    content: Selection,
    doc: Doc,
    spec: FieldSpec,
): Selection {
    if (spec.clean.isEmpty()) return content
    content.find(spec.clean.joinToString(",")).remove()
    return content
}

// Transform matching elements
internal fun transformElements(
    content: Selection,
    doc: Doc,
    spec: FieldSpec,
): Selection {
    if (spec.transforms.isEmpty()) return content

    for ((key, transform) in spec.transforms) {
        val matches = content.find(key)
        when (transform) {
            is Transform.Rename ->
                matches.each { _, node ->
                    convertNodeTo(doc.wrap(node), doc, transform.to)
                }
            is Transform.Run ->
                matches.each { _, node ->
                    val result = transform.apply(doc.wrap(node), doc)
                    if (result is TransformResult.Rename) {
                        convertNodeTo(doc.wrap(node), doc, result.to)
                    }
                }
        }
    }
    return content
}

private fun findMatchingSelector(
    doc: Doc,
    selectors: List<Selector>,
    extractHtml: Boolean,
    allowMultiple: Boolean,
): Selector? =
    selectors.find { selector ->
        when {
            selector.compound.isNotEmpty() ->
                // All parts must match — mirrors upstream's reduce(&&).
                selector.compound.all { doc(it).length > 0 }
            selector.attr != null -> {
                val match = doc(selector.css)
                if (extractHtml) {
                    match.length > 0
                } else {
                    (allowMultiple || match.length == 1) &&
                        match.attr(selector.attr)?.trim()?.isNotEmpty() == true
                }
            }
            else -> {
                val match = doc(selector.css)
                (allowMultiple || match.length == 1) && match.text().trim().isNotEmpty()
            }
        }
    }

/** Run a [FieldSpec] against [doc] and return the extracted value or null. */
internal fun selectField(
    doc: Doc,
    url: String,
    type: String,
    extractionOpts: FieldSpec?,
    extractHtml: Boolean = false,
    title: String = "",
    excerpt: String? = null,
): String? {
    if (extractionOpts == null) return null
    if (extractionOpts.literal != null) return extractionOpts.literal
    val overrideAllowMultiple = type == "lead_image_url" || extractionOpts.allowMultiple

    val matching =
        findMatchingSelector(doc, extractionOpts.selectors, extractHtml, overrideAllowMultiple)
            ?: return null

    fun transformAndClean(node: Selection): Selection {
        makeLinksAbsolute(node, doc, url)
        cleanBySelectors(node, doc, extractionOpts)
        transformElements(node, doc, extractionOpts)
        return node
    }

    if (extractHtml) {
        val content = doc(matching.css)
        // Wrap CLONES (not the live nodes) in a div so transformations can act
        // on the root without mutating the source document — otherwise later
        // field extractors (dek, leadImageUrl, etc.) lose access to descendants
        // of the original selector.
        val wrapper = doc.document.createElement("div")
        content.elements.forEach { wrapper.appendChild(it.clone()) }
        val wrapped = doc.wrap(wrapper)
        val transformed = transformAndClean(wrapped)

        if (type == "content") {
            extractCleanNode(
                transformed,
                doc,
                cleanConditionally = extractionOpts.defaultCleaner,
                title = title,
                url = url,
                defaultCleaner = extractionOpts.defaultCleaner,
            )
        }
        return transformed.outerHtml()
    }

    val match = doc(matching.css)
    val cleaned = transformAndClean(match)
    val attrValue: String? =
        if (matching.attr != null) {
            cleaned.attr(matching.attr)?.trim()
        } else {
            cleaned.text().trim().takeIf { it.isNotEmpty() }
        }
    val raw: String? =
        if (matching.transformValue != null && attrValue != null) {
            matching.transformValue.invoke(attrValue)?.trim()?.takeIf { it.isNotEmpty() }
        } else {
            attrValue
        }

    if (raw == null) return null

    // Allow custom extractor to skip default cleaner for this type; defaults to true.
    if (!extractionOpts.defaultCleaner) return raw
    return when (type) {
        "title" -> cleanTitle(raw, doc, url)
        "author" -> cleanAuthor(raw)
        "date_published" -> cleanDatePublished(raw, extractionOpts.timezone, extractionOpts.format)
        "lead_image_url" -> cleanLeadImageUrl(raw)
        "dek" -> cleanDek(raw, doc, excerpt)
        else -> raw
    }
}

private fun extractField(
    extractor: CustomExtractor,
    doc: Doc,
    url: String,
    html: String?,
    metaCache: List<String>,
    type: String,
    fallback: Boolean = true,
    extractHtml: Boolean = false,
    title: String = "",
    content: String? = null,
    excerpt: String? = null,
): String? {
    val spec =
        when (type) {
            "title" -> extractor.title
            "author" -> extractor.author
            "date_published" -> extractor.datePublished
            "content" -> extractor.content
            "dek" -> extractor.dek
            "excerpt" -> extractor.excerpt
            "lead_image_url" -> extractor.leadImageUrl
            "next_page_url" -> extractor.nextPageUrl
            else -> null
        }
    val result = selectField(doc, url, type, spec, extractHtml = extractHtml, title = title, excerpt = excerpt)
    if (result != null) return result
    if (!fallback) return null

    return when (type) {
        "title" -> extractGenericTitle(doc, url, metaCache)
        "author" -> extractGenericAuthor(doc, metaCache)
        "date_published" -> extractGenericDatePublished(doc, url, metaCache)
        "content" -> com.jocmp.mercury.extractors.generic.content.extractGenericContent(doc, html, title, url)
        "dek" -> extractGenericDek()
        "excerpt" -> extractGenericExcerpt(doc, content, metaCache)
        "lead_image_url" -> extractGenericLeadImageUrl(doc, content, metaCache, html)
        "next_page_url" -> extractGenericNextPageUrl(doc, url)
        else -> null
    }
}

object RootExtractor {
    fun extract(
        extractor: CustomExtractor?,
        doc: Doc,
        url: String,
        html: String?,
        metaCache: List<String>,
        fallback: Boolean = true,
    ): ParseResult {
        // No custom extractor → straight to generic.
        if (extractor == null) {
            return GenericExtractor.extract(html = html, docIn = doc, url = url)
        }

        val title = extractField(extractor, doc, url, html, metaCache, "title", fallback = fallback).orEmpty()
        val datePublished = extractField(extractor, doc, url, html, metaCache, "date_published", fallback = fallback)
        val author = extractField(extractor, doc, url, html, metaCache, "author", fallback = fallback)
        val nextPageUrl = extractField(extractor, doc, url, html, metaCache, "next_page_url", fallback = fallback)
        val content =
            extractField(
                extractor, doc, url, html, metaCache,
                "content", fallback = fallback,
                extractHtml = true, title = title,
            )
        val leadImageUrl =
            extractField(
                extractor,
                doc,
                url,
                html,
                metaCache,
                "lead_image_url",
                fallback = fallback,
                content = content,
            )
        val excerpt =
            extractField(
                extractor,
                doc,
                url,
                html,
                metaCache,
                "excerpt",
                fallback = fallback,
                content = content,
            )
        val dek =
            extractField(
                extractor, doc, url, html, metaCache,
                "dek", fallback = fallback, content = content, excerpt = excerpt,
            )
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
            domain = urlAndDomain.domain.orEmpty(),
            excerpt = excerpt,
            wordCount = wordCount,
            direction = direction,
        )
    }
}
