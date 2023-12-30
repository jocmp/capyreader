package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.Folder
import com.jocmp.basilreader.ui.fixtures.FolderPreviewFixture
import com.jocmp.basilreader.ui.theme.BasilReaderTheme

@Composable
fun FolderRow(
    folder: Folder,
    onFolderSelect: (folderTitle: String) -> Unit,
    onFeedSelect: (feedID: String) -> Unit
) {
    Column {
        Box(modifier = Modifier.clickable { onFolderSelect(folder.title) }) {
            Text(
                folder.title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
        folder.feeds.forEach { feed ->
            Row {
                FeedRow(feed = feed, onSelect = onFeedSelect)
            }
        }
    }
}

@Preview
@Composable
fun FolderRowPreview() {
    val folder = FolderPreviewFixture().values.take(1).first()

    BasilReaderTheme {
        FolderRow(
            folder = folder,
            onFolderSelect = {},
            onFeedSelect = {}
        )
    }
}
