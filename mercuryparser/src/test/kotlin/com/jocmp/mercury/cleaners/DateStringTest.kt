package com.jocmp.mercury.cleaners

import kotlin.test.Test
import kotlin.test.assertEquals

class DateStringTest {
    @Test
    fun `removes published text from a datePublished string`() {
        val datePublished = cleanDateString("published: 1/1/2020")
        assertEquals("1/1/2020", datePublished)
    }

    @Test
    fun `trims whitespace`() {
        val datePublished = cleanDateString("    1/1/2020     ")
        assertEquals("1/1/2020", datePublished)
    }

    @Test
    fun `puts a space between a time and am-pm`() {
        // The JS date parser is forgiving, but
        // it needs am/pm separated from a time
        val date1 = cleanDateString("1/1/2020 8:30am")
        assertEquals("1/1/2020 8:30 am", date1)

        val date2 = cleanDateString("8:30PM 1/1/2020")
        assertEquals("8:30 PM   1/1/2020", date2)
    }

    @Test
    fun `cleans the dots from am or pm`() {
        // The JS date parser is forgiving, but
        // it needs a.m./p.m. without dots
        val date1 = cleanDateString("1/1/2020 8:30 a.m.")
        assertEquals("1/1/2020 8:30 am", date1)
    }

    @Test
    fun `can handle some tough timestamps`() {
        val date1 = cleanDateString("This page was last modified on 15 April 2016, at 10:59.")
        assertEquals("15 Apr 2016 10:59", date1)
    }

    @Test
    fun `massages the T out`() {
        val date1 = cleanDateString("2016-11-22T08:57-500")
        assertEquals("2016 11 22 08:57 -500", date1)
    }
}
