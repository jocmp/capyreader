package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture
import com.jocmp.basilreader.ui.fixtures.FolderPreviewFixture

@Composable
fun FeedList(
    folders: List<Folder> = emptyList(),
    feeds: List<Feed> = emptyList(),
    onFeedAdd: () -> Unit,
    onSelectFolder: (folderTitle: String) -> Unit,
    onSelectFeed: (feedID: String) -> Unit,
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
                onFolderSelect = onSelectFolder,
                onFeedSelect = onSelectFeed,
            )
        }
        if (feeds.isNotEmpty()) {
            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Feeds", fontWeight = FontWeight.Bold)
            }
            feeds.forEach {
                FeedRow(
                    feed = it,
                    onSelect = onSelectFeed
                )
            }
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
        onSelectFolder = {},
        onSelectFeed = {}
    )
}
