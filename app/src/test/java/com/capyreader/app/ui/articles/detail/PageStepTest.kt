package com.capyreader.app.ui.articles.detail

import org.junit.Assert.assertEquals
import org.junit.Test

class PageStepTest {
    @Test
    fun `steps most of the space between the current bottom and next top edges`() {
        val step = pageStep(
            viewport = 1800f,
            leading = 250f,
            trailing = 50f,
        )

        assertEquals(1440f, step)
    }
}
