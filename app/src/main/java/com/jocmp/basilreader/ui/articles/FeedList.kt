package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture
import com.jocmp.basilreader.ui.fixtures.FolderPreviewFixture
import com.jocmp.basilreader.ui.navigationTitle

@Composable
fun FeedList(
    filter: ArticleFilter,
    statusCount: Long,
    folders: List<Folder> = emptyList(),
    feeds: List<Feed> = emptyList(),
    onAddFeed: () -> Unit,
    onFilterSelect: () -> Unit,
    onSelectFolder: (folderTitle: String) -> Unit,
    onSelectFeed: (feedID: String) -> Unit,
    onNavigateToAccounts: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val articleStatus = filter.status

    Column(Modifier.verticalScroll(scrollState)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            OutlinedButton(
                onClick = { onAddFeed() },
            ) {
                Box(Modifier.padding(end = 8.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(stringResource(R.string.nav_add_feed))
            }
            IconButton(onClick = { onNavigateToAccounts() }) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = stringResource(R.string.accounts_action)
                )
            }
        }

        NavigationDrawerItem(
            icon = { ArticleStatusIcon(status = articleStatus) },
            label = {
                ListTitle(
                    stringResource(articleStatus.navigationTitle),
                )
            },
            badge = { CountBadge(count = statusCount) },
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
            HorizontalDivider(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            )
            ListHeadline(text = stringResource(R.string.nav_headline_feeds))
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
        filter = ArticleFilter.default(),
        statusCount = 10,
    )
}
