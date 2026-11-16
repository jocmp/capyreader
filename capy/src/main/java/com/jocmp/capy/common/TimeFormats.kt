package com.jocmp.capy.common

import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
 */
internal object TimeFormats {
    private const val RFC_822 = "[EEE, ]d MMM yy HH:mm[:ss][ z][ Z]"

    // The RFC_1123_DATE_TIME in the standard library doesn't fully conform to spec,
    // make up for it with this.
    private const val RFC_1123 = "[EEE, ]d MMM yyyy HH:mm[:ss][ z][ Z]"

    private val DATETIME_PATTERNS = listOf(
        RFC_822,
        RFC_1123,
    )

    private val DATE_PATTERNS = listOf(
        "MMM dd, yyyy",
        "yyyy-MM-dd",
    )

    fun dateTimeFormatters() =
        listOf(
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.RFC_1123_DATE_TIME,
        ) + usPatterns(DATETIME_PATTERNS) + ukPatterns(DATETIME_PATTERNS)

    fun dateFormatters() = usPatterns(DATE_PATTERNS)

    private fun usPatterns(list: List<String>) = list.map {
        DateTimeFormatter.ofPattern(it, Locale.US)
    }

    private fun ukPatterns(list: List<String>) = list.map {
        DateTimeFormatter.ofPattern(it, Locale.UK)
    }
}
