package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jocmp.basil.Feed

@Composable
fun FeedRow(
    feed: Feed,
    onSelect: (id: String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect(feed.id)
            }
    ) {
        Text(
            "${feed.name} (${feed.feedURL})",
            modifier = Modifier.padding(8.dp)
        )
    }
}
