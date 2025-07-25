package com.capyreader.app.ui.articles.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.preferences.LayoutPreference
import com.capyreader.app.ui.articles.FilterActionMenu
import com.capyreader.app.ui.articles.FilterAppBarTitle
import com.capyreader.app.ui.components.ArticleSearch
import com.capyreader.app.ui.components.SearchTextField
import com.capyreader.app.ui.rememberLayoutPreference
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.SavedSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListTopBar(
    onRequestJumpToTop: () -> Unit,
    onNavigateToDrawer: () -> Unit,
    onRemoveFeed: (feedID: String, completion: (result: Result<Unit>) -> Unit) -> Unit,
    onRemoveFolder: (folderTitle: String, completion: (result: Result<Unit>) -> Unit) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    onMarkAllRead: () -> Unit,
    search: ArticleSearch,
    filter: ArticleFilter,
    currentFeed: Feed?,
    feeds: List<Feed>,
    savedSearches: List<SavedSearch>,
    folders: List<Folder>,
) {
    val enableSearch = search.isActive
    val layout = rememberLayoutPreference()

    val closeSearch = {
        search.clear()
    }

    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            if (enableSearch) {
                val focusRequester = remember { FocusRequester() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SearchTextField(
                        placeholder = { Text(stringResource(R.string.search_bar_placeholder)) },
                        value = search.query.orEmpty(),
                        onValueChange = {
                            search.update(it)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            IconButton(onClick = closeSearch) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    )
                }

                LaunchedEffect(search.isActive) {
                    if (search.isActive) {
                        focusRequester.requestFocus()
                    }
                }
            } else {
                FilterAppBarTitle(
                    filter = filter,
                    allFeeds = feeds,
                    allFolders = folders,
                    allSavedSearches = savedSearches,
                    onRequestJumpToTop = onRequestJumpToTop
                )
            }
        },
        navigationIcon = {
            if (enableSearch) {
                IconButton(
                    onClick = {
                        search.clear()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.feed_list_top_bar_close_search)
                    )
                }
            } else if (layout != LayoutPreference.SINGLE) {
                IconButton(
                    onClick = onNavigateToDrawer
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            FilterActionMenu(
                filter = filter,
                currentFeed = currentFeed,
                onRemoveFeed = onRemoveFeed,
                onRemoveFolder = onRemoveFolder,
                onRequestSearch = { search.start() },
                onMarkAllRead = { onMarkAllRead() },
                hideSearchIcon = enableSearch,
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
        onRemoveFeed = { _, _ -> },
        onRemoveFolder = { _, _ -> },
        scrollBehavior = scrollBehavior,
        onMarkAllRead = {},
        search = ArticleSearch(),
        filter = ArticleFilter.default(),
        currentFeed = null,
        feeds = listOf(),
        savedSearches = emptyList(),
        folders = emptyList()
    )
}
