# Card Layout

The article list has two layouts, chosen under Settings -> Article List -> Article Layout: List (the default row) and Cards. Origin: discussion #2284, which asked for Palabre's staggered card grid.

## Aligned rows, not masonry

Cards follow Material 3's "Feed" canonical layout: equal-width columns, a fixed 16:9 image, and every card in a row stretched to the tallest one. Staggered (Pinterest-style) masonry was rejected because columns fill out of order, which breaks reading order and makes mark-read-on-scroll ambiguous. Palabre's rotating 1/1.5/2-unit spans were also dropped as dated.

Column count comes from the list pane width (`cardColumnCount`): 180dp minimum card width, 8dp gutters, capped at 4. A 411dp phone gets 2 columns, a 50% tablet pane gets 3, and a full tablet gets 4.

## One LazyColumn item per row

Cards render as rows of `n` articles inside the same `LazyColumn`, so `LazyListState` and the scrollbar stay unchanged. The trade-off is that a list index is a row, not an article. `ArticleColumns` holds the mapping, and anything that converts between the two must go through it:

- `ScrollToSelectedArticleEffect`: article -> row
- `MarkReadOnScrollEffect`: the row scrolled past -> its last article, clamped to the item count
- `rememberNewArticlesIndicator`: the first visible row -> its first article
- Row keys use the id of the row's first article, so the indicator's key comparison still holds

In List mode the count is 1 and every mapping is the identity.

The hoisted `ArticleColumns` is the only column count, and it is saveable. `ArticleColumnsEffect` keeps it in sync:

- The target count comes from a `derivedStateOf` over the measured width, so dragging the pane only recomposes when the count changes.
- It remembers the first visible article (the anchor). When the count changes or the paging snapshot changes, it requests the anchor's row in a `SideEffect`, before layout. This mirrors `LazyColumn`'s key anchoring, which a row-keyed list loses whenever articles are prepended.
- While the list is empty, `articles.rememberLazyListState()` hands out a throwaway state. A restored count waits for real data before remapping, so the restored scroll index is converted from the units it was saved in.

Equal row heights come from `IntrinsicSize.Min`. That only works because the swipe box (saket/swipe) is a plain `Box`; wrapping cards in anything built on `SubcomposeLayout` will crash intrinsic measurement.

## Tablet pane default

Windows at least 1200dp wide (the "large" width class: tablets in landscape, not foldables) start with the list/detail split at 50%. This only applies while `pane_expansion_anchor` is unset, and the initial anchor is no longer persisted until the user moves it. That keeps a tablet first opened in portrait from saving the narrow default. Installs that already stored an anchor keep it.
