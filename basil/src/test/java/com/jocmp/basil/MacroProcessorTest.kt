package com.jocmp.basil

import org.junit.Test
import kotlin.test.assertEquals

class MacroProcessorTest {
    private val substitutions = mapOf(
        "one" to "1",
        "two" to "2"
    )

    @Test
    fun `it substitutes key-values`() {
        val substitutions = mapOf(
            "one" to "1",
            "two" to "2"
        )

        val template = "foo [[one]] bar [[two]] baz"
        val expected = "foo 1 bar 2 baz"

        val processor = MacroProcessor(template = template, substitutions = substitutions)

        assertEquals(expected = expected, actual = processor.renderedText)
    }

    @Test
    fun `it substitutes key-values when the template starts with a macro`() {
        val template = "[[one]] foo [[two]] bar"
        val expected = "1 foo 2 bar"

        val processor = MacroProcessor(template = template, substitutions = substitutions)

        assertEquals(expected = expected, actual = processor.renderedText)
    }

    @Test
    fun `it substitutes key-values when the template ends with a macro`() {
        val template = "foo [[one]] bar [[two]]"
        val expected = "foo 1 bar 2"

        val processor = MacroProcessor(template = template, substitutions = substitutions)

        assertEquals(expected = expected, actual = processor.renderedText)
    }

    @Test
    fun `it skips over non-existent keys`() {
        val template = "foo [[nonexistent]] bar"

        val processor = MacroProcessor(template = template, substitutions = substitutions)

        assertEquals(expected = template, actual = processor.renderedText)
    }

    @Test
    fun `it parses equal delimiters`() {
        val template = "foo |one| bar |two| baz"
        val expected = "foo 1 bar 2 baz"

        val processor = MacroProcessor(
            template = template,
            substitutions = substitutions,
            openTag = "|",
            closeTag = "|"
        )

        assertEquals(expected = expected, actual = processor.renderedText)
    }

    @Test
    fun `macro replacement shouldn't be recursive`() {
        val invalidSubstitutions = mapOf(
            "one" to "[[two]]",
            "two" to "2"
        )

        val template = "foo [[one]] bar"
        val expected = "foo [[two]] bar"

        val processor = MacroProcessor(template = template, substitutions = invalidSubstitutions)

        assertEquals(expected = expected, actual = processor.renderedText)
    }
}
