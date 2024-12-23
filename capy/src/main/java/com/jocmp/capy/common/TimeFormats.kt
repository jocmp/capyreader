package com.jocmp.capy.common

import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
 */
internal object TimeFormats {
    private const val RSS_1123_UK = "EEE, d MMM yyyy HH:mm:ss ZZZ"

    private val DATETIME_PATTERNS = listOf(
        "EEE, dd MMM yyyy HH:mm:ss z",
    )

    private val DATE_PATTERNS = listOf(
        "MMM dd, yyyy",
    )

    fun dateTimeFormatters() =
        listOf(
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ofPattern(RSS_1123_UK).withLocale(Locale.UK),
        ) + usPatterns(DATETIME_PATTERNS)

    fun dateFormatters() = usPatterns(DATE_PATTERNS)

    private fun usPatterns(list: List<String>) = list.map {
        DateTimeFormatter.ofPattern(it).withLocale(Locale.US)
    }
}
