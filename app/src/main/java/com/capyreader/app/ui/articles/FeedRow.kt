package com.capyreader.app.ui.articles

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.capy.Feed
import com.capyreader.app.ui.fixtures.FeedPreviewFixture

@Composable
fun FeedRow(
    selected: Boolean,
    feed: Feed,
    onSelect: (feed: Feed) -> Unit,
) {
    NavigationDrawerItem(
        icon = { FaviconBadge(url = feed.faviconURL) },
        label = { ListTitle(feed.title) },
        badge = { CountBadge(count = feed.count) },
        selected = selected,
        onClick = {
            onSelect(feed)
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
