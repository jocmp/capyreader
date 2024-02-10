package com.jocmp.basilreader.ui.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.Folder
import com.jocmp.basilreader.ui.fixtures.FolderPreviewFixture
import com.jocmp.basilreader.ui.theme.BasilReaderTheme
import kotlin.math.exp

@Composable
fun FolderRow(
    filter: ArticleFilter,
    folder: Folder,
    onFolderSelect: (folderTitle: String) -> Unit,
    onFeedSelect: (feedID: String) -> Unit,
) {
    val isFolderSelected = filter.isFolderSelect(folder)
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    val expandedIcon = if (expanded) {
        Icons.Filled.KeyboardArrowUp
    } else {
        Icons.Filled.KeyboardArrowDown
    }

    Column {
        NavigationDrawerItem(
            selected = isFolderSelected,
            onClick = { onFolderSelect(folder.title) },
            label = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = { setExpanded(!expanded) }) {
                        Icon(
                            imageVector = expandedIcon,
                            contentDescription = null
                        )
                    }
                    Text(
                        folder.title,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    Text(folder.count.toString())
                }
            }
        )
        AnimatedVisibility(
            expanded,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(animationSpec = tween()),
        ) {
            Column(Modifier.padding(start = 16.dp)) {
                folder.feeds.forEach { feed ->
                    FeedRow(
                        feed = feed,
                        onSelect = onFeedSelect,
                        selected = filter.isFeedSelected(feed),
                    )
                }
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
            onFeedSelect = {},
            filter = ArticleFilter.default()
        )
    }
}
