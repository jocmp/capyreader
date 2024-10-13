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
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.capyreader.app.R
import com.capyreader.app.ui.components.safeEdgePadding
import com.capyreader.app.ui.fixtures.FeedSample
import com.capyreader.app.ui.fixtures.FolderPreviewFixture
import com.capyreader.app.ui.navigationTitle

@Composable
fun FeedList(
    filter: ArticleFilter,
    statusCount: Long,
    folders: List<Folder> = emptyList(),
    feeds: List<Feed> = emptyList(),
    onFilterSelect: () -> Unit,
    onSelectFolder: (folder: Folder) -> Unit,
    onSelectFeed: (feed: Feed) -> Unit,
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
        Modifier
            .safeEdgePadding()
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
                selected = filter.hasArticlesSelected(),
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
        onSelectFeed = {},
        onNavigateToSettings = {},
        onFilterSelect = {},
        filter = ArticleFilter.default(),
        statusCount = 10,
        onFeedAdded = {},
        onSelectStatus = {}
    )
}
