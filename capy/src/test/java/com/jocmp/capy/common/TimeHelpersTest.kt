package com.jocmp.capy.common


import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime
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
    fun `parseISODate parses RFC822 timestamps`() {
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
    fun `parseISODate discards null values`() {
        val result = "".toDateTime

        assertEquals(expected = null, actual = result)
    }
}
