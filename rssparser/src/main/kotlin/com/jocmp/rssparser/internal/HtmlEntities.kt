package com.jocmp.rssparser.internal

private val NUMERIC_ENTITY_REGEX = Regex("&#(x[0-9a-fA-F]+|[0-9]+);")

/**
 * Decodes only numeric HTML entities (e.g., &#x27; &#39;) while leaving
 * named entities (e.g., &lt; &amp;) intact.
 *
 * This is useful for RSS titles where we want to decode apostrophes and quotes
 * but preserve escaped HTML characters that should remain as literal text.
 */
internal fun decodeNumericEntities(text: String): String {
    return NUMERIC_ENTITY_REGEX.replace(text) { matchResult ->
        val code = matchResult.groupValues[1]
        val codePoint = if (code.startsWith("x", ignoreCase = true)) {
            code.substring(1).toIntOrNull(16)
        } else {
            code.toIntOrNull()
        }
        codePoint?.let { String(Character.toChars(it)) } ?: matchResult.value
    }
}
