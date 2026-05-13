package com.jocmp.hyperview

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Runs every mercury-parser HTML fixture through [HtmlParser] to catch crashes
 * and regressions across a wide spread of real-world article HTML.
 *
 * Resolves the fixture directory in this order:
 *   1. `hyperview.fixtures.dir` system property (CI-friendly override).
 *   2. `HYPERVIEW_FIXTURES_DIR` env var.
 *   3. `../mercury-parser/fixtures` relative to the repo root (developer default).
 *
 * Skips silently when fixtures aren't available, so CI without the sibling
 * checkout doesn't fail.
 */
class MercuryFixtureSweepTest {

    @Test
    fun `every fixture parses without throwing`() {
        val dir = fixtureDir() ?: run {
            println("[hyperview] mercury-parser fixtures not found; skipping sweep.")
            return
        }
        val fixtures = dir.walkTopDown().filter { it.isFile && it.extension == "html" }.toList()
        assertTrue(fixtures.isNotEmpty(), "Expected fixtures in $dir")
        assertTrue(fixtures.size > 100, "Expected lots of fixtures, got ${fixtures.size} in $dir")

        val failures = mutableListOf<Pair<File, Throwable>>()
        for (file in fixtures) {
            try {
                val doc = HtmlParser.parse(file.readText())
                check(doc.blocks.size >= 0)
            } catch (t: Throwable) {
                failures += file to t
            }
        }

        if (failures.isNotEmpty()) {
            fail(
                "Failed to parse ${failures.size}/${fixtures.size} fixtures:\n" +
                    failures.take(10).joinToString("\n") { (f, t) -> "  ${f.name}: ${t.message}" }
            )
        }
    }

    @Test
    fun `most fixtures yield at least one block`() {
        val dir = fixtureDir() ?: return
        val fixtures = dir.walkTopDown().filter { it.isFile && it.extension == "html" }.toList()
        val empty = fixtures.filter { HtmlParser.parse(it.readText()).blocks.isEmpty() }
        // Some fixtures may legitimately be empty shells; allow a small tail.
        val ratio = empty.size.toDouble() / fixtures.size
        assertTrue(
            ratio < 0.05,
            "More than 5% of fixtures (${empty.size}/${fixtures.size}) yielded zero blocks: " +
                empty.take(10).joinToString { it.name }
        )
    }

    private fun fixtureDir(): File? {
        System.getProperty("hyperview.fixtures.dir")?.let { File(it).takeIf(File::isDirectory)?.let { return it } }
        System.getenv("HYPERVIEW_FIXTURES_DIR")?.let { File(it).takeIf(File::isDirectory)?.let { return it } }
        val candidates = listOf(
            File("../mercury-parser/fixtures"),
            File("../../mercury-parser/fixtures"),
            File(System.getProperty("user.home"), "dev/jocmp/mercury-parser/fixtures"),
        )
        return candidates.firstOrNull { it.isDirectory }?.absoluteFile
    }
}
