package com.jocmp.basil.common

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

val String.toDateTime: OffsetDateTime?
    get() {
        return try {
            OffsetDateTime.parse(this).withOffsetSameInstant(ZoneOffset.UTC)
        } catch (_: DateTimeParseException) {
            null
        }
    }

val Long.toDateTimeFromSeconds: OffsetDateTime
    get() {
        val instant = Instant.ofEpochMilli(this * 1000)

        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
    }

fun nowUTCInSeconds(): Long {
    return nowUTC().toEpochSecond()
}

fun nowUTC(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
