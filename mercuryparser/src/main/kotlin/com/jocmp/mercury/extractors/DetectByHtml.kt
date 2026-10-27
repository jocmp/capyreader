package com.jocmp.mercury.extractors

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.extractors.custom.substack.com.SubstackComExtractor

private val detectors: List<Pair<String, CustomExtractor>> =
    listOf(
        "link[href^=\"https://substackcdn.com\"]" to SubstackComExtractor,
    )

/** Match platforms that serve custom domains by markers in their HTML. */
fun detectByHtml(doc: Doc): CustomExtractor? =
    detectors
        .firstOrNull { (selector, _) -> doc(selector).length > 0 }
        ?.second
