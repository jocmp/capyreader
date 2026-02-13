package com.jocmp.capy.articles

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ReadingTimeTest {
    @Test
    fun `returns null for null content`() {
        assertNull(ReadingTime.calculate(null))
    }

    @Test
    fun `returns null for blank content`() {
        assertNull(ReadingTime.calculate(""))
        assertNull(ReadingTime.calculate("   "))
    }

    @Test
    fun `returns null for HTML with no text`() {
        assertNull(ReadingTime.calculate("<div></div>"))
    }

    @Test
    fun `calculates reading time for English content`() {
        // 500 chars / 500 cpm = 1 min
        val content = "<p>${"a".repeat(500)}</p>"
        assertEquals(1L, ReadingTime.calculate(content))
    }

    @Test
    fun `rounds up for partial minutes`() {
        // 501 chars / 500 cpm = 1.002 -> 2 min
        val content = "<p>${"a".repeat(501)}</p>"
        assertEquals(2L, ReadingTime.calculate(content))
    }

    @Test
    fun `calculates reading time for longer content`() {
        // 2500 chars / 500 cpm = 5 min
        val content = "<p>${"word ".repeat(500)}</p>"
        val result = ReadingTime.calculate(content)!!
        assertTrue(result > 0)
    }

    @Test
    fun `calculates reading time for CJK content`() {
        // 265 CJK chars / 265 cpm = 1 min
        val content = "<p>${"\u4e00".repeat(265)}</p>"
        assertEquals(1L, ReadingTime.calculate(content))
    }

    @Test
    fun `uses CJK rate when majority is CJK`() {
        // 530 CJK chars / 265 cpm = 2 min
        val content = "<p>${"\u4e00".repeat(530)}</p>"
        assertEquals(2L, ReadingTime.calculate(content))
    }

    @Test
    fun `isCJK detects Chinese text`() {
        assertTrue(ReadingTime.isCJK("\u4e00\u4e01\u4e02"))
    }

    @Test
    fun `isCJK detects Japanese text`() {
        assertTrue(ReadingTime.isCJK("\u3042\u3044\u3046")) // hiragana
    }

    @Test
    fun `isCJK detects Korean text`() {
        assertTrue(ReadingTime.isCJK("\uAC00\uAC01\uAC02")) // hangul
    }

    @Test
    fun `isCJK returns false for Latin text`() {
        assertFalse(ReadingTime.isCJK("Hello world"))
    }

    @Test
    fun `strips HTML tags before counting`() {
        val content = "<div><h1>Title</h1><p>Body text</p></div>"
        val result = ReadingTime.calculate(content)!!
        assertTrue(result >= 1L)
    }
}
