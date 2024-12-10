package com.capyreader.app.ui.articles

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
import com.capyreader.app.R
import com.capyreader.app.ui.fixtures.FeedSample
import com.capyreader.app.ui.fixtures.FolderPreviewFixture
import com.capyreader.app.ui.navigationTitle
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder

@Composable
fun FeedList(
    filter: ArticleFilter,
    statusCount: Long,
    folders: List<Folder> = emptyList(),
    feeds: List<Feed> = emptyList(),
    onFilterSelect: () -> Unit,
    onSelectFolder: (folder: Folder) -> Unit,
    onSelectFeed: (feed: Feed, folderTitle: String?) -> Unit,
    onFeedAdded: (feedID: String) -> Unit,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val (isMenuExpanded, setMenuExpanded) = remember { mutableStateOf(false) }
    val articleStatus = filter.status

    val onStatusChange = { status: ArticleStatus ->
        setMenuExpanded(false)
        onSelectStatus(status)
    }

    Column(
        Modifier.fillMaxSize()
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
                selected = filter.hasArticlesSelected(),
                onClick = {
                    onFilterSelect()
                }
            )

            if (folders.isNotEmpty()) {
                FeedListDivider()
                ListHeadline(text = stringResource(R.string.nav_headline_tags))
            }

            folders.forEach { folder ->
                FolderRow(
                    folder = folder,
                    onFolderSelect = onSelectFolder,
                    onFeedSelect = { feed ->
                        onSelectFeed(feed, folder.title)
                    },
                    filter = filter
                )
            }

            if (folders.isNotEmpty() && feeds.isNotEmpty()) {
                FeedListDivider()
            }

            if (feeds.isNotEmpty()) {
                ListHeadline(text = stringResource(R.string.nav_headline_feeds))
            }

            feeds.forEach { feed ->
                FeedRow(
                    feed = feed,
                    onSelect = {
                        onSelectFeed(it, null)
                    },
                    selected = filter.isFeedSelected(feed),
                )
            }

            Box(Modifier.padding(vertical = 16.dp))
        }

        HorizontalDivider()

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { onNavigateToSettings() }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings)
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
    val feeds = FeedSample().values.take(2).toList()

    FeedList(
        folders = folders,
        feeds = feeds,
        onSelectFolder = {},
        onSelectFeed = { _, _ -> },
        onNavigateToSettings = {},
        onFilterSelect = {},
        filter = ArticleFilter.default(),
        statusCount = 10,
        onFeedAdded = {},
        onSelectStatus = {}
    )
}
