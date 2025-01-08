package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.TimeZone

@Composable
fun relativeTime(
    time: ZonedDateTime,
    currentTime: LocalDateTime,
): String {
    val local = time.withZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime()
    val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    val duration = Duration.between(local, currentTime)

    return when {
        duration.toHours() < 24 -> timeFormat.format(local)
        else -> dateFormat.format(local)
    }
}
