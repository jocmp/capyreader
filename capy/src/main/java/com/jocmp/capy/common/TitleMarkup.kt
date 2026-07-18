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
 * Strips embedded HTML from a title (some feeds put real markup, e.g. `<i>`, directly in
 * the title). Some feeds also put literal tag-shaped text in titles that was never meant as
 * markup (e.g. a title literally naming the `<dl>` HTML tag). Jsoup can't tell those apart:
 * it opens a real `<dl>` element that swallows everything after it, which the tag stripper
 * then deletes. If any tag-shaped token in the title has no matching closing tag, treat the
 * whole title as plain text and only decode entities, instead of risking that data loss.
 */
fun String.stripTitleMarkup(): String {
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
