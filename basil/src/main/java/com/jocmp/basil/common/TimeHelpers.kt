package com.jocmp.basil.common

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

val String.toDateTime: ZonedDateTime?
    get() {
        return try {
            ZonedDateTime.parse(this).withZoneSameInstant(ZoneOffset.UTC)
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
