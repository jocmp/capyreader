package com.jocmp.basilreader.ui.articles

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basil.Feed
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture

@Composable
fun FeedRow(
    selected: Boolean,
    feed: Feed,
    onSelect: (id: String) -> Unit,
) {
    NavigationDrawerItem(
        label = { ListTitle(feed.name) },
        badge = { CountBadge(count = feed.count) },
        selected = selected,
        onClick = {
            onSelect(feed.id)
        }
    )
}

@Preview
@Composable
fun FeedRowPreview() {
    val feed = FeedPreviewFixture().values.take(1).first()

    MaterialTheme {
        FeedRow(
            feed = feed,
            onSelect = {},
            selected = false
        )
    }
}
