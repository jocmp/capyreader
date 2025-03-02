package com.jocmp.capy.common


import com.jocmp.capy.common.TimeHelpers.nowUTC
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.assertEquals

class TimeHelpersTest {
    private val defaultLocale = Locale.getDefault()

    @AfterTest
    fun teardown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `parseISODate parses an offset ISO timestamp to UTC`() {
        val result = "2023-12-25T09:00:00-05:00".toDateTime

        val expected = ZonedDateTime.of(
            2023,
            12,
            25,
            14,
            0,
            0,
            0,
            ZoneOffset.UTC
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `parseISODate parses RFC1123 timestamps`() {
        val result = "Mon, 25 Dec 2023 17:18:03 +0000".toDateTime

        val expected = ZonedDateTime.of(
            2023,
            12,
            25,
            17,
            18,
            3,
            0,
            ZoneOffset.UTC
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `RFC1123 with 4 letter dates`() {
        val result = "Thu, 05 Sept 2024 15:26:54 +0200".toDateTime

        val expected = ZonedDateTime.of(
            2024,
            9,
            5,
            13,
            26,
            54,
            0,
            ZoneOffset.UTC
        )

        assertEquals(expected = expected, actual = result)
    }


    @Test
    fun `parseISODate discards null values`() {
        val result = "".toDateTime

        assertEquals(expected = null, actual = result)
    }

    @Test
    fun `published handles Z offset in ISO 8601 format`() {
        val timestamp = "2025-02-21T16:00:00Z"

        val result = TimeHelpers.published(timestamp, fallback = nowUTC())

        val expected = ZonedDateTime.of(
            2025,
            2,
            21,
            16,
            0,
            0,
            0,
            ZoneOffset.UTC
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `published clamps future time`() {
        val now = nowUTC()

        val futureTime = now.plusHours(1).format(DateTimeFormatter.RFC_1123_DATE_TIME)

        val result = TimeHelpers.published(futureTime, fallback = now)

        assertEquals(expected = now, actual = result)
    }

    @Test
    fun `RFC1123 with Z offset`() {
        val result = "Fri, 30 Aug 2024 05:23:12 Z".toDateTime

        val expected = ZonedDateTime.of(
            2024,
            8,
            30,
            5,
            23,
            12,
            0,
            ZoneOffset.UTC
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `RFC1123 with timezone abbreviation`() {
        val result = "Sun, 22 Dec 2024 08:18:56 EDT".toDateTime

        val expected = ZonedDateTime.of(
            2024,
            12,
            22,
            13,
            18,
            56,
            0,
            ZoneOffset.UTC
        )

        assertEquals(expected = expected, actual = result)
    }


    @Test
    fun `RFC1123 with timezone abbreviation non-US locale`() {
        Locale.setDefault(Locale("en", "AU"))

        val result = "Sun, 22 Dec 2024 08:18:56 EDT".toDateTime

        val expected = ZonedDateTime.of(
            2024,
            12,
            22,
            13,
            18,
            56,
            0,
            ZoneOffset.UTC
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `RFC1123 with timezone abbreviation non-English locale`() {
        Locale.setDefault(Locale("es", "US"))

        val result = "Sun, 22 Dec 2024 08:18:56 EDT".toDateTime

        val expected = ZonedDateTime.of(
            2024,
            12,
            22,
            13,
            18,
            56,
            0,
            ZoneOffset.UTC
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `Date only`() {
        val result = "Sep 20, 2024".toDateTime

        val expected = ZonedDateTime.of(
            2024,
            9,
            20,
            0,
            0,
            0,
            0,
            ZoneOffset.UTC
        )

        assertEquals(expected = expected, actual = result)
    }
}
