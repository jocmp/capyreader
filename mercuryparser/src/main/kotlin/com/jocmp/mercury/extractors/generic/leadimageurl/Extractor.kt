package com.jocmp.mercury.extractors.generic.leadimageurl

import com.jocmp.mercury.cleaners.cleanLeadImageUrl
import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.utils.dom.extractFromMeta

// Given a resource, try to find the lead image URL from within
// it. Like content and next page extraction, uses a scoring system
// to determine what the most likely image may be. Short circuits
// on really probable things like og:image meta tags.
//
// Potential signals to still take advantage of:
//   * domain
//   * weird aspect ratio
fun extractGenericLeadImageUrl(
    doc: Doc,
    content: String?,
    metaCache: List<String>,
    html: String?,
): String? {
    var cleanUrl: String?

    if (doc("head").length == 0 && html != null) {
        doc("*").first().elements.firstOrNull()?.prepend(html)
    }

    // Check to see if we have a matching meta tag that we can make use of.
    // Moving this higher because common practice is now to use large
    // images on things like Open Graph or Twitter cards.
    // images usually have for things like Open Graph.
    val imageUrl = extractFromMeta(doc, LEAD_IMAGE_URL_META_TAGS, metaCache, cleanTags = false)

    if (imageUrl != null) {
        cleanUrl = cleanLeadImageUrl(imageUrl)
        if (cleanUrl != null) return cleanUrl
    }

    // Next, try to find the "best" image via the content.
    // We'd rather not have to fetch each image and check dimensions,
    // so try to do some analysis and determine them instead.
    if (content != null) {
        val contentDoc = Doc.load(content, isDocument = false)
        val imgs = contentDoc("img").elements.toList()
        val imgScores = mutableMapOf<String, Int>()

        imgs.forEachIndexed { index, raw ->
            val img = contentDoc.wrap(raw)
            val src = img.attr("src") ?: return@forEachIndexed

            var score = scoreImageUrl(src)
            score += scoreAttr(img)
            score += scoreByParents(img)
            score += scoreBySibling(img)
            score += scoreByDimensions(img)
            score += scoreByPosition(imgs, index)

            imgScores[src] = score
        }

        val top = imgScores.maxByOrNull { it.value }
        if (top != null && top.value > 0) {
            cleanUrl = cleanLeadImageUrl(top.key)
            if (cleanUrl != null) return cleanUrl
        }
    }

    // If nothing else worked, check to see if there are any really
    // probable nodes in the doc, like <link rel="image_src" />.
    for (selector in LEAD_IMAGE_URL_SELECTORS) {
        val node = doc(selector).first()
        val src = node.attr("src")
        if (src != null) {
            cleanUrl = cleanLeadImageUrl(src)
            if (cleanUrl != null) return cleanUrl
        }

        val href = node.attr("href")
        if (href != null) {
            cleanUrl = cleanLeadImageUrl(href)
            if (cleanUrl != null) return cleanUrl
        }

        val value = node.attr("value")
        if (value != null) {
            cleanUrl = cleanLeadImageUrl(value)
            if (cleanUrl != null) return cleanUrl
        }
    }

    return null
}
