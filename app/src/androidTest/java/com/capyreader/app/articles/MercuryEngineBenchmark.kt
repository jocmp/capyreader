package com.capyreader.app.articles

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jocmp.capy.articles.KotlinMercuryEngine
import com.jocmp.capy.articles.MercuryEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compares the Kotlin port against the JS parser in JavaScriptSandbox using
 * the mercuryparser fixtures. Results are logged under [TAG]:
 *
 * adb logcat -s MercuryBench
 *
 * Pass `-e fixtures a.html,b.html` to run a subset.
 */
@RunWith(AndroidJUnit4::class)
class MercuryEngineBenchmark {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun compareEngines() = runBlocking {
        val fixtures = loadFixtures()
        val sandbox = SandboxMercuryEngine(
            context = instrumentation.targetContext,
            fallback = { _, _ -> error("Sandbox fell back to Kotlin") },
        )

        val sandboxColdStart = elapsedMillis { assertTrue(sandbox.prepare()) }
        val (first) = fixtures
        val sandboxFirstParse = elapsedMillis { sandbox.parse(first.url, first.html) }
        val kotlinFirstParse = elapsedMillis { KotlinMercuryEngine.parse(first.url, first.html) }

        Log.i(TAG, "cold\tsandbox_connect=$sandboxColdStart\tsandbox_first=$sandboxFirstParse\tkotlin_first=$kotlinFirstParse")
        Log.i(TAG, "header\tfixture\tbytes\tkotlin_ms\tjs_ms\tkotlin_len\tjs_len")

        fixtures.forEach { fixture ->
            val kotlin = measure(KotlinMercuryEngine, fixture)
            val js = measure(sandbox, fixture)

            Log.i(
                TAG,
                "row\t${fixture.name}\t${fixture.html.length}\t${kotlin.millis}\t${js.millis}\t${kotlin.length}\t${js.length}"
            )
        }

        sandbox.close()
    }

    private suspend fun measure(engine: MercuryEngine, fixture: Fixture): Measurement {
        val length = try {
            engine.parse(fixture.url, fixture.html).content?.length ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "error\t${fixture.name}\t${e.message}")
            return Measurement(millis = -1, length = -1)
        }

        val samples = List(RUNS) { elapsedMillis { engine.parse(fixture.url, fixture.html) } }

        return Measurement(millis = samples.sorted()[RUNS / 2], length = length)
    }

    private fun loadFixtures(): List<Fixture> {
        val assets = instrumentation.context.assets
        val only = InstrumentationRegistry.getArguments().getString("fixtures")?.split(",")

        return assets.list(FIXTURES).orEmpty()
            .filter { it.endsWith(".html") && (only == null || it in only) }
            .sorted()
            .map { name ->
                val html = assets.open("$FIXTURES/$name").bufferedReader().use { it.readText() }
                val host = name.removeSuffix(".html").substringBefore("--")

                Fixture(name = name, url = "https://$host/", html = html)
            }
    }

    private inline fun elapsedMillis(block: () -> Unit): Long {
        val start = System.nanoTime()
        block()
        return (System.nanoTime() - start) / 1_000_000
    }

    private data class Fixture(val name: String, val url: String, val html: String)

    private data class Measurement(val millis: Long, val length: Int)

    companion object {
        private const val TAG = "MercuryBench"
        private const val FIXTURES = "fixtures"
        private const val RUNS = 3
    }
}
