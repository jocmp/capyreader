package com.jocmp.mercury.cleaners

import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveSplitTitleTest {
    @Test
    fun `does nothing if title not splittable`() {
        val title = "This Is a Normal Title"
        assertEquals(title, resolveSplitTitle(title))
    }

    @Test
    fun `extracts titles from breadcrumb-like titles`() {
        val title = "The Best Gadgets on Earth : Bits : Blogs : NYTimes.com"
        assertEquals("The Best Gadgets on Earth ", resolveSplitTitle(title))
    }

    @Test
    fun `cleans domains from titles at the front`() {
        val title = "NYTimes - The Best Gadgets on Earth"
        val url = "https://www.nytimes.com/bits/blog/etc/"

        assertEquals("The Best Gadgets on Earth", resolveSplitTitle(title, url))
    }

    @Test
    fun `cleans domains from titles at the back`() {
        val title = "The Best Gadgets on Earth | NYTimes"
        val url = "https://www.nytimes.com/bits/blog/etc/"

        assertEquals("The Best Gadgets on Earth", resolveSplitTitle(title, url))
    }
}
