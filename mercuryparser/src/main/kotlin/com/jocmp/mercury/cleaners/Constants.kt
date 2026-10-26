package com.jocmp.mercury.cleaners

// CLEAN AUTHOR CONSTANTS
val CLEAN_AUTHOR_RE: Regex = Regex("""^\s*(posted |written )?by\s*:?\s*(.*)""", RegexOption.IGNORE_CASE)

// CLEAN DEK CONSTANTS
val TEXT_LINK_RE: Regex = Regex("http(s)?://", RegexOption.IGNORE_CASE)

// An ordered list of meta tag names that denote likely article deks.
// From most distinct to least distinct.
//
// NOTE: There are currently no meta tags that seem to provide the right
// content consistenty enough. Two options were:
//  - og:description
//  - dc.description
// However, these tags often have SEO-specific junk in them that's not
// header-worthy like a dek is. Excerpt material at best.
val DEK_META_TAGS: List<String> = emptyList()

// An ordered list of Selectors to find likely article deks. From
// most explicit to least explicit.
//
// Should be more restrictive than not, as a failed dek can be pretty
// detrimental to the aesthetics of an article.
val DEK_SELECTORS: List<String> = listOf(".entry-summary")

// CLEAN DATE PUBLISHED CONSTANTS
val MS_DATE_STRING: Regex = Regex("""^\d{13}$""", RegexOption.IGNORE_CASE)
val SEC_DATE_STRING: Regex = Regex("""^\d{10}$""", RegexOption.IGNORE_CASE)
val CLEAN_DATE_STRING_RE: Regex = Regex("""^\s*published\s*:?\s*(.*)""", RegexOption.IGNORE_CASE)
val TIME_MERIDIAN_SPACE_RE: Regex = Regex("""(.*\d)(am|pm)(.*)""", RegexOption.IGNORE_CASE)
val TIME_MERIDIAN_DOTS_RE: Regex = Regex("""\.m\.""", RegexOption.IGNORE_CASE)
val TIME_NOW_STRING: Regex = Regex("""^\s*(just|right)?\s*now\s*""", RegexOption.IGNORE_CASE)

private val timeUnits =
    listOf(
        "seconds?",
        "minutes?",
        "hours?",
        "days?",
        "weeks?",
        "months?",
        "years?",
    )
private val allTimeUnits = timeUnits.joinToString("|")
val TIME_AGO_STRING: Regex = Regex("""(\d+)\s+($allTimeUnits)\s+ago""", RegexOption.IGNORE_CASE)

private val months =
    listOf(
        "jan", "feb", "mar", "apr", "may", "jun",
        "jul", "aug", "sep", "oct", "nov", "dec",
    )
private val allMonths = months.joinToString("|")
private const val TIMESTAMP_1 = "[0-9]{1,2}:[0-9]{2,2}( ?[ap].?m.?)?"
private const val TIMESTAMP_2 = "[0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}"
private const val TIMESTAMP_3 = "-[0-9]{3,4}$"
val SPLIT_DATE_STRING: Regex =
    Regex(
        "($TIMESTAMP_1)|($TIMESTAMP_2)|($TIMESTAMP_3)|([0-9]{1,4})|($allMonths)",
        setOf(RegexOption.IGNORE_CASE),
    )

// 2016-11-22T08:57-500, 2016-12-22T19:27:41+00:00, 2016-12-22T19:27:41Z
// Check if datetime string has an offset at the end
val TIME_WITH_OFFSET_RE: Regex = Regex("""([+-]\d{2}:?\d{2}|Z)$""")

// CLEAN TITLE CONSTANTS
// A regular expression that will match separating characters on a
// title, that usually denote breadcrumbs or something similar.
val TITLE_SPLITTERS_RE: Regex = Regex("""(: | - | \| )""")

val DOMAIN_ENDINGS_RE: Regex = Regex("""\.com$|\.net$|\.org$|\.co\.uk$""")
