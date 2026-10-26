package com.jocmp.mercury.extractors.generic.nextpageurl

import com.jocmp.mercury.dsl.Doc

// TODO: port scoring/* for next-page detection.
// For now, return null so multi-page pagination is no-op.
@Suppress("UNUSED_PARAMETER")
fun extractGenericNextPageUrl(
    doc: Doc,
    url: String,
    parsedUrl: String? = null,
): String? = null
