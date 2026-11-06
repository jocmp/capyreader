package com.jocmp.malletmath

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MathParserTest {
    @Test
    fun primesBecomeSuperscripts() {
        val atom = parse("f''").single()

        assertEquals("f", atom.nucleus)
        assertEquals(listOf("′", "′"), atom.superscript!!.map { it.nucleus })
    }

    @Test
    fun primeCombinesWithLaterSuperscript() {
        val atom = parse("f'^2").single()

        assertEquals(listOf("′", "2"), atom.superscript!!.map { it.nucleus })
    }

    @Test
    fun operatorName() {
        val op = assertIs<LargeOperator>(parse("\\operatorname{rank} A").first())

        assertEquals("rank", op.nucleus)
        assertEquals(false, op.hasLimits)
    }

    @Test
    fun starredOperatorNameHasLimits() {
        val op = assertIs<LargeOperator>(parse("\\operatorname*{arg\\,max}_x").first())

        assertEquals("arg max", op.nucleus)
        assertTrue(op.hasLimits)
    }

    @Test
    fun tagsAndLabelsAreDropped() {
        val atoms = parse("E = mc^2 \\tag{1} \\label{eq:energy}")

        assertEquals(listOf("E", "=", "m", "c"), atoms.map { it.nucleus })
    }

    @Test
    fun alignEnvironmentAlternatesColumns() {
        val table = assertIs<MathTable>(parse("\\begin{align} a &= b & c &= d \\\\ e &= f \\end{align}").single())

        assertEquals(2, table.numRows)
        assertEquals(4, table.numColumns)
        assertEquals(ColumnAlignment.RIGHT, table.alignment(0))
        assertEquals(ColumnAlignment.LEFT, table.alignment(1))
        assertEquals(ColumnAlignment.RIGHT, table.alignment(2))
    }

    @Test
    fun trailingRowBreakIsIgnored() {
        val table = assertIs<MathTable>(parse("\\begin{aligned} a &= b \\\\ \\end{aligned}").single())

        assertEquals(1, table.numRows)
    }

    @Test
    fun casesWrapsTableInLeftBrace() {
        val inner = assertIs<Inner>(parse("\\begin{cases} 1 & x > 0 \\\\ 0 & \\text{otherwise} \\end{cases}").single())

        assertEquals("{", inner.leftBoundary)
        assertEquals("", inner.rightBoundary)
    }

    @Test
    fun arrayReadsColumnSpec() {
        val table = assertIs<MathTable>(parse("\\begin{array}{l|cr} a & b & c \\end{array}").single())

        assertEquals(ColumnAlignment.LEFT, table.alignment(0))
        assertEquals(ColumnAlignment.CENTER, table.alignment(1))
        assertEquals(ColumnAlignment.RIGHT, table.alignment(2))
    }

    @Test
    fun colorAppliesToRestOfGroup() {
        val atoms = parse("a {\\color{red} b c} d")

        val color = assertIs<MathColor>(atoms[1])
        assertEquals(0xFFFF0000.toInt(), color.argb)
        assertEquals(listOf("b", "c"), color.innerList.map { it.nucleus })
        assertEquals("d", atoms[2].nucleus)
    }

    @Test
    fun hexColor() {
        val color = assertIs<MathColor>(parse("\\textcolor{#0a0}{x}").single())

        assertEquals(0xFF00AA00.toInt(), color.argb)
    }

    @Test
    fun notNegatesRelation() {
        assertEquals("≠", parse("\\not=").single().nucleus)
        assertEquals("∉", parse("\\not\\in").single().nucleus)
    }

    @Test
    fun pmodExpands() {
        val atoms = parse("a \\equiv b \\pmod{n}")

        assertTrue(atoms.any { it.nucleus == "(" })
        assertTrue(atoms.any { it.nucleus == "n" })
    }

    @Test
    fun textKeepsSpaces() {
        val atoms = parse("\\text{if } x")

        assertEquals(listOf("i", "f", " ", "x"), atoms.map { it.nucleus })
        assertEquals(FontStyle.ROMAN, atoms[0].fontStyle)
        assertEquals(FontStyle.DEFAULT, atoms[3].fontStyle)
    }

    @Test
    fun bigDelimiters() {
        val atoms = parse("\\bigl( x \\bigr)")

        val open = assertIs<SizedDelimiter>(atoms.first())
        assertEquals(AtomType.OPEN, open.type)
        assertEquals(1.2f, open.height)
        assertEquals(AtomType.CLOSE, atoms.last().type)
    }

    @Test
    fun unicodeSymbolsKeepTheirType() {
        val atoms = parse("a × b ≤ c")

        assertEquals(AtomType.BINARY_OPERATOR, atoms[1].type)
        assertEquals(AtomType.RELATION, atoms[3].type)
    }

    @Test
    fun displayFractionWrapsStyle() {
        val inner = assertIs<Inner>(parse("\\dfrac{1}{2}").single())

        assertEquals(LineStyle.DISPLAY, assertIs<MathStyle>(inner.innerList.first()).style)
        assertIs<Fraction>(inner.innerList.last())
    }

    @Test
    fun emptyDelimiterForLeftDot() {
        val inner = assertIs<Inner>(parse("\\left. x \\right|").single())

        assertEquals("", inner.leftBoundary)
        assertEquals("|", inner.rightBoundary)
    }

    @Test
    fun commentsAreSkipped() {
        assertEquals(listOf("a", "b"), parse("a % comment\nb").map { it.nucleus })
    }

    @Test
    fun spacingCommands() {
        val space = assertIs<MathSpace>(parse("\\hspace{1em}").single())

        assertEquals(18f, space.space)
    }

    @Test
    fun unknownCommandFails() {
        assertFailsWith<MathParseException> { parse("\\notacommand x") }
    }

    @Test
    fun mismatchedBracesFail() {
        assertFailsWith<MathParseException> { parse("{x") }
        assertFailsWith<MathParseException> { parse("x}") }
    }

    @Test
    fun missingRightFails() {
        assertFailsWith<MathParseException> { parse("\\left( x") }
    }

    @Test
    fun unmatchedEnvironmentFails() {
        assertFailsWith<MathParseException> { parse("\\begin{matrix} a \\end{pmatrix}") }
    }

    @Test
    fun overUnderSet() {
        val stack = assertIs<Stack>(parse("\\overset{\\text{def}}{=}").single())

        assertEquals(AtomType.RELATION, stack.type)
        assertNull(stack.under)
    }

    private fun parse(tex: String): MathList = MathParser(tex).parse()
}
