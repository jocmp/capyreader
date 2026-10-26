package com.jocmp.mercury.extractors.generic.content.scoring

import kotlin.test.Test
import kotlin.test.assertEquals

class ScoreCommasTest {
    @Test
    fun `returns 0 if text has no commas`() {
        assertEquals(0, scoreCommas("Foo bar"))
    }

    @Test
    fun `returns a point for every comma in the text`() {
        assertEquals(1, scoreCommas("Foo, bar"))
        assertEquals(2, scoreCommas("Foo, bar, baz"))
        assertEquals(3, scoreCommas("Foo, bar, baz, bat"))
    }
}
