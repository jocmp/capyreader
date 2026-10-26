package com.jocmp.mercury.cleaners

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DatePublishedTest {
    @Test
    fun `returns null if date is invalid`() {
        assertNull(cleanDatePublished("blargh"))
    }

    @Test
    fun `handles timezones`() {
        val datePublished =
            cleanDatePublished(
                "November 29, 2016: 8:18 AM ET",
                timezone = "America/New_York",
            )
        assertEquals("2016-11-29T13:18:00Z", datePublished)
    }

    @Test
    fun `can handle 'now'`() {
        val today = LocalDate.now(ZoneOffset.UTC)
        val date = cleanDatePublished("now")
        val parsed = ZonedDateTime.parse(date)
        assertEquals(today, parsed.toLocalDate())
    }

    @Test
    fun `can handle 'just now'`() {
        val today = LocalDate.now(ZoneOffset.UTC)
        val date = cleanDatePublished("just now")
        val parsed = ZonedDateTime.parse(date)
        assertEquals(today, parsed.toLocalDate())
    }

    @Test
    fun `can handle 'right now'`() {
        val today = LocalDate.now(ZoneOffset.UTC)
        val date = cleanDatePublished("right now")
        val parsed = ZonedDateTime.parse(date)
        assertEquals(today, parsed.toLocalDate())
    }

    @Test
    fun `can handle '1 hour ago'`() {
        val expected = ZonedDateTime.now(ZoneOffset.UTC).minusHours(1).toLocalDate()
        val date = cleanDatePublished("1 hour ago")
        val parsed = ZonedDateTime.parse(date)
        assertEquals(expected, parsed.toLocalDate())
    }

    @Test
    fun `can handle '5 days ago'`() {
        val expected = ZonedDateTime.now(ZoneOffset.UTC).minusDays(5).toLocalDate()
        val date = cleanDatePublished("5 days ago")
        val parsed = ZonedDateTime.parse(date)
        assertEquals(expected, parsed.toLocalDate())
    }

    @Test
    fun `can handle '10 months ago'`() {
        val expected = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(10).toLocalDate()
        val date = cleanDatePublished("10 months ago")
        val parsed = ZonedDateTime.parse(date)
        assertEquals(expected, parsed.toLocalDate())
    }
}
