package com.capyreader.app.ui.articles.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class SkipCalculatorTest {
    @Test
    fun skipBack_fromMiddle() {
        val result = SkipCalculator.skipBack(60_000L)
        assertEquals(30_000L, result)
    }

    @Test
    fun skipBack_nearStart_clampsToZero() {
        val result = SkipCalculator.skipBack(15_000L)
        assertEquals(0L, result)
    }

    @Test
    fun skipBack_atStart_staysAtZero() {
        val result = SkipCalculator.skipBack(0L)
        assertEquals(0L, result)
    }

    @Test
    fun skipForward_fromMiddle() {
        val result = SkipCalculator.skipForward(60_000L, 120_000L)
        assertEquals(90_000L, result)
    }

    @Test
    fun skipForward_nearEnd_clampsToDuration() {
        val result = SkipCalculator.skipForward(100_000L, 120_000L)
        assertEquals(120_000L, result)
    }

    @Test
    fun skipForward_unknownDuration_addsSkipAmount() {
        val result = SkipCalculator.skipForward(60_000L, 0L)
        assertEquals(90_000L, result)
    }

    @Test
    fun skipForward_negativeDuration_addsSkipAmount() {
        val result = SkipCalculator.skipForward(60_000L, -1L)
        assertEquals(90_000L, result)
    }
}
