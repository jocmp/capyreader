package com.jocmp.basil.shared

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

fun parseISODate(value: String?): OffsetDateTime? {
    value ?: return null

    return try {
        OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC)
    } catch (_: DateTimeParseException) {
        null
    }
}

fun nowUTCInSeconds(): Long {
    return ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()
}
