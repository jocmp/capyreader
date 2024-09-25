package com.jocmp.capy.common

import com.jocmp.capy.common.TimeFormats.dateFormatters
import com.jocmp.capy.common.TimeFormats.dateTimeFormatters
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object TimeHelpers {
    fun nowUTC(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

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
}

val String.toDateTime: ZonedDateTime?
    get() {
        val dateTime = parse(this)

        return dateTime?.withZoneSameInstant(ZoneOffset.UTC)
    }

private fun parse(text: String): ZonedDateTime? {
    val dateTime = dateTimeFormatters().firstNotNullOfOrNull { formatter ->
        parseDateTime(text, formatter)
    }

    if (dateTime != null) {
        return dateTime
    }

    return dateFormatters().firstNotNullOfOrNull { formatter ->
        parseDate(text, formatter)
    }
}

private fun parseDateTime(text: CharSequence, formatter: DateTimeFormatter): ZonedDateTime? {
    return try {
        ZonedDateTime.parse(text, formatter)
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun parseDate(text: CharSequence, formatter: DateTimeFormatter): ZonedDateTime? {
    return try {
        LocalDate.parse(text, formatter).atStartOfDay(ZoneOffset.UTC)
    } catch (_: DateTimeParseException) {
        null
    }
}

val Long.toDateTimeFromSeconds: ZonedDateTime
    get() {
        val instant = Instant.ofEpochMilli(this * 1000)

        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
    }
