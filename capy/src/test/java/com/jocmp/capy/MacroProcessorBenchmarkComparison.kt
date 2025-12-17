package com.jocmp.capy

import org.junit.Test
import kotlin.system.measureNanoTime

/**
 * Benchmark comparing optimized StringBuilder implementation vs original String concatenation
 */
class MacroProcessorBenchmarkComparison {
    private val template = """
        <!DOCTYPE html>
        <html dir="auto">
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1" />
            <meta http-equiv="Content-Security-Policy" content="upgrade-insecure-requests" />
            <style>
              :root {
                --color-primary: {{color_primary}};
                --color-surface: {{color_surface}};
                --color-surface-container-highest: {{color_surface_container_highest}};
                --color-on-surface: {{color_on_surface}};
                --color-on-surface-variant: {{color_on_surface_variant}};
                --color-surface-variant: {{color_surface_variant}};
                --color-primary-container: {{color_primary_container}};
                --color-on-primary-container: {{color_on_primary_container}};
                --color-secondary: {{color_secondary}};
                --color-surface-container: {{color_surface_container}};
                --color-surface-tint: {{color_surface_tint}};
                --article-top-margin: {{top_margin}};
                --article-font-size: {{font_size}};
                --pre-white-space: {{pre_white_space}};
              }
            </style>
            {{font_preload}}
            <link rel="stylesheet" href="https://appassets.androidplatform.net/assets/stylesheet.css">
            <script type="text/javascript" src="https://appassets.androidplatform.net/assets/media.js"></script>
            <script type="text/javascript" src="https://appassets.androidplatform.net/assets/mercury-parser.js"></script>
            <script type="text/javascript" src="https://appassets.androidplatform.net/assets/full-content.js"></script>
          </head>
          <body>
            <article role="main">
              <header>
                <a class="article__header" href="{{external_link}}">
                  <h1 class="article__title">{{title}}</h1>
                  <div>{{byline}}</div>
                  <div>{{feed_name}}</div>
                </a>
              </header>
              <div class="article__body article__body--font-{{font_family}}">
                <div id="article-body-content"></div>
              </div>
            </article>
          </body>
        </html>
    """.trimIndent()

    private val substitutions = mapOf(
        "color_primary" to "#FF6200EE",
        "color_surface" to "#FFFFFFFF",
        "color_surface_container_highest" to "#FFE7E0EC",
        "color_on_surface" to "#FF1C1B1F",
        "color_on_surface_variant" to "#FF49454F",
        "color_surface_variant" to "#FFE7E0EC",
        "color_primary_container" to "#FFEADDFF",
        "color_on_primary_container" to "#FF21005D",
        "color_secondary" to "#FF625B71",
        "color_surface_container" to "#FFF3EDF7",
        "color_surface_tint" to "#FF6200EE",
        "top_margin" to "64px",
        "font_size" to "16px",
        "pre_white_space" to "pre-wrap",
        "font_preload" to "",
        "external_link" to "https://example.com/article",
        "title" to "Example Article Title",
        "byline" to "By John Doe on Jan 1, 2024",
        "feed_name" to "Example Feed",
        "font_family" to "system-default"
    )

    @Test
    fun `compare optimized vs unoptimized`() {
        val warmupIterations = 200
        val benchmarkIterations = 2000

        val output = StringBuilder()
        output.appendLine("\n=== MacroProcessor Performance Comparison (REVERSED ORDER) ===\n")

        // Interleaved warmup to ensure both are JIT compiled
        output.appendLine("Warming up both implementations (old first)...")
        repeat(warmupIterations / 2) {
            MacroProcessorOld(template = template, substitutions = substitutions).renderedText
            MacroProcessor(template = template, substitutions = substitutions).renderedText
        }

        output.appendLine("Running benchmarks with interleaved execution...\n")

        // Interleaved benchmarking to reduce JVM bias
        val optimizedTimes = mutableListOf<Long>()
        val unoptimizedTimes = mutableListOf<Long>()

        repeat(benchmarkIterations) {
            // Alternate between implementations to avoid any temporal bias
            // REVERSED ORDER - old implementation first
            if (it % 2 == 0) {
                val time = measureNanoTime {
                    MacroProcessorOld(template = template, substitutions = substitutions).renderedText
                }
                unoptimizedTimes.add(time)

                val time2 = measureNanoTime {
                    MacroProcessor(template = template, substitutions = substitutions).renderedText
                }
                optimizedTimes.add(time2)
            } else {
                val time = measureNanoTime {
                    MacroProcessor(template = template, substitutions = substitutions).renderedText
                }
                optimizedTimes.add(time)

                val time2 = measureNanoTime {
                    MacroProcessorOld(template = template, substitutions = substitutions).renderedText
                }
                unoptimizedTimes.add(time2)
            }
        }

        optimizedTimes.sort()
        unoptimizedTimes.sort()

        // Print comparison
        output.appendLine("=== Results (n=$benchmarkIterations) ===\n")

        output.appendLine("Optimized (StringBuilder):")
        output.append(formatStats(optimizedTimes))

        output.appendLine("\nUnoptimized (String concatenation):")
        output.append(formatStats(unoptimizedTimes))

        output.appendLine("\n=== Speedup ===")
        val avgSpeedup = unoptimizedTimes.average() / optimizedTimes.average()
        val medianSpeedup = unoptimizedTimes[unoptimizedTimes.size / 2].toDouble() /
                           optimizedTimes[optimizedTimes.size / 2]
        output.appendLine("  Average: %.2fx faster".format(avgSpeedup))
        output.appendLine("  Median:  %.2fx faster".format(medianSpeedup))

        // Print to console
        println(output.toString())

        // Also write to file for easier reading
        java.io.File("/tmp/macro_benchmark_results.txt").writeText(output.toString())
    }

    private fun benchmarkOptimized(warmup: Int, iterations: Int): List<Long> {
        // Warmup
        repeat(warmup) {
            val processor = MacroProcessor(template = template, substitutions = substitutions)
            processor.renderedText
        }

        // Benchmark
        val times = mutableListOf<Long>()
        repeat(iterations) {
            val time = measureNanoTime {
                val processor = MacroProcessor(template = template, substitutions = substitutions)
                processor.renderedText
            }
            times.add(time)
        }
        times.sort()
        return times
    }

    private fun benchmarkUnoptimized(warmup: Int, iterations: Int): List<Long> {
        // Warmup
        repeat(warmup) {
            val processor = MacroProcessorOld(template = template, substitutions = substitutions)
            processor.renderedText
        }

        // Benchmark
        val times = mutableListOf<Long>()
        repeat(iterations) {
            val time = measureNanoTime {
                val processor = MacroProcessorOld(template = template, substitutions = substitutions)
                processor.renderedText
            }
            times.add(time)
        }
        times.sort()
        return times
    }

    private fun formatStats(times: List<Long>): String {
        val min = times.first()
        val max = times.last()
        val median = times[times.size / 2]
        val avg = times.average()
        val p95 = times[(times.size * 0.95).toInt()]
        val p99 = times[(times.size * 0.99).toInt()]

        return buildString {
            appendLine("  Min:    ${formatNanos(min)}")
            appendLine("  Median: ${formatNanos(median)}")
            appendLine("  Avg:    ${formatNanos(avg)}")
            appendLine("  P95:    ${formatNanos(p95)}")
            appendLine("  P99:    ${formatNanos(p99)}")
            appendLine("  Max:    ${formatNanos(max)}")
        }
    }

    private fun formatNanos(nanos: Double): String {
        return when {
            nanos < 1_000 -> "%.2f ns".format(nanos)
            nanos < 1_000_000 -> "%.2f μs".format(nanos / 1_000)
            else -> "%.2f ms".format(nanos / 1_000_000)
        }
    }

    private fun formatNanos(nanos: Long): String = formatNanos(nanos.toDouble())
}
