package com.capyreader.app.ui.articles.detail

import org.junit.Assert.assertEquals
import org.junit.Test

class PageStepTest {
    @Test
    fun `steps from the current bottom edge to the next top edge less the overlap`() {
        val step = pageStep(
            viewport = 1800f,
            leading = 250f,
            trailing = 50f,
            overlap = 200f,
        )

        assertEquals(1300f, step)
    }

    @Test
    fun `steps at least half the visible height on short screens`() {
        val step = pageStep(
            viewport = 600f,
            leading = 100f,
            trailing = 100f,
            overlap = 300f,
        )

        assertEquals(200f, step)
    }
}
