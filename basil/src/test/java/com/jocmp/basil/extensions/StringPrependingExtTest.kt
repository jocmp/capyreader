package com.jocmp.basil.extensions

import org.junit.Test
import kotlin.math.exp
import kotlin.test.assertEquals

class StringPrependingExtTest {
    @Test
    fun prepending() {
        val str = "Hello Moto".prepending(tabCount = 2)
        val expected = "    Hello Moto"

        assertEquals(expected = expected, actual = str)
    }

    @Test
    fun repeatTab() {
        assertEquals(expected = "  ", actual = repeatTab(tabCount = 1))
        assertEquals(expected = "    ", actual = repeatTab(tabCount = 2))
    }
}
