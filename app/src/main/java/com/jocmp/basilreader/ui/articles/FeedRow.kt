package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.Feed
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture
import com.jocmp.basilreader.ui.theme.BasilReaderTheme

@Composable
fun FeedRow(
    feed: Feed,
    onSelect: (id: String) -> Unit
) {
    Box(
        modifier = Modifier
            .clickable {
                onSelect(feed.id)
            }
    ) {
        Text(
            "${feed.name} (${feed.feedURL})",
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun FeedRowPreview() {
    val feed = FeedPreviewFixture().values.take(1).first()

    BasilReaderTheme {
        FeedRow(
            feed = feed,
            onSelect = {}
        )
    }
}
