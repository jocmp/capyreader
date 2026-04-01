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
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.FeedGroup
import com.capyreader.app.ui.articles.AddFeedButton
import com.capyreader.app.ui.articles.ArticleStatusIcon
import com.capyreader.app.ui.articles.CountBadge
import com.capyreader.app.ui.articles.ListTitle
import com.capyreader.app.ui.articles.SavedSearchRow
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.capyreader.app.ui.folderNavTitle
import com.capyreader.app.ui.savedSearchNavTitle
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.accounts.Source

@Composable
fun FeedList(
    source: Source,
    filter: ArticleFilter,
    statusCount: Long,
    todayCount: Long,
    starredCount: Long,
    folders: List<Folder> = emptyList(),
    feeds: List<Feed> = emptyList(),
    readLaterFeed: Feed? = null,
    savedSearches: List<SavedSearch> = emptyList(),
    onFilterSelect: () -> Unit,
    onSelectToday: () -> Unit,
    onSelectStarred: () -> Unit,
    onSelectSavedSearch: (search: SavedSearch) -> Unit,
    refreshState: AngleRefreshState,
    onRefresh: () -> Unit,
    onSelectFolder: (folder: Folder) -> Unit,
    onSelectFeed: (feed: Feed, folderTitle: String?) -> Unit,
    onFeedAdded: (feedID: String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
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
                Icon(
                    painterResource(R.drawable.capy_icon_small),
                    contentDescription = null,
                    modifier = Modifier.padding(
                        vertical = 18.dp,
                        horizontal = 16.dp
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
                    IconButton(onClick = { onRefresh() }) {
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
                        onComplete = { onFeedAdded(it) }
                    )
                }
            }
            DrawerItem(
                icon = {
                    Icon(
                        Icons.Rounded.Today,
                        contentDescription = null
                    )
                },
                label = {
                    ListTitle(
                        stringResource(R.string.filter_today),
                    )
                },
                badge = { CountBadge(count = todayCount) },
                selected = filter.hasTodaySelected(),
                onClick = {
                    onSelectToday()
                }
            )

            DrawerItem(
                icon = { ArticleStatusIcon(status = ArticleStatus.UNREAD) },
                label = {
                    ListTitle(
                        stringResource(R.string.filter_unread),
                    )
                },
                badge = { CountBadge(count = statusCount) },
                selected = filter.hasArticlesSelected(),
                onClick = {
                    onFilterSelect()
                }
            )

            DrawerItem(
                icon = {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null
                    )
                },
                label = {
                    ListTitle(
                        stringResource(R.string.filter_starred),
                    )
                },
                badge = { CountBadge(count = starredCount) },
                selected = filter.hasStarredSelected(),
                onClick = {
                    onSelectStarred()
                }
            )

            if (readLaterFeed != null) {
                DrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Bookmark,
                            contentDescription = null
                        )
                    },
                    label = {
                        ListTitle(
                            stringResource(R.string.filter_read_later),
                        )
                    },
                    badge = { CountBadge(count = readLaterFeed.count) },
                    selected = filter.isFeedSelected(readLaterFeed),
                    onClick = {
                        onSelectFeed(readLaterFeed, null)
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            if (savedSearches.isNotEmpty()) {
                FeedListDivider()
                FeedGroupList(
                    type = FeedGroup.SAVED_SEARCHES,
                    title = stringResource(source.savedSearchNavTitle),
                ) {
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
                FeedGroupList(
                    type = FeedGroup.FOLDERS,
                    title = stringResource(source.folderNavTitle)
                ) {
                    folders.forEach { folder ->
                        FolderRow(
                            folder = folder,
                            onFolderSelect = onSelectFolder,
                            onFeedSelect = { feed ->
                                onSelectFeed(feed, folder.title)
                            },
                            filter = filter,
                            source = source,
                        )
                    }
                }
            }

            if (feeds.isNotEmpty()) {
                FeedListDivider()
                FeedGroupList(
                    type = FeedGroup.FEEDS,
                    title = stringResource(R.string.nav_headline_feeds),
                ) {
                    feeds.forEach { feed ->
                        FeedRow(
                            feed = feed,
                            onSelect = {
                                onSelectFeed(it, null)
                            },
                            selected = filter.isFeedSelected(feed),
                            source = source,
                        )
                    }
                }
            }

            Box(Modifier.padding(vertical = 16.dp))
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
    PreviewKoinApplication {
        CapyTheme {
            FeedList(
                source = Source.LOCAL,
                onSelectFolder = {},
                onSelectFeed = { _, _ -> },
                onNavigateToSettings = {},
                onRefresh = {},
                onFilterSelect = {},
                onSelectToday = {},
                onSelectStarred = {},
                filter = ArticleFilter.default(),
                statusCount = 10,
                todayCount = 5,
                starredCount = 3,
                onFeedAdded = {},
                onSelectSavedSearch = {},
                refreshState = AngleRefreshState.STOPPED,
            )
        }
    }
}

