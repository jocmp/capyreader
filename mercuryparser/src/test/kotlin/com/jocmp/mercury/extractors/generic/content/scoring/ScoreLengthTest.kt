package com.jocmp.mercury.extractors.generic.content.scoring

import kotlin.test.Test
import kotlin.test.assertEquals

class ScoreLengthTest {
    @Test
    fun `returns 0 if length less than 50 chars`() {
        assertEquals(0.0, scoreLength(30))
    }

    @Test
    fun `returns varying scores but maxes out at 3`() {
        assertEquals(1.0, scoreLength(150))
        assertEquals(1.98, scoreLength(199))
        assertEquals(2.0, scoreLength(200))
        assertEquals(3.0, scoreLength(250))
        assertEquals(3.0, scoreLength(500))
        assertEquals(3.0, scoreLength(1500))
    }
}
