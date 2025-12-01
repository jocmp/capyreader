package com.jocmp.capy

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.measureTime

class MacroProcessorTest {
    @Test
    fun `benchmark MacroProcessor vs String format`() {
        // Template with %s placeholders (like ReadYou uses)
        val formatTemplate = """
            <!DOCTYPE html>
            <html dir="auto">
              <head>
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <style>
                  :root {
                    --color-primary: %s;
                    --color-surface: %s;
                    --color-surface-container-highest: %s;
                    --color-on-surface: %s;
                    --color-on-surface-variant: %s;
                    --color-surface-variant: %s;
                    --color-primary-container: %s;
                    --color-on-primary-container: %s;
                    --color-secondary: %s;
                    --color-surface-container: %s;
                    --color-surface-tint: %s;
                    --article-top-margin: %s;
                    --article-font-size: %s;
                    --pre-white-space: %s;
                  }
                </style>
                %s
              </head>
              <body>
                <article role="main">
                  <header>
                    <a class="article__header" href="%s">
                      <h1 class="article__title">%s</h1>
                      <div>%s</div>
                      <div>%s</div>
                    </a>
                  </header>
                  <div class="article__body article__body--font-%s">
                    <div id="article-body-content">%s</div>
                  </div>
                </article>
              </body>
            </html>
        """.trimIndent()

        // Template with {{key}} placeholders (current approach)
        val macroTemplate = """
            <!DOCTYPE html>
            <html dir="auto">
              <head>
                <meta name="viewport" content="width=device-width, initial-scale=1" />
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
                    <div id="article-body-content">{{body}}</div>
                  </div>
                </article>
              </body>
            </html>
        """.trimIndent()

        // Simulate a large article body with HTML content
        val largeBody = """
            <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
            <p>Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</p>
            <img src="https://example.com/image1.jpg" alt="Example image" />
            <p>Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.</p>
            <blockquote>Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</blockquote>
            <pre><code>fun example() { println("Hello, World!") }</code></pre>
        """.trimIndent().repeat(20) // Make it ~20x larger

        // Ordered values for String.format
        val formatArgs = arrayOf(
            "#6750A4",       // color_primary
            "#FFFBFE",       // color_surface
            "#E6E0E9",       // color_surface_container_highest
            "#1C1B1F",       // color_on_surface
            "#49454F",       // color_on_surface_variant
            "#E7E0EC",       // color_surface_variant
            "#EADDFF",       // color_primary_container
            "#21005D",       // color_on_primary_container
            "#625B71",       // color_secondary
            "#F3EDF7",       // color_surface_container
            "#6750A4",       // color_surface_tint
            "64px",          // top_margin
            "16px",          // font_size
            "pre-wrap",      // pre_white_space
            """<link rel="preload" href="https://appassets.androidplatform.net/res/font/system.ttf" as="font" type="font/ttf" crossorigin>""", // font_preload
            "https://example.com/article/12345", // external_link
            "Sample Article Title That Could Be Pretty Long", // title
            "By John Doe · November 30, 2025", // byline
            "Tech News Daily", // feed_name
            "system",        // font_family
            largeBody        // body
        )

        val substitutions = mapOf(
            "color_primary" to "#6750A4",
            "color_surface" to "#FFFBFE",
            "color_surface_container_highest" to "#E6E0E9",
            "color_on_surface" to "#1C1B1F",
            "color_on_surface_variant" to "#49454F",
            "color_surface_variant" to "#E7E0EC",
            "color_primary_container" to "#EADDFF",
            "color_on_primary_container" to "#21005D",
            "color_secondary" to "#625B71",
            "color_surface_container" to "#F3EDF7",
            "color_surface_tint" to "#6750A4",
            "top_margin" to "64px",
            "font_size" to "16px",
            "pre_white_space" to "pre-wrap",
            "font_preload" to """<link rel="preload" href="https://appassets.androidplatform.net/res/font/system.ttf" as="font" type="font/ttf" crossorigin>""",
            "external_link" to "https://example.com/article/12345",
            "title" to "Sample Article Title That Could Be Pretty Long",
            "byline" to "By John Doe · November 30, 2025",
            "feed_name" to "Tech News Daily",
            "font_family" to "system",
            "body" to largeBody
        )

        val iterations = 1000

        // Warmup
        repeat(100) {
            MacroProcessor(macroTemplate, substitutions).renderedText
            String.format(formatTemplate, *formatArgs)
        }

        // Benchmark MacroProcessor
        val macroProcessorTime = measureTime {
            repeat(iterations) {
                MacroProcessor(macroTemplate, substitutions).renderedText
            }
        }

        // Benchmark String.format (ReadYou approach)
        val stringFormatTime = measureTime {
            repeat(iterations) {
                String.format(formatTemplate, *formatArgs)
            }
        }

        println("\n=== Benchmark Results ($iterations iterations) ===")
        println("MacroProcessor:   ${macroProcessorTime.inWholeMilliseconds}ms total, ${macroProcessorTime.inWholeNanoseconds / iterations}ns per iteration")
        println("String.format:    ${stringFormatTime.inWholeMilliseconds}ms total, ${stringFormatTime.inWholeNanoseconds / iterations}ns per iteration")
        println("Speedup:          ${String.format("%.2f", macroProcessorTime.inWholeNanoseconds.toDouble() / stringFormatTime.inWholeNanoseconds)}x faster with String.format")
        println()

        // Verify String.format produces valid output
        val formatResult = String.format(formatTemplate, *formatArgs)
        assertEquals(false, formatResult.contains("%s"), "String.format result should have no remaining placeholders")
    }
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

        val template = "<html>foo {{one}} bar {{two}} baz"
        val expected = "<html>foo 1 bar 2 baz"

        val processor = MacroProcessor(template = template, substitutions = substitutions)

        assertEquals(expected = expected, actual = processor.renderedText)
    }

    @Test
    fun `it substitutes key-values when the template starts with a macro`() {
        val template = "{{one}} foo {{two}} bar"
        val expected = "1 foo 2 bar"

        val processor = MacroProcessor(template = template, substitutions = substitutions)

        assertEquals(expected = expected, actual = processor.renderedText)
    }

    @Test
    fun `it substitutes key-values when the template ends with a macro`() {
        val template = "foo {{one}} bar {{two}}"
        val expected = "foo 1 bar 2"

        val processor = MacroProcessor(template = template, substitutions = substitutions)

        assertEquals(expected = expected, actual = processor.renderedText)
    }

    @Test
    fun `it skips uneven delimiters with partial open tag match`() {
        val template = "foo :root { bar {{two}}"
        val expected = "foo :root { bar 2"

        val processor = MacroProcessor(template = template, substitutions = substitutions)

        assertEquals(expected = expected, actual = processor.renderedText)
    }

    @Test
    fun `it skips uneven delimiters with total open tag match`() {
        val template = "foo {{one bar {{two}}"
        val expected = "foo {{one bar 2"

        val processor = MacroProcessor(template = template, substitutions = substitutions)

        assertEquals(expected = expected, actual = processor.renderedText)
    }

    @Test
    fun `it skips over non-existent keys`() {
        val template = "foo {{nonexistent}} bar"

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
            "one" to "{{two}}",
            "two" to "2"
        )

        val template = "foo {{one}} bar"
        val expected = "foo {{two}} bar"

        val processor = MacroProcessor(template = template, substitutions = invalidSubstitutions)

        assertEquals(expected = expected, actual = processor.renderedText)
    }
}
