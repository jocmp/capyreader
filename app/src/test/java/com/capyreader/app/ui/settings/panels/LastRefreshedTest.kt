package com.capyreader.app.ui.settings.panels

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class LastRefreshedTest {
    @Test
    fun zeroEpoch_returnsNever() {
        val result = LastRefreshed.from(epochSeconds = 0L)

        assertTrue(result is LastRefreshed.Never)
    }

    @Test
    fun todayTimestamp_returnsToday() {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val epochSeconds = now.toEpochSecond()

        val result = LastRefreshed.from(
            epochSeconds = epochSeconds,
            today = now.toLocalDate(),
        )

        assertTrue(result is LastRefreshed.Today)
    }

    @Test
    fun pastTimestamp_returnsPastDate() {
        val today = LocalDate.of(2026, 3, 1)
        val pastDate = ZonedDateTime.of(2026, 2, 15, 10, 30, 0, 0, ZoneId.systemDefault())
        val epochSeconds = pastDate.toEpochSecond()

        val result = LastRefreshed.from(
            epochSeconds = epochSeconds,
            today = today,
        )

        assertTrue(result is LastRefreshed.Past)
    }

    @Test
    fun todayResult_includesFormattedTime() {
        val dateTime = ZonedDateTime.of(2026, 3, 1, 14, 30, 0, 0, ZoneId.systemDefault())
        val epochSeconds = dateTime.toEpochSecond()

        val result = LastRefreshed.from(
            epochSeconds = epochSeconds,
            today = dateTime.toLocalDate(),
        )

        val time = (result as LastRefreshed.Today).time
        assertTrue(time.isNotBlank())
    }

    @Test
    fun pastDateResult_includesFormattedDateAndTime() {
        val today = LocalDate.of(2026, 3, 1)
        val pastDate = ZonedDateTime.of(2026, 2, 15, 10, 30, 0, 0, ZoneId.systemDefault())
        val epochSeconds = pastDate.toEpochSecond()

        val result = LastRefreshed.from(
            epochSeconds = epochSeconds,
            today = today,
        ) as LastRefreshed.Past

        assertTrue(result.date.isNotBlank())
        assertTrue(result.time.isNotBlank())
    }
}
