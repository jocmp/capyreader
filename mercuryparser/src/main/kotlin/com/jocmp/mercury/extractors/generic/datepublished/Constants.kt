package com.jocmp.mercury.extractors.generic.datepublished

// An ordered list of meta tag names that denote
// likely date published dates. All attributes
// should be lowercase for faster case-insensitive matching.
// From most distinct to least distinct.
val DATE_PUBLISHED_META_TAGS: List<String> =
    listOf(
        "article:published_time",
        "displaydate",
        "dc.date",
        "dc.date.issued",
        "rbpubdate",
        "publish_date",
        "pub_date",
        "pagedate",
        "pubdate",
        "revision_date",
        "doc_date",
        "date_created",
        "content_create_date",
        "lastmodified",
        "created",
        "date",
    )

// An ordered list of XPath Selectors to find
// likely date published dates. From most explicit
// to least explicit.
val DATE_PUBLISHED_SELECTORS: List<String> =
    listOf(
        ".hentry .dtstamp.published",
        ".hentry .published",
        ".hentry .dtstamp.updated",
        ".hentry .updated",
        ".single .published",
        ".meta .published",
        ".meta .postDate",
        ".entry-date",
        ".byline .date",
        ".postmetadata .date",
        ".article_datetime",
        ".date-header",
        ".story-date",
        ".dateStamp",
        "#story .datetime",
        ".dateline",
        ".pubdate",
    )

// An ordered list of compiled regular expressions to find likely date
// published dates from the URL. These should always have the first
// reference be a date string that is parseable by dateutil.parser.parse
private const val ABBREV_MONTHS_STR: String = "(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)"
val DATE_PUBLISHED_URL_RES: List<Regex> =
    listOf(
        Regex("""/(20\d{2}/\d{2}/\d{2})/""", RegexOption.IGNORE_CASE),
        Regex("""(20\d{2}-[01]\d-[0-3]\d)""", RegexOption.IGNORE_CASE),
        Regex("""/(20\d{2}/$ABBREV_MONTHS_STR/[0-3]\d)/""", RegexOption.IGNORE_CASE),
    )
