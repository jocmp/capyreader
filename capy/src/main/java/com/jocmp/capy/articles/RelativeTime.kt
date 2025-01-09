package com.jocmp.capy.articles

import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.TimeZone

fun relativeTime(
    time: ZonedDateTime,
    currentTime: LocalDateTime,
): String {
    val local = time.withZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime()
    val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val currentDay = currentTime.toLocalDate().atStartOfDay()

    return when {
        local.isAfter(currentDay) -> timeFormat.format(local)
        else -> dateFormat.format(local)
    }
}
