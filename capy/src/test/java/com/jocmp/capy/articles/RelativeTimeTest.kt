package com.jocmp.capy.articles

import com.jocmp.capy.common.DisplayTimeFormats
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RelativeTimeTest {
    private val defaultZone = TimeZone.getDefault()
    private val defaultLocale = Locale.getDefault()

    private val formats = DisplayTimeFormats(
        time = DateTimeFormatter.ofPattern("h:mm a", Locale.US),
        shortDate = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US),
        longDate = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US),
    )

    @BeforeTest
    fun setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
    }

    @AfterTest
    fun teardown() {
        TimeZone.setDefault(defaultZone)
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `when the article date is on the current day`() {
        val time = ZonedDateTime.parse("2023-12-25T09:00:00-00:00")
        val currentTime = time.toLocalDateTime()

        val result = relativeTime(time = time, currentTime = currentTime, formats = formats)

        assertEquals(expected = "9:00 AM", actual = result)
    }

    @Test
    fun `when the article date is on the current day at midnight`() {
        val time = ZonedDateTime.parse("2023-12-25T09:00:00-00:00")
        val currentTime = time.toLocalDateTime()

        val result = relativeTime(time = time, currentTime = currentTime, formats = formats)

        assertEquals(expected = "9:00 AM", actual = result)
    }

    @Test
    fun `honors a 24-hour time formatter`() {
        val time = ZonedDateTime.parse("2023-12-25T09:00:00-00:00")
        val currentTime = time.toLocalDateTime()
        val twentyFourHour = formats.copy(time = DateTimeFormatter.ofPattern("HH:mm"))

        val result = relativeTime(time = time, currentTime = currentTime, formats = twentyFourHour)

        assertEquals(expected = "09:00", actual = result)
    }

    @Test
    fun `when the article date is before the current day`() {
        val time = ZonedDateTime.parse("2023-12-24T23:59:00-00:00")
        val currentTime = ZonedDateTime.parse("2023-12-25T09:00:00-00:00").toLocalDateTime()

        val result = relativeTime(time = time, currentTime = currentTime, formats = formats)

        assertEquals(expected = "Dec 24, 2023", actual = result)
    }
}
