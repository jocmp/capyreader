package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
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
    onFilterSelect: () -> Unit,
    onSelectFolder: (folderTitle: String) -> Unit,
    onSelectFeed: (feedID: String) -> Unit,
    onFeedAdded: (feedID: String) -> Unit,
    onStatusSelect: (status: ArticleStatus) -> Unit,
    onNavigateToAccounts: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val (isMenuExpanded, setMenuExpanded) = remember { mutableStateOf(false) }
    val articleStatus = filter.status

    val onStatusChange = { status: ArticleStatus ->
        setMenuExpanded(false)
        onStatusSelect(status)
    }

    Column(
        Modifier
            .fillMaxSize()
    ) {
        Column(
            Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp)
                .weight(1f)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                TextButton(
                    onClick = { setMenuExpanded(true) },
                ) {
                    Text(
                        stringResource(articleStatus.navigationTitle),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Box(Modifier.padding(start = 8.dp)) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                        )
                    }
                }
                ArticleFilterMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { setMenuExpanded(false) },
                    onSelect = onStatusChange,
                )
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

            if (folders.isNotEmpty()) {
                FeedListDivider()
                ListHeadline(text = stringResource(R.string.nav_headline_tags))
            }

            folders.forEach {
                FolderRow(
                    folder = it,
                    onFolderSelect = onSelectFolder,
                    onFeedSelect = onSelectFeed,
                    filter = filter
                )
            }

            if (folders.isNotEmpty() && feeds.isNotEmpty()) {
                FeedListDivider()
            }

            if (feeds.isNotEmpty()) {
                ListHeadline(text = stringResource(R.string.nav_headline_feeds))
            }

            feeds.forEach {
                FeedRow(
                    feed = it,
                    onSelect = onSelectFeed,
                    selected = filter.isFeedSelected(it),
                )
            }

            Box(Modifier.padding(vertical = 16.dp))
        }

        Surface(
            shadowElevation = 2.dp,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = { onNavigateToAccounts() }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.accounts_action)
                    )
                }
                AddFeedButton(
                    onComplete = {
                        onFeedAdded(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun FeedListDivider() {
    HorizontalDivider(
        Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
    )
}

@Preview
@Composable
fun FeedListPreview() {
    val folders = FolderPreviewFixture().values.take(2).toList()
    val feeds = FeedPreviewFixture().values.take(2).toList()

    FeedList(
        folders = folders,
        feeds = feeds,
        onSelectFolder = {},
        onSelectFeed = {},
        onNavigateToAccounts = {},
        onFilterSelect = {},
        filter = ArticleFilter.default(),
        statusCount = 10,
        onFeedAdded = {},
        onStatusSelect = {}
    )
}
