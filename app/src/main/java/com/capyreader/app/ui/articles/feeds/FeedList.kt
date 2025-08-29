package com.capyreader.app.ui.articles.feeds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.FeedGroup
import com.capyreader.app.ui.articles.AddFeedButton
import com.capyreader.app.ui.articles.ArticleStatusBar
import com.capyreader.app.ui.articles.ArticleStatusIcon
import com.capyreader.app.ui.articles.CountBadge
import com.capyreader.app.ui.articles.ListTitle
import com.capyreader.app.ui.articles.SavedSearchRow
import com.capyreader.app.ui.fixtures.FeedSample
import com.capyreader.app.ui.fixtures.FolderPreviewFixture
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.capyreader.app.ui.navigationTitle
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.SavedSearch

@Composable
fun FeedList(
    filter: ArticleFilter,
    statusCount: Long,
    folders: List<Folder> = emptyList(),
    feeds: List<Feed> = emptyList(),
    savedSearches: List<SavedSearch> = emptyList(),
    onFilterSelect: () -> Unit,
    onSelectSavedSearch: (search: SavedSearch) -> Unit,
    refreshState: AngleRefreshState,
    onRefresh: () -> Unit,
    onSelectFolder: (folder: Folder) -> Unit,
    onSelectFeed: (feed: Feed, folderTitle: String?) -> Unit,
    onFeedAdded: (feedID: String) -> Unit,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val articleStatus = filter.status
    val scrollState = rememberScrollState()
    val buttonState = rememberRefreshButtonState(refreshState)

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
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.feed_nav_drawer_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(
                            vertical = 18.dp,
                            horizontal = 12.dp
                        ),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigateToSettings() }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                    IconButton(onClick = {
                        onRefresh()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.feed_nav_drawer_refresh_all),
                            modifier = Modifier.graphicsLayer {
                                rotationZ = buttonState.iconRotation
                            }
                        )
                    }
                    AddFeedButton(
                        iconOnly = true,
                        onComplete = {
                            onFeedAdded(it)
                        }
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
                selected = filter.hasArticlesSelected(),
                onClick = {
                    onFilterSelect()
                }
            )

            Spacer(Modifier.height(8.dp))

            if (savedSearches.isNotEmpty()) {
                FeedListDivider()
                FeedGroupList(FeedGroup.SAVED_SEARCHES) {
                    savedSearches.forEach {
                        SavedSearchRow(
                            onSelect = onSelectSavedSearch,
                            selected = filter.isSavedSearchSelected(it),
                            savedSearch = it,
                        )
                    }
                }
            }

            if (folders.isNotEmpty()) {
                FeedListDivider()
                FeedGroupList(FeedGroup.FOLDERS) {
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
                }
            }

            if (feeds.isNotEmpty()) {
                FeedListDivider()
                FeedGroupList(FeedGroup.FEEDS) {
                    feeds.forEach { feed ->
                        FeedRow(
                            feed = feed,
                            onSelect = {
                                onSelectFeed(it, null)
                            },
                            selected = filter.isFeedSelected(feed),
                        )
                    }
                }
            }

            Box(Modifier.padding(vertical = 16.dp))
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            ArticleStatusBar(
                status = filter.status,
                onSelectStatus = onSelectStatus,
            )
        }
    }
}

@Composable
private fun FeedListDivider() {
    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
}

@Preview
@Composable
fun FeedListPreview() {
    val folders = FolderPreviewFixture().values.take(2).toList()
    val feeds = FeedSample().values.take(2).toList()

    PreviewKoinApplication {
        CapyTheme {
            FeedList(
                folders = folders,
                feeds = feeds,
                onSelectFolder = {},
                onSelectFeed = { _, _ -> },
                onNavigateToSettings = {},
                onRefresh = {},
                onFilterSelect = {},
                filter = ArticleFilter.default(),
                statusCount = 10,
                onFeedAdded = {},
                onSelectStatus = {},
                onSelectSavedSearch = {},
                refreshState = AngleRefreshState.STOPPED,
            )
        }
    }
}
