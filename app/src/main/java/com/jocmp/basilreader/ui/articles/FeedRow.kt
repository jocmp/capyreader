package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basil.Feed
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture
import com.jocmp.basilreader.ui.theme.BasilReaderTheme

@Composable
fun FeedRow(
    selected: Boolean,
    feed: Feed,
    onSelect: (id: String) -> Unit,
) {
    NavigationDrawerItem(
        label = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(feed.name)
                Text(feed.count.toString())
            }
        },
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

    BasilReaderTheme {
        FeedRow(
            feed = feed,
            onSelect = {},
            selected = false
        )
    }
}
