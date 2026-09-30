package com.capyreader.app.ui.articles.detail

import com.capyreader.app.ui.articles.reader.PageInsets
import org.junit.Assert.assertEquals
import org.junit.Test

class PageStepTest {
    @Test
    fun `steps the visible height minus the overlap`() {
        val step = pageStep(
            viewport = 1800f,
            insets = PageInsets(top = 150f, bottom = 250f),
            overlap = 200f,
        )

        assertEquals(1200f, step)
    }

    @Test
    fun `steps at least half the visible height on short screens`() {
        val step = pageStep(
            viewport = 600f,
            insets = PageInsets(top = 100f, bottom = 100f),
            overlap = 300f,
        )

        assertEquals(200f, step)
    }
}
