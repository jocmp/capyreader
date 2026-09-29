package com.capyreader.app.ui.articles.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.articles.FilterActionMenu
import com.capyreader.app.ui.articles.FilterAppBarTitle
import com.capyreader.app.ui.components.ArticleSearch
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.accounts.Source

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListTopBar(
    onRequestJumpToTop: () -> Unit,
    onNavigateToDrawer: () -> Unit,
    onRemoveFolder: (folderTitle: String, completion: (result: Result<Unit>) -> Unit) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    search: ArticleSearch,
    filter: ArticleFilter,
    currentFeed: Feed?,
    feeds: List<Feed>,
    savedSearches: List<SavedSearch>,
    folders: List<Folder>,
    source: Source,
) {
    // Search runs in its own full-surface overlay (see SearchView), so the list top bar only needs
    // to launch it; the active-search field/back-arrow used to live here.
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            FilterAppBarTitle(
                filter = filter,
                allFeeds = feeds,
                allFolders = folders,
                allSavedSearches = savedSearches,
                onRequestJumpToTop = onRequestJumpToTop
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateToDrawer
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = null
                )
            }
        },
        actions = {
            FilterActionMenu(
                filter = filter,
                currentFeed = currentFeed,
                onRemoveFolder = onRemoveFolder,
                onRequestSearch = { search.start() },
                hideSearchIcon = false,
                source = source,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun FeedListTopBarPreview() {
    val scrollBehavior = pinnedScrollBehavior()
    ArticleListTopBar(
        onRequestJumpToTop = { },
        onNavigateToDrawer = { },
        onRemoveFolder = { _, _ -> },
        scrollBehavior = scrollBehavior,
        search = ArticleSearch(),
        filter = ArticleFilter.default(),
        currentFeed = null,
        feeds = listOf(),
        savedSearches = emptyList(),
        folders = emptyList(),
        source = Source.LOCAL,
    )
}
