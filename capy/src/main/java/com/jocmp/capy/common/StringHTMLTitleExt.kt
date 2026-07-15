package com.jocmp.capy.common

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist

private val VOID_ELEMENTS = setOf(
    "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "source",
    "track", "wbr"
)

private val OPEN_TAG_REGEX = Regex("<([a-zA-Z][a-zA-Z0-9]*)(?:\\s[^>]*)?(?<!/)>")

/**
 * Feeds sometimes put literal angle brackets in titles (e.g. a title referencing an HTML
 * tag by name, delivered via CDATA). Jsoup's tag-stripping treats any tag-shaped token as
 * real markup and, when it's never closed, silently drops everything nested inside it. If
 * any tag in the title lacks a matching closing tag, treat the whole title as plain text
 * (only decoding entities) instead of risking that data loss.
 */
val String.strippedOfTitleHTML: String
    get() {
        val hasUnclosedTag = OPEN_TAG_REGEX.findAll(this).any { match ->
            val tagName = match.groupValues[1].lowercase()

            tagName !in VOID_ELEMENTS && !this.contains("</$tagName>", ignoreCase = true)
        }

        return if (hasUnclosedTag) {
            Parser.unescapeEntities(this, false)
        } else {
            Cleaner(Safelist.none()).clean(Jsoup.parse(this)).text()
        }
    }
