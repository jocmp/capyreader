package com.jocmp.basil.common


import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals

class TimeHelpersTest {
    @Test
    fun `parseISODate parses an offset ISO timestamp to UTC`() {
        val result = parseISODate("2023-12-25T09:00:00-05:00")

        val expected = OffsetDateTime.of(
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
    fun `parseISODate discards non-ISO formatted strings`() {
        val result = parseISODate("Mon, 25 Dec 2023 17:18:03 +0000")

        assertEquals(expected = null, actual = result)
    }

    @Test
    fun `parseISODate discards null values`() {
        val result = parseISODate(null)

        assertEquals(expected = null, actual = result)
    }
}
