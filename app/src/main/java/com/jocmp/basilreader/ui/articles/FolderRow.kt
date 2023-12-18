package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basil.Folder

@Composable
fun FolderRow(
    folder: Folder,
    onFeedSelect: (feedID: String) -> Unit
) {
    Text(folder.title)
    folder.feeds.forEach { feed ->
        Row {
            Text("- ")
            FeedRow(feed = feed, onSelect = onFeedSelect)
        }
    }
}
