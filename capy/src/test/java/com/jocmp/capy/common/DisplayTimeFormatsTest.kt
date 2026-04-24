package com.jocmp.capy.common

import java.time.LocalDateTime
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DisplayTimeFormatsTest {
    private val defaultLocale = Locale.getDefault()
    private val instant = LocalDateTime.of(2023, 12, 25, 9, 5)

    @AfterTest
    fun teardown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `localized reflects the default locale`() {
        val cases = listOf(
            Case(Locale.US, time = "9:05\u202FAM", shortDate = "Dec 25, 2023", longDate = "December 25, 2023"),
            Case(Locale.UK, time = "09:05", shortDate = "25 Dec 2023", longDate = "25 December 2023"),
            Case(Locale.FRANCE, time = "09:05", shortDate = "25 déc. 2023", longDate = "25 décembre 2023"),
            Case(Locale.GERMANY, time = "09:05", shortDate = "25.12.2023", longDate = "25. Dezember 2023"),
        )

        cases.forEach { case ->
            Locale.setDefault(case.locale)
            val formats = DisplayTimeFormats.localized()

            assertEquals(expected = case.time, actual = formats.time.format(instant), message = "time (${case.locale})")
            assertEquals(expected = case.shortDate, actual = formats.shortDate.format(instant), message = "shortDate (${case.locale})")
            assertEquals(expected = case.longDate, actual = formats.longDate.format(instant), message = "longDate (${case.locale})")
        }
    }

    @Test
    fun `localized rebuilds when the default locale changes`() {
        Locale.setDefault(Locale.FRANCE)
        val french = DisplayTimeFormats.localized()

        Locale.setDefault(Locale.GERMANY)
        val german = DisplayTimeFormats.localized()

        assertEquals(expected = "25 déc. 2023", actual = french.shortDate.format(instant))
        assertEquals(expected = "25.12.2023", actual = german.shortDate.format(instant))
    }

    private data class Case(
        val locale: Locale,
        val time: String,
        val shortDate: String,
        val longDate: String,
    )
}
