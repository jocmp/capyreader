package com.jocmp.capy.common

import com.jocmp.capy.common.DateTimeFormatters.LONG_MONTH_DATE_TIME_FORMATTER
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

val formatters = listOf(
    DateTimeFormatter.ISO_ZONED_DATE_TIME,
    DateTimeFormatter.RFC_1123_DATE_TIME,
    LONG_MONTH_DATE_TIME_FORMATTER,
)

val String.toDateTime: ZonedDateTime?
    get() {
        val dateTime = formatters.firstNotNullOfOrNull { formatter ->
            parseOrNull(this, formatter)
        }

        return dateTime?.withZoneSameInstant(ZoneOffset.UTC)
    }

private fun parseOrNull(text: CharSequence, formatter: DateTimeFormatter): ZonedDateTime? {
    return try {
        ZonedDateTime.parse(text, formatter)
    } catch (_: DateTimeParseException) {
        null
    }
}

val Long.toDateTimeFromSeconds: ZonedDateTime
    get() {
        val instant = Instant.ofEpochMilli(this * 1000)

        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
    }

fun nowUTCInSeconds(): Long {
    return nowUTC().toEpochSecond()
}

fun nowUTC(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
