package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture
import com.jocmp.basilreader.ui.fixtures.FolderPreviewFixture

@Composable
fun FeedList(
    folders: List<Folder> = emptyList(),
    feeds: List<Feed> = emptyList(),
    onFeedAdd: () -> Unit,
    onFeedSelect: (feedID: String) -> Unit
) {
    Column {
        Row {
            Button(onClick = onFeedAdd) {
                Text("+ Feed")
            }
        }
        folders.forEach {
            FolderRow(
                folder = it,
                onFeedSelect = onFeedSelect
            )
        }
        feeds.forEach {
            FeedRow(
                feed = it,
                onSelect = onFeedSelect
            )
        }
    }
}

@Preview
@Composable
fun FeedListPreview() {
    val folders = FolderPreviewFixture().values.take(2).toList()
    val feeds = FeedPreviewFixture().values.take(2).toList()

    FeedList(
        folders = folders,
        feeds = feeds,
        onFeedAdd = {},
        onFeedSelect = {}
    )
}
