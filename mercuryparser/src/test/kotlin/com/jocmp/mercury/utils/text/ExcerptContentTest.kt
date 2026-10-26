package com.jocmp.mercury.utils.text

import kotlin.test.Test
import kotlin.test.assertEquals

class ExcerptContentTest {
    @Test
    fun `extracts the requested number of words from content`() {
        val content = " One  two three four five six, seven eight, nine, ten."

        val three = excerptContent(content, 3)
        assertEquals("One two three", three)

        val ten = excerptContent(content, 10)
        assertEquals(content.trim().replace(Regex("""\s+"""), " "), ten)
    }
}
