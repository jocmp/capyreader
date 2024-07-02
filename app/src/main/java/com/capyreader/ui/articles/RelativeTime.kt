<<<<<<<< HEAD:app/src/main/java/com/capyreader/ui/components/RelativeTime.kt
package com.capyreader.ui.components
========
package com.jocmp.capyreader.ui.articles
>>>>>>>> 07507dc (wip):app/src/main/java/com/capyreader/ui/articles/RelativeTime.kt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.R
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
    val format = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

    val duration = Duration.between(local, currentTime)

    return when {
        duration.toMinutes() < 1 -> stringResource(R.string.relative_time_just_now)
        duration.toMinutes() < 60 -> stringResource(R.string.relative_time_minutes, duration.toMinutes())
        duration.toHours() < 24 -> stringResource(R.string.relative_time_hours, duration.toHours())
        duration.toDays() < 7 -> stringResource(R.string.relative_time_days, duration.toDays())
        else -> format.format(local)
    }
}
