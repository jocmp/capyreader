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

`ArticleContent.fetch` (device-side, browser headers) -> `Mercury.parse(html = ...)` in `capy/articles/MercuryParser.kt`, using `com.jocmp:mercury-parser` (own library). Replaces the ~1MB JS asset the WebView loaded.

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

## Gaps

- Non-YouTube iframe embeds (Instagram, Bluesky, Vimeo, Spotify) are dropped at parse time, not just unrendered — `parseIframeVideo` only emits for YouTube. The blockquote form of an Instagram embed survives as quoted fallback text. These all worked under the WebView.
- Heading and code-block line heights, above.
