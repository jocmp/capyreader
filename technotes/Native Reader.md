# Native Reader

The article reader is Compose, not a WebView. Shipped in #2245 (2026-09-20), replacing the templated HTML page.

## Pipeline

HTML -> `mallet` -> `List<LinearElement>` -> Compose.

- `Mallet.flatten(html, baseUrl)` returns `Result<LinearArticle>`. Failures are `LinearizeError`.
- `mallet` is a plain Kotlin JVM module with only JSoup, so it tests under `:mallet:test` with no Android.
- Dependency chain is `mallet -> capy -> app`. `Article.flatten()` in `capy/articles/ArticleFlattenExt.kt` is the seam; the app never talks to mallet's parser directly.
- Parsing happens in the ViewModel so the article and its elements arrive in the same frame. Splitting them causes a visible judder on the article transition.
- Renderer lives in `app/ui/articles/reader`.

Caps are 2000 elements / 100k chars. Over that, `LinearArticle.truncated` is set and mallet appends no notice text — `capy` decides what to show.

## Full content

`ArticleContent.fetch` (device-side, browser headers) -> `Mercury.parse(html = ...)` in `capy/articles/MercuryParser.kt`, from the `:mercuryparser` module (Kotlin port of Mercury Parser; per-site rules live in `extractors/custom`). Replaces the ~1MB JS asset the WebView loaded.

## Styling

Matches the old `stylesheet.css`, which is worth diffing against before changing spacing:

- Body line height 1.6, paragraph gaps of `1em` that scale with the reader font size.
- Byline and feed name inherit the browser default 16px in `onSurface` — they were bare `div`s, not a smaller style.
- Headings use 1.3 and code blocks ~1.78 where the stylesheet had ~1.2 and 1.6. Not yet reconciled.

## YouTube

Embeds play inline in a `WebView`; everything else in the reader is native. Two constraints, both expensive to rediscover:

- **A WebView sends no `Referer`,** so YouTube rejects the embed with error 152. The fix is `loadDataWithBaseURL` with `baseUrl` set to the app's identity (`https://capyreader.com`) plus `referrerpolicy="strict-origin-when-cross-origin"` on the iframe. Loading the embed URL as a top-level navigation is not enough — that leaves YouTube with no embedding origin, and setting the `Referer` header alone does not satisfy it.
- **A WebView's containing block has no height,** so `height: 100%` and `100vh` collapse to zero. A zero-height iframe still loads its `src`, so the player boots and renders nothing. Size off `100vw` with `aspect-ratio` instead.

Fullscreen goes through `WebChromeClient.onShowCustomView` into a `Dialog`. Embedded players must be at least 200x200 CSS px; 480x270 recommended for 16:9.

- <https://developers.google.com/youtube/terms/required-minimum-functionality>

## Math

Feeds carry server-rendered math. KaTeX emits three copies of each formula (MathML, the TeX in `<annotation encoding="application/x-tex">`, and a visual `katex-html` copy), and flattening all three garbled the text.

- `mathSource` in `mallet/MathSource.kt` recovers TeX from KaTeX spans, bare `<math>` with a TeX annotation, Pandoc `span.math`, and MathJax 2 `script[type=math/tex]`. Inline math becomes `LinearTextAnnotationMath`; display math becomes `LinearMath`.
- `replaceMathWithSource()` does the same for plain-text summaries (`ContentFormatter`).
- Default: the TeX source as inline code or a code block. "Render math formulas" (`article_render_math`, off by default) typesets it with `com.jocmp.mallet.tex`.

### KaTeX port

`mallet.tex` is a port of KaTeX (MIT, `LICENSE-KaTeX.txt`) at `9b2280d`. Feed TeX is whatever KaTeX accepted when the blog was built, so the parser follows KaTeX rather than LaTeX: anything KaTeX rejects fails here too and the source is shown.

- **Ported line for line:** Lexer, MacroExpander, macros (including the four `defineMacro` calls that live in `operatorname.ts`, `phantom.ts`, and `array.ts`), Parser, symbols, Unicode tables, Options/Style/units, buildCommon, buildHTML, delimiter, stretchy, svgGeometry, and the HTML builders in `functions/` and `environments/`. MathML builders are skipped. Files keep KaTeX's names, so a KaTeX change maps onto one Kotlin file.
- **Settings** are KaTeX's defaults: `strict: "warn"` (so strict checks never change behavior), `trust: false` (so `\href`, `\url`, `\includegraphics`, and `\html*` take the untrusted path), `throwOnError: true` (so errors fall back to source instead of red text).
- **`TexLayout` stands in for the browser.** KaTeX computes heights and depths but leaves widths, baselines, and stretchy SVG sizing to katex.css. `TexLayout` emulates the rules the builders rely on (inline-block runs, `text-align` in vlists, `width: 100%`, sizing classes, margins, padding, borders, laps, relative offsets, absolutely positioned SVG, overflow clipping) and emits `TexItem`s in ems. `makeVList` stores resolved row baselines in `VListSpan` instead of pstrut spans; no builder looks inside a vlist, so this is invisible to them.
- **Metrics:** `fontMetricsData.tsv` is KaTeX's `fontMetricsData.js`, regenerated with `node mallet/scripts/katex_metrics.mjs <katex-checkout>`. Glyphs without metrics (e.g. CJK in `\text`) are measured by the renderer's `TexTextMeasurer`, as a browser would.
- **Fonts:** the 18 KaTeX TTFs with metrics are in `app/src/main/res/font/katex_*` (~500 KB). `MathFonts` draws glyphs with `Paint`, so missing glyphs fall back to system fonts.
- `tryCombineChars` is a no-op: merging symbols only shrinks the DOM and would lose per-glyph metrics.
- Formulas render at 1.21× the body size (`Tex.FONT_SCALE`), as `.katex` does.

`TexCoverageTest` renders a corpus of feed-shaped formulas; `TEX_PNG=1 ./gradlew :mallet:test --tests '*TexCoverageTest' --rerun` writes Java2D PNGs to `mallet/build/tex-png` for comparing against katex.org. `MathElementTest` (instrumented) checks the Compose path and saves a sample to the app's files dir with `-e mathScreenshot true`.

Known gaps:

- Equation numbers come from a CSS counter in KaTeX, which counts across the page; here each formula counts from (1).
- `\color` values aren't adjusted for dark themes.
- `\tag` sits at the end of the row and can overlap a formula wider than the screen, as `.katex-tag` does.
- Display math scrolls horizontally when it can. With horizontal pagination or in a table cell, it scales down to fit the width instead.
- An inline placeholder covers only the formula's height (`AboveBaseline`), because Compose has no baseline-offset alignment. The depth draws into the line spacing, so a tall denominator or subscript can touch the line below.
- An inline formula is one unbreakable placeholder. KaTeX breaks inline math after top-level relations and binary operators; here, a formula wider than the line overflows.

## Gaps

- Non-YouTube iframe embeds (Instagram, Bluesky, Vimeo, Spotify) are dropped at parse time, not just unrendered — `parseIframeVideo` only emits for YouTube. The blockquote form of an Instagram embed survives as quoted fallback text. These all worked under the WebView.
- Heading and code-block line heights, above.
