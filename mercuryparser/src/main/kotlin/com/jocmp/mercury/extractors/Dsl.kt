package com.jocmp.mercury.extractors

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

@DslMarker
annotation class ExtractorDsl

@ExtractorDsl
class ExtractorBuilder(val domain: String) {
    var supportedDomains: List<String> = emptyList()
    private var title: FieldSpec? = null
    private var author: FieldSpec? = null
    private var content: FieldSpec? = null
    private var datePublished: FieldSpec? = null
    private var dek: FieldSpec? = null
    private var excerpt: FieldSpec? = null
    private var leadImageUrl: FieldSpec? = null
    private var nextPageUrl: FieldSpec? = null

    fun title(block: FieldBuilder.() -> Unit) {
        title = FieldBuilder().apply(block).build()
    }

    fun author(block: FieldBuilder.() -> Unit) {
        author = FieldBuilder().apply(block).build()
    }

    fun content(block: FieldBuilder.() -> Unit) {
        content = FieldBuilder().apply(block).build()
    }

    fun datePublished(block: FieldBuilder.() -> Unit) {
        datePublished = FieldBuilder().apply(block).build()
    }

    fun dek(block: FieldBuilder.() -> Unit) {
        dek = FieldBuilder().apply(block).build()
    }

    fun excerpt(block: FieldBuilder.() -> Unit) {
        excerpt = FieldBuilder().apply(block).build()
    }

    fun leadImageUrl(block: FieldBuilder.() -> Unit) {
        leadImageUrl = FieldBuilder().apply(block).build()
    }

    fun nextPageUrl(block: FieldBuilder.() -> Unit) {
        nextPageUrl = FieldBuilder().apply(block).build()
    }

    fun build(): CustomExtractor =
        CustomExtractor(
            domain = domain,
            supportedDomains = supportedDomains,
            title = title,
            author = author,
            content = content,
            datePublished = datePublished,
            dek = dek,
            excerpt = excerpt,
            leadImageUrl = leadImageUrl,
            nextPageUrl = nextPageUrl,
        )
}

@ExtractorDsl
class FieldBuilder {
    private val selectorList = mutableListOf<Selector>()
    private val cleanList = mutableListOf<String>()
    private val transformsMap = mutableMapOf<String, Transform>()
    private var literalValue: String? = null
    var defaultCleaner: Boolean = true
    var allowMultiple: Boolean = false
    var timezone: String? = null
    var format: String? = null

    // Mirrors upstream's constant-valued field (e.g. `author: 'TMZ STAFF'`).
    fun literal(value: String) {
        literalValue = value
    }

    // Plain CSS selectors.
    fun selectors(vararg css: String) {
        css.forEach { selectorList.add(Selector(it)) }
    }

    fun selector(css: String) {
        selectorList.add(Selector(css))
    }

    // Attribute extraction. Mirrors the upstream `[selector, attribute]` tuple.
    fun attr(
        css: String,
        attribute: String,
    ) {
        selectorList.add(Selector(css, attribute))
    }

    // Attribute extraction + post-transform. Mirrors the upstream 3-tuple
    // `[selector, attribute, fn]` (genius.com parses the attr as JSON).
    fun attr(
        css: String,
        attribute: String,
        transformValue: (String) -> String?,
    ) {
        selectorList.add(Selector(css, attribute, transformValue = transformValue))
    }

    // Compound selector — mirrors upstream `[['selectorA', 'selectorB']]` for
    // content. All parts must match in the document; their union (joined with
    // `,`) is used as the matching set. Order matters for upstream parity.
    fun compound(vararg parts: String) {
        require(parts.isNotEmpty()) { "compound() needs at least one selector" }
        selectorList.add(Selector(css = parts.joinToString(","), compound = parts.toList()))
    }

    fun clean(vararg css: String) {
        cleanList.addAll(css)
    }

    fun transform(
        selector: String,
        renameTo: String,
    ) {
        transformsMap[selector] = Transform.Rename(renameTo)
    }

    fun transform(
        selector: String,
        fn: (node: Selection, doc: Doc) -> TransformResult,
    ) {
        transformsMap[selector] = Transform.Run(fn)
    }

    fun build(): FieldSpec =
        FieldSpec(
            selectors = selectorList.toList(),
            clean = cleanList.toList(),
            transforms = transformsMap.toMap(),
            defaultCleaner = defaultCleaner,
            allowMultiple = allowMultiple,
            literal = literalValue,
            timezone = timezone,
            format = format,
        )
}

fun extractor(
    domain: String,
    block: ExtractorBuilder.() -> Unit,
): CustomExtractor = ExtractorBuilder(domain).apply(block).build()
