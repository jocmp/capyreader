package com.jocmp.capy.common

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

val String.toDateTime: ZonedDateTime?
    get() {
        val dateTime = isoFormat(this) ?: rfc1123Format(this)

        return dateTime?.withZoneSameInstant(ZoneOffset.UTC)
    }

private fun isoFormat(dateTime: String): ZonedDateTime? {
    return try {
        ZonedDateTime.parse(dateTime)
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun rfc1123Format(dateTime: String): ZonedDateTime? {
    return try {
        ZonedDateTime.parse(dateTime, DateTimeFormatter.RFC_1123_DATE_TIME)
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
