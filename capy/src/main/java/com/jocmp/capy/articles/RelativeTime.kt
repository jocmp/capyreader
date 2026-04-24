package com.jocmp.capy.articles

import com.jocmp.capy.common.DisplayTimeFormats
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.TimeZone

fun relativeTime(
    time: ZonedDateTime,
    currentTime: LocalDateTime,
    formats: DisplayTimeFormats,
): String {
    val local = time.withZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime()
    val currentDay = currentTime.toLocalDate().atStartOfDay()

    return if (local.isAfter(currentDay)) {
        formats.time.format(local)
    } else {
        formats.shortDate.format(local)
    }
}
