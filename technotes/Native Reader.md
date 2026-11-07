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

- `mathSource` in `mallet/MathSource.kt` recovers TeX from KaTeX spans, bare `<math>` with a TeX annotation, Pandoc `span.math`, and MathJax 2 `script[type=math/tex]`. Inline math becomes `LinearTextAnnotationMath` over the collapsed TeX; display math becomes `LinearMath`.
- `replaceMathWithSource()` does the same for plain-text summaries (`ContentFormatter`), which had the same leak.
- Default: the TeX source as inline code or a code block. "Render math formulas" (`article_render_math`, Reader › Content, off by default) draws it with `com.jocmp.mallet.math`.

`mallet.math` is extracted from AndroidMath (MIT, a Kotlin port of iosMath) at `170d8e3`: parser, atom model and typesetter, rewritten to produce an immutable `MathBox` tree. Editor plumbing (index ranges, `toLatexString`, placeholders) and the View were dropped.

- **No FreeType.** AndroidMath read fonts and rasterised every glyph through JNI FreeType, which needs the NDK. `scripts/extract_font.py` precomputes metrics, MATH tables and outlines into `math-glyphs.bin` (a Latin Modern Math subset, ~375 KB compressed), and the app draws glyphs as Compose `Path`s. `minSdk` 30 rules out `Canvas.drawGlyphs`. The font is GUST-licensed, which asks derived works to be renamed; hence the file name and `FONT-NOTICE.txt`.
- **Target is KaTeX's command set**, since feed TeX is whatever KaTeX accepted. Anything else throws `MathParseException` and the source is shown instead.
- **Fidelity:** `MathTypesetterTest` ports AndroidMath's typesetter dimensions and matches within 0.05pt.
- Formulas render at 1.21× the body size, as KaTeX does. Inline placeholders are the formula's ascent tall on `AboveBaseline`, so baselines line up and the descent draws into the line spacing.
- Upstream bugs fixed: `\ll`/`\gg` swapped, `\Uparrow`-family delimiters missing `\u`, deprecated U+2329 angle brackets, accent variants falling back to `.notdef`, delimiter shortfall in raw points rather than em, and a leading minus after `\textstyle` spaced as binary.

`\overbrace`/`\underbrace` build the brace from the font's horizontal variants and assembly parts (extracted alongside the vertical ones) and always set their label as a limit, as TeX does.

Known gaps: `\middle`, array rules (`\hline` is skipped), non-stretching `\xrightarrow`, `\tag` numbers (dropped), and `\color` values aren't adjusted for dark themes.

## Gaps

- Non-YouTube iframe embeds (Instagram, Bluesky, Vimeo, Spotify) are dropped at parse time, not just unrendered — `parseIframeVideo` only emits for YouTube. The blockquote form of an Instagram embed survives as quoted fallback text. These all worked under the WebView.
- Heading and code-block line heights, above.
