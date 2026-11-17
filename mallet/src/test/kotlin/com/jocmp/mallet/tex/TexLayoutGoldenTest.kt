package com.jocmp.mallet.tex

import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.fail

/**
 * Pins the exact layout of the coverage corpus and every symbol command, so changes to
 * [TexLayout] or the builders can't move glyphs without notice. Regenerate after an intended
 * change with `TEX_GOLDEN_UPDATE=1 ./gradlew :mallet:test --tests '*TexLayoutGoldenTest' --rerun`.
 */
class TexLayoutGoldenTest {
    @Test
    fun layoutsMatchGolden() {
        val actual = cases().joinToString("") { (label, tex, display) -> dump(label, tex, display) }

        if (System.getenv("TEX_GOLDEN_UPDATE") != null) {
            File(GOLDEN_PATH).apply { parentFile.mkdirs() }.writeText(actual)
            return
        }

        val expected =
            TexLayoutGoldenTest::class.java.getResourceAsStream("/com/jocmp/mallet/tex/layout-golden.txt")
                ?.bufferedReader()?.readText()
                ?: fail("Missing layout-golden.txt; generate it with TEX_GOLDEN_UPDATE=1")

        val mismatches = compare(expected.lines(), actual.lines())
        if (mismatches.isNotEmpty()) {
            fail("${mismatches.size} layout differences:\n" + mismatches.take(MAX_REPORTED).joinToString("\n"))
        }
    }

    private fun cases(): List<Triple<String, String, Boolean>> {
        val corpus = TexCoverageTest.corpus
        val display = corpus.map { Triple("display", it, true) }
        val inline =
            corpus.filterNot { tex -> TexCoverageTest.displayOnly.any { tex.contains(it) } }
                .map { Triple("inline", it, false) }
        val symbols = TexCoverageTest.symbolCommands.map { Triple("symbol", "\\$it", false) }
        return display + inline + symbols
    }

    private fun dump(label: String, tex: String, display: Boolean): String {
        val out = StringBuilder("== $label ${tex.replace("\n", " ")}\n")
        val render = Tex.render(tex, displayMode = display).getOrElse {
            return out.append("error ${it.message?.lineSequence()?.first()}\n").toString()
        }
        dumpBox(out, "box", render.box)
        render.tag?.let { dumpBox(out, "tag", it) }
        return out.toString()
    }

    private fun dumpBox(out: StringBuilder, name: String, box: TexBox) {
        out.append("$name ${n(box.width)} ${n(box.height)} ${n(box.depth)}\n")
        box.items.forEach { item ->
            val line =
                when (item) {
                    is TexItem.Glyph ->
                        "glyph ${n(item.x)} ${n(item.y)} ${n(item.size)} ${item.fontName} ${item.color} ${item.mirrored} ${item.text.codePoints().toArray().joinToString(",")}"
                    is TexItem.Rect ->
                        "rect ${n(item.x)} ${n(item.y)} ${n(item.width)} ${n(item.height)} ${item.color} ${item.dashed}"
                    is TexItem.Svg ->
                        "svg ${n(item.x)} ${n(item.y)} ${n(item.width)} ${n(item.height)} ${item.viewBox?.joinToString(",") { n(it) }} " +
                            "${item.preserveAspectRatio} ${item.color} ${item.paths.joinToString("|").hashCode()} " +
                            (item.clip?.let { "clip ${n(it.x)} ${n(it.y)} ${n(it.width)} ${n(it.height)}" } ?: "noclip")
                    is TexItem.Line ->
                        "line ${n(item.x1)} ${n(item.y1)} ${n(item.x2)} ${n(item.y2)} ${n(item.strokeWidth)} ${item.color}"
                }
            out.append(line).append('\n')
        }
    }

    /** Line-by-line, token-by-token; numbers may differ by [TOLERANCE] em. */
    private fun compare(expected: List<String>, actual: List<String>): List<String> {
        val mismatches = mutableListOf<String>()
        var header = ""
        (0 until maxOf(expected.size, actual.size)).forEach { i ->
            val e = expected.getOrNull(i)
            val a = actual.getOrNull(i)
            if (e?.startsWith("== ") == true) {
                header = e
            }
            if (e != a && !sameWithinTolerance(e, a)) {
                mismatches.add("$header\n  expected: $e\n  actual:   $a")
            }
        }
        return mismatches
    }

    private fun sameWithinTolerance(e: String?, a: String?): Boolean {
        if (e == null || a == null) {
            return false
        }
        val et = e.split(' ')
        val at = a.split(' ')
        return et.size == at.size &&
            et.indices.all { i ->
                val x = et[i].toDoubleOrNull()
                val y = at[i].toDoubleOrNull()
                if (x != null && y != null) abs(x - y) <= TOLERANCE else et[i] == at[i]
            }
    }

    private fun n(value: Double): String {
        val v = if (abs(value) < 5e-7) 0.0 else value
        return String.format(Locale.ROOT, "%.6f", v)
    }

    private companion object {
        const val GOLDEN_PATH = "src/test/resources/com/jocmp/mallet/tex/layout-golden.txt"
        const val TOLERANCE = 1e-6
        const val MAX_REPORTED = 20
    }
}
