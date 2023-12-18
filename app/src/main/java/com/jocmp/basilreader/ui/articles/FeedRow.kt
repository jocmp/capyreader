package com.jocmp.basilreader.ui.articles

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.jocmp.basil.Feed

@Composable
fun FeedRow(
    feed: Feed,
    onSelect: (id: String) -> Unit
) {
    Text("${feed.name} (${feed.feedURL})")
}
