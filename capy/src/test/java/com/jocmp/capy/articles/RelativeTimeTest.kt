package com.jocmp.capy.articles

import java.time.ZonedDateTime
import java.util.Locale
import java.util.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RelativeTimeTest {
    private val defaultZone = TimeZone.getDefault()
    private val defaultLocale = Locale.getDefault()

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

        val result = relativeTime(time = time, currentTime = currentTime)

        assertEquals(expected = "9:00 AM", actual = result)
    }

    @Test
    fun `same day with a different locale`() {
        Locale.setDefault(Locale("fr", "FR"))
        val time = ZonedDateTime.parse("2023-12-25T09:00:00-00:00")
        val currentTime = time.toLocalDateTime()

        val result = relativeTime(time = time, currentTime = currentTime)

        assertEquals(expected = "09:00", actual = result)
    }

    @Test
    fun `when the article date is before the current day`() {
        val time = ZonedDateTime.parse("2023-12-24T23:59:00-00:00")
        val currentTime = ZonedDateTime.parse("2023-12-25T09:00:00-00:00").toLocalDateTime()

        val result = relativeTime(time = time, currentTime = currentTime)

        assertEquals(expected = "Dec 24, 2023", actual = result)
    }
}
