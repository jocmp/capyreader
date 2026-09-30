# E Ink

Display & Appearance > E Ink groups four settings. On a detected E Ink device, all four turn on once, the first time the app starts, and again after logging out (logout clears preferences, then reapplies the device defaults). The theme also switches to Monochrome, unless the user already picked one.

| Setting            | Preference                             | Effect                                                         |
|--------------------|----------------------------------------|----------------------------------------------------------------|
| Reduce animations  | `reduce_motion`                        | Every Compose animation in `MainActivity` finishes instantly   |
| Tap to turn pages  | `article_enable_paging_tap_gesture`    | Left quarter goes back a page, right quarter forward           |
| E Ink scrollbar    | `article_enable_e_ink_scrollbar`       | Large scrollbar with page and line buttons (reader and list)   |
| Page turn buttons  | `article_enable_page_turn_keys`        | Volume keys, Page Up/Down, and arrow keys turn pages           |

The tap preference key predates this work (it was "E Ink tap to scroll", bottom-corner zones that jumped 96% of the screen), so existing users keep their setting.

## Prior art

- Kindle, Kobo: discrete pages, large invisible tap zones, page animation off by default.
- KOReader: calls continuous scroll "more suitable for non e-ink screens"; offers page overlap in scroll mode because lines get cut at the edges.
- Moon+ Reader: E Ink mode removes all animations. "Keep one line when paging" repeats the last line at the top of the next page.
- Readwise Reader: detects E Ink devices and turns on a bundle (reduce motion, reduce page animations, high contrast, volume buttons turn pages). This is the model for the bundle here.
- Boox: the system "E-ink Center" maps hardware page buttons to volume keys for most third-party apps, so volume keys are the de facto page-turn API.
- None of the RSS readers checked (Feeder, Read You, Inoreader, NewsBlur) had E Ink paging.

Related issues: #445 (reduce animation), #1247 (overlap), #1856 (tap scroll goes a line too far), #1236 (tap zones for next/previous article, not done), #1597 (keyboard shortcuts and page turners).

## Why not pages

A paged reader (discrete pages turned sideways, masks over partial lines, "3 / 12") was built and dropped. It needed a mask above and below every page, a trailing spacer so the last page could start on its break, re-breaking and snapping whenever an image loaded, and its own toolbar and gesture handling. Each of those produced a bug. Scrolling with page-sized jumps gets the same reading rhythm with none of that.

Android has no general pagination for native layouts. `StaticLayout`/`TextMeasurer` split plain text into lines, and the print framework (`PrintedPdfDocument`) leaves page layout to the app. The only built-in fragmentation for mixed content is a WebView with CSS columns, which is what EPUB readers use. With a native reader, page breaking is the app's job, as it is for Moon+ and KOReader's crengine.

## Scrollbar

`EInkScrollbar` follows the Windows 95 scrollbar's behavior, drawn in Material 3: arrows at each end move one line, a tap on the track above or below the thumb moves one page, and the thumb drags. It's one outlined capsule (`surfaceContainerLow` with an `outlineVariant` border), 28dp wide, since light grey fills disappear in the Monochrome theme.

- Reader: the arrows and track taps both turn a page like the tap zones and keys (see Page turns).
- Article list: page down makes the cut-off row the first row; page up moves a screen and aligns to a row. Arrows move one row. The thumb size and position are estimated from the average visible row height, since `LazyColumn` doesn't know the total height.
- The thin library scrollbar is hidden while the E Ink scrollbar shows.

## Page turns

Tap zones, page keys and scrollbar track taps all call the same `scrollBy`: the visible height (between the toolbars, when they aren't pinned) minus 80dp, floored at half the visible height for short screens. No animation, regardless of Reduce animations.

- The step follows EinkBro, which pages by `webView.height - 80dp` by default (`WebViewNavigationHelper.shiftOffset()`, "Page reserved height" setting). 80dp is about three lines of overlap at the default text size, which covers #1247. #1856 (a line too far) came from miscounted toolbar offsets, fixed in #1860, not from the step size.
- A page can end mid-line; the overlap means the full line shows at the top of the next page.
- At the end of an article, forward opens the next article; at the top, back opens the previous article scrolled to its end.
- A forward turn hides the toolbars and the system navigation bar.
- Images are capped at one screen tall, keeping their aspect ratio.

An earlier version snapped each turn to text lines and moved images whole to the next jump. It needed every text element and image to register its layout, and it produced uneven steps: short caption lines, crawling when an image sat just below the top, and a jump past the unregistered header. Plain `scrollBy` avoids all of that.

## Taps

Kindle's zones: a strip about half an inch wide on the left goes back, a band about 1.25" tall across the top opens the menus, and the rest (about 80%) goes forward. Links and images there open on tap.

Capy uses a left and right quarter. The edges always turn the page, even over a link or image: `pageTapZones` reads them in `PointerEventPass.Initial` and consumes the up, so the child's click never fires. Deferring to children made page taps easy to miss, since articles are dense with links and wide images. The middle half reads taps in `PointerEventPass.Final` and only acts on taps no child consumed, so links and images open there. A plain tap in the middle toggles the toolbars (and the system navigation bar), since page jumps don't scroll them back into view.

Drags past touch slop and long presses are ignored in every zone, so scrolling and text selection still work from the edges.

## Keys

`MainActivity` routes keys through `PageTurnKeys`, which dispatches to the last registered reader. Without a reader or with the setting off, keys pass through untouched, so volume works everywhere else.

- Volume and Page Up/Down are taken in `dispatchKeyEvent`, before views. Otherwise the window would change the volume.
- Arrow keys are handled in `onKeyDown`/`onKeyUp`, only when no view consumed them, so text fields keep cursor movement.
- Key repeats are consumed but don't turn more pages.

Bluetooth page turners send some mix of volume, Page Up/Down, or arrow keys, depending on the model and mode.

## Reduce animations

Compose reads a `MotionDurationScale` from the recomposer's coroutine context; the default follows the system "Remove animations" setting. `MainActivity` builds its window recomposer with `createLifecycleAwareWindowRecomposer(coroutineContext = AppMotionDurationScale)` and passes it to `setContent(parent = ...)`. The scale is 0 when Reduce animations is on and the system animator scale otherwise. Dialogs and popups compose under the same recomposer, so they inherit it.

This covers navigation transitions, the article-to-article `AnimatedContent`, toolbar show/hide, drawers and sheets, and switches. Indeterminate progress indicators show a still frame, the same as the system setting. Scroll flings keep their own scale and are unaffected.

Only `MainActivity` uses it. The share and add-link activities are small and still follow the system setting.

## Detection

`EInkDevice.isEInk` matches `Build.MANUFACTURER`/`BRAND` against known vendors (Onyx/Boox, Bigme, Meebook/Likebook, PocketBook, Ratta/Supernote) and `Build.MODEL` against "InkPalm" and "e-ink". This is a heuristic; a miss just means the user turns the settings on manually. `e_ink_defaults_applied` makes it one-shot, so turning a setting off sticks.

## Not done

- Full-screen flash to clear ghosting. The Onyx SDK (`EpdController`) is a vendor Maven dependency that has broken across Boox hardware; a manual black/white frame would avoid it.
- Keyboard shortcuts beyond paging (#1597).
