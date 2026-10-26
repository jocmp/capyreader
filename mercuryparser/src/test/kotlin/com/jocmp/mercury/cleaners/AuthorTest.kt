package com.jocmp.mercury.cleaners

import kotlin.test.Test
import kotlin.test.assertEquals

class AuthorTest {
    @Test
    fun `removes the By from an author string`() {
        val author = cleanAuthor("By Bob Dylan")
        assertEquals("Bob Dylan", author)
    }

    @Test
    fun `trims trailing whitespace and line breaks`() {
        val text = """
          written by
          Bob Dylan
        """
        val author = cleanAuthor(text)
        assertEquals("Bob Dylan", author)
    }
}
