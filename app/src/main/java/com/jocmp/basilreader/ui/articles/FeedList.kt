package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture
import com.jocmp.basilreader.ui.fixtures.FolderPreviewFixture

@Composable
fun FeedList(
    filter: ArticleFilter,
    folders: List<Folder> = emptyList(),
    feeds: List<Feed> = emptyList(),
    onAddFeed: () -> Unit,
    onFilterSelect: () -> Unit,
    onSelectFolder: (folderTitle: String) -> Unit,
    onSelectFeed: (feedID: String) -> Unit,
    onNavigateToAccounts: () -> Unit,
    articleStatus: ArticleStatus,
) {
    val scrollState = rememberScrollState()

    Column(Modifier.verticalScroll(scrollState)) {
        Row {
            Button(onClick = onAddFeed) {
                Text("+ Feed")
            }
            IconButton(
                onClick = {
                    onNavigateToAccounts()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = stringResource(R.string.settings_action)
                )
            }
        }

        NavigationDrawerItem(
            label = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(articleStatus.navigationTitle))
                }
            },
            selected = filter.areArticlesSelected(),
            onClick = {
                onFilterSelect()
            }
        )

        folders.forEach {
            FolderRow(
                folder = it,
                onFolderSelect = onSelectFolder,
                onFeedSelect = onSelectFeed,
                filter = filter
            )
        }
        if (feeds.isNotEmpty()) {
            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Feeds", fontWeight = FontWeight.Bold)
            }
            feeds.forEach {
                FeedRow(
                    feed = it,
                    onSelect = onSelectFeed,
                    selected = filter.isFeedSelected(it),
                )
            }
        }
    }
}

val ArticleStatus.navigationTitle: Int
    get() = when (this) {
        ArticleStatus.ALL -> R.string.filter_all
        ArticleStatus.UNREAD -> R.string.filter_unread
        ArticleStatus.STARRED -> R.string.filter_starred
    }

@Preview
@Composable
fun FeedListPreview() {
    val folders = FolderPreviewFixture().values.take(2).toList()
    val feeds = FeedPreviewFixture().values.take(2).toList()

    FeedList(
        folders = folders,
        feeds = feeds,
        onAddFeed = {},
        onSelectFolder = {},
        onSelectFeed = {},
        onNavigateToAccounts = {},
        onFilterSelect = {},
        articleStatus = ArticleStatus.ALL,
        filter = ArticleFilter.default(),
    )
}
