# Card Layout

The article list has two layouts, chosen under Settings -> Article List -> Article Layout: List (the default row) and Cards. Origin: discussion #2284, which asked for Palabre/Newsify-style staggered cards.

## Staggered, not aligned rows

Cards render in a `LazyVerticalStaggeredGrid`. The first attempt used aligned rows with a fixed 16:9 image, following Material 3's Feed layout. It read like a table, so it was dropped. What makes the layout interesting is cards of different heights in independent columns.

Images keep their natural aspect ratio, clamped between 3:4 (tall) and 2:1 (wide). The ratio is only known once Coil loads the image, so a card starts at 16:9 and settles afterwards. Settled ratios are cached per URL (an LRU in `ArticleCard.kt`), so scrolling back up does not reflow. A failed image collapses to a text-only card.

Column count comes from the available width (`ArticleCardCells`): 180dp minimum card width, 8dp gutters, capped at 4. A phone gets 2 columns, a 50% tablet pane gets 3, and a full tablet gets 4.

## Reading order and mark-read-on-scroll

A staggered grid places each item in the shortest lane, so an item's top edge is never above an earlier item's top. That keeps two guarantees:

- Index order reads top to bottom.
- Every article before `firstVisibleItemIndex` is above the viewport.

Mark-read-on-scroll therefore uses the same `firstVisibleItemIndex - 1` rule in both layouts.

## Scroll state

`LazyColumn` and the staggered grid use different state types. `ArticleScrollState` wraps either one behind the surface the list features need: first visible index/offset, visible items (index, key, size), `isScrollInProgress`, and scroll/animate/request-scroll. Scroll-to-top, scroll-to-selected, mark-read-on-scroll, the new-articles indicator, the top-bar reset, and the filter-change reset all go through it.

`rememberArticleScrollState(layout)` keeps both states alive and returns the one for the current layout, including the empty-paging workaround from `LazyListStateExt.kt`.

The scrollbar library (nanihadesuka 2.2.0) has no staggered-grid variant, so Cards has no scrollbar. That includes the wide E Ink scrollbar.

## Tablet pane default

Windows at least 1200dp wide (the "large" width class: tablets in landscape, not foldables) start with the list/detail split at 50%. This only applies while `pane_expansion_anchor` is unset, and the initial anchor is not persisted until the user moves it. That keeps a tablet first opened in portrait from saving the narrow default. Installs that already stored an anchor keep it.
