package com.jocmp.capy.common


import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.exp
import kotlin.test.assertEquals

class TimeHelpersTest {
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
    fun `published clamps future time`() {
        val now = TimeHelpers.nowUTC()

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
