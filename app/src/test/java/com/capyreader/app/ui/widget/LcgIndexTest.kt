package com.capyreader.app.ui.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class LcgIndexTest {
    private val baseTime = LocalDateTime.of(2026, 4, 6, 22, 13, 34)
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
    private val interval = 30 * 60 * 1000L

    @Test
    fun resultIsWithinBounds() {
        val size = 10
        (0L..100L).forEach { slot ->
            val index = lcgIndex(currentTimeMillis = baseTime + slot * interval, size = size)
            assertTrue("index $index out of bounds", index in 0 until size)
        }
    }

    @Test
    fun sameSlotReturnsSameIndex() {
        val a = lcgIndex(currentTimeMillis = baseTime, size = 10)
        val b = lcgIndex(currentTimeMillis = baseTime + 60_000L, size = 10)
        assertEquals(a, b)
    }

    @Test
    fun differentSlotsProduceDifferentIndices() {
        val size = 10
        val indices = (0L..9L).map { slot ->
            lcgIndex(currentTimeMillis = baseTime + slot * interval, size = size)
        }.toSet()
        assertTrue("Expected multiple distinct indices, got $indices", indices.size > 1)
    }
}
