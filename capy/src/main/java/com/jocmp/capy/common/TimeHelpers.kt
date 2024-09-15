package com.jocmp.capy.common

import com.jocmp.capy.common.DateTimeFormatters.LONG_MONTH_DATE_TIME_FORMATTER
import com.jocmp.capy.common.DateTimeFormatters.ZULU_DATE_TIME_FORMATTER
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

val formatters = listOf(
    DateTimeFormatter.ISO_ZONED_DATE_TIME,
    DateTimeFormatter.RFC_1123_DATE_TIME,
    LONG_MONTH_DATE_TIME_FORMATTER,
    ZULU_DATE_TIME_FORMATTER,
)

/**
 * Normalize publish time to avoid recording future times
 *
 * https://github.com/feedbin/feedbin/blob/757bce1b63f4c78e9ca700c277a55300b7ef735f/app/models/entry.rb#L345-L351
 */
fun published(timestamp: String?, fallback: ZonedDateTime): ZonedDateTime {
    val parsed = timestamp?.toDateTime

    return if (parsed == null || parsed > nowUTC()) {
        fallback
    } else {
        parsed
    }
}

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
