package com.jocmp.basil.common

import java.time.Instant
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

val String.toDateTime: OffsetDateTime?
    get() {
        return try {
            OffsetDateTime.parse(this).withOffsetSameInstant(ZoneOffset.UTC)
        } catch (_: DateTimeParseException) {
            null
        }
    }

val Long.toDateTime: OffsetDateTime
    get() {
        val instant = Instant.ofEpochMilli(this)

        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
    }

fun nowUTCInSeconds(): Long {
    return OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond()
}
