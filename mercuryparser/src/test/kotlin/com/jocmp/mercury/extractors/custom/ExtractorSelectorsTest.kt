package com.jocmp.mercury.extractors.custom

import com.jocmp.mercury.extractors.CustomExtractor
import com.jocmp.mercury.extractors.FieldSpec
import org.jsoup.select.QueryParser
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtractorSelectorsTest {
    @Test
    fun `every custom extractor selector parses`() {
        val invalid =
            AllExtractors.all.flatMap { extractor ->
                extractor.fieldSpecs()
                    .flatMap { spec -> spec.queries() }
                    .filter { query -> runCatching { QueryParser.parse(query) }.isFailure }
                    .map { query -> "${extractor.domain}: $query" }
            }

        assertEquals(emptyList(), invalid)
    }

    private fun CustomExtractor.fieldSpecs(): List<FieldSpec> =
        listOfNotNull(title, author, content, datePublished, dek, excerpt, leadImageUrl, nextPageUrl)

    private fun FieldSpec.queries(): List<String> =
        selectors.flatMap { it.compound + it.css }.filter { it.isNotBlank() } +
            clean +
            transforms.keys
}
