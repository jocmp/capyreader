package com.capyreader.app.ui.articles

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.fixtures.FeedSample
import com.capyreader.app.ui.fixtures.FolderPreviewFixture
import com.capyreader.app.ui.navigationTitle
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.SavedSearch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedList(
    filter: ArticleFilter,
    statusCount: Long,
    folders: List<Folder> = emptyList(),
    feeds: List<Feed> = emptyList(),
    savedSearches: List<SavedSearch> = emptyList(),
    onFilterSelect: () -> Unit,
    onSelectSavedSearch: (search: SavedSearch) -> Unit,
    onRefreshAll: (completion: () -> Unit) -> Unit,
    onSelectFolder: (folder: Folder) -> Unit,
    onSelectFeed: (feed: Feed, folderTitle: String?) -> Unit,
    onFeedAdded: (feedID: String) -> Unit,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val articleStatus = filter.status

    var refreshing by remember { mutableStateOf(false) }
    val angle by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = ""
    )

    val refreshAll = {
        refreshing = true
        onRefreshAll {
            refreshing = false
        }
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
                    IconButton(onClick = { refreshAll() }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.feed_nav_drawer_refresh_all),
                            modifier = Modifier.graphicsLayer {
                                rotationZ = if (refreshing) angle else 0f
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

            if (savedSearches.isNotEmpty()) {
                FeedListDivider()
                ListHeadline(text = stringResource(R.string.nav_headline_saved_searches))

                savedSearches.forEach {
                    SavedSearchRow(
                        onSelect = onSelectSavedSearch,
                        selected = filter.isSavedSearchSelected(it),
                        savedSearch = it,
                    )
                }
            }

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
    val coroutineScope = rememberCoroutineScope()

    fun onRefresh(completion: () -> Unit) {
        coroutineScope.launch {
            delay(1500)
            completion()
        }
    }

    CapyTheme {
        FeedList(
            folders = folders,
            feeds = feeds,
            onSelectFolder = {},
            onSelectFeed = { _, _ -> },
            onNavigateToSettings = {},
            onRefreshAll = {
                onRefresh(it)
            },
            onFilterSelect = {},
            filter = ArticleFilter.default(),
            statusCount = 10,
            onFeedAdded = {},
            onSelectStatus = {},
            onSelectSavedSearch = {}
        )
    }
}
