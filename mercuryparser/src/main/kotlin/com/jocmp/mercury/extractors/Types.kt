package com.jocmp.mercury.extractors

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

// A single selector entry. `attr` is non-null when the upstream tuple form
// `[selector, attribute]` is used to pull a value off an attribute. `compound`
// is non-empty for content selectors of the form `[['a', 'b']]` — all parts
// must match in the document, and the union of their elements is used.
// `transformValue` mirrors upstream's 3-tuple `[selector, attr, fn]` — the
// extracted attribute value is fed through `fn` (e.g. JSON-parsed for genius.com).
data class Selector(
    val css: String,
    val attr: String? = null,
    val compound: List<String> = emptyList(),
    val transformValue: ((String) -> String?)? = null,
)

// Mirrors upstream's transforms map values:
//   - string → rename matched elements to that tag
//   - function → return new tag name (or NoChange) and optionally mutate node
sealed interface Transform {
    data class Rename(val to: String) : Transform

    data class Run(val apply: (node: Selection, doc: Doc) -> TransformResult) : Transform
}

sealed interface TransformResult {
    data object NoChange : TransformResult

    data class Rename(val to: String) : TransformResult
}

// Field-level extraction options. Mirrors the shape of the JS object literal:
//   {
//     selectors: [...],
//     clean: [...],
//     transforms: { selector: ... },
//     defaultCleaner: false,
//     allowMultiple: true,
//   }
data class FieldSpec(
    val selectors: List<Selector>,
    val clean: List<String> = emptyList(),
    val transforms: Map<String, Transform> = emptyMap(),
    val defaultCleaner: Boolean = true,
    val allowMultiple: Boolean = false,
    // Constant string value (upstream allows e.g. `author: 'TMZ STAFF'`).
    // Bypasses selectors entirely when set.
    val literal: String? = null,
    // Per-field timezone and format hints for date_published. Mirror upstream's
    // `timezone: 'Asia/Tokyo'` / `format: 'YYYY年M月D日'` options.
    val timezone: String? = null,
    val format: String? = null,
)

// One custom extractor definition. Any field set to null falls through to
// the generic extractor at run time.
data class CustomExtractor(
    val domain: String,
    val supportedDomains: List<String> = emptyList(),
    val title: FieldSpec? = null,
    val author: FieldSpec? = null,
    val content: FieldSpec? = null,
    val datePublished: FieldSpec? = null,
    val dek: FieldSpec? = null,
    val excerpt: FieldSpec? = null,
    val leadImageUrl: FieldSpec? = null,
    val nextPageUrl: FieldSpec? = null,
)
