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
import com.capyreader.app.ui.articles.FeedActions
import com.capyreader.app.ui.articles.FilterAppBarTitle
import com.capyreader.app.ui.components.ArticleSearch
import com.capyreader.app.ui.components.SearchTextField
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedListTopBar(
    onRequestJumpToTop: () -> Unit,
    onNavigateToDrawer: () -> Unit,
    onRequestSnackbar: (message: String) -> Unit,
    onRemoveFeed: (feedID: String, onSuccess: () -> Unit, onFailure: () -> Unit) -> Unit,
    onSearchQueryChange: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    onMarkAllRead: (MarkRead) -> Unit,
    search: ArticleSearch,
    filter: ArticleFilter,
    currentFeed: Feed?,
    feeds: List<Feed>,
    allFolders: List<Folder>,
) {
    val editSuccessMessage = stringResource(R.string.feed_action_edit_success)
    val unsubscribeMessage = stringResource(R.string.feed_action_unsubscribe_success)
    val unsubscribeErrorMessage = stringResource(R.string.unsubscribe_error)
    val enableSearch = search.isActive

    val openSearch = {
        search.update("")
    }

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
                        value = search.query ?: "",
                        onValueChange = {
                            search.update(it)
                            onSearchQueryChange()
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

                LaunchedEffect(search.isInitialized) {
                    if (search.isInitialized) {
                        focusRequester.requestFocus()
                    }
                }
            } else {
                FilterAppBarTitle(
                    filter = filter,
                    allFeeds = feeds,
                    allFolders = allFolders,
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
            } else {
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
            FeedActions(
                onMarkAllRead = {
                    onMarkAllRead(MarkRead.All)
                },
                onFeedEdited = {
                    onRequestSnackbar(editSuccessMessage)
                },
                onRemoveFeed = { feedID ->
                    onRemoveFeed(
                        feedID,
                        {
                            onRequestSnackbar(unsubscribeMessage)
                        },
                        {
                            onRequestSnackbar(unsubscribeErrorMessage)
                        }
                    )
                },
                onEditFailure = onRequestSnackbar,
                onRequestSearch = openSearch,
                hideSearchIcon = enableSearch,
                feed = currentFeed,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun FeedListTopBarPreview() {
    val scrollBehavior = pinnedScrollBehavior()
    FeedListTopBar(
        onRequestJumpToTop = { },
        onNavigateToDrawer = { },
        onRequestSnackbar = {},
        onRemoveFeed = { _, _, _ -> },
        onSearchQueryChange = {},
        scrollBehavior = scrollBehavior,
        onMarkAllRead = {},
        search = ArticleSearch(),
        filter = ArticleFilter.default(),
        currentFeed = null,
        feeds = listOf(),
        allFolders = emptyList()
    )
}
