package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.capyreader.app.R
import com.capyreader.app.ui.LocalMarkAllReadButtonPosition
import com.capyreader.app.ui.articles.list.FeedActionMenu
import com.capyreader.app.ui.articles.list.FolderActionMenu
import com.capyreader.app.ui.articles.list.MarkAllReadButton
import com.capyreader.app.ui.fixtures.FeedSample
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.Source

@Composable
fun FilterActionMenu(
    filter: ArticleFilter,
    currentFeed: Feed?,
    onMarkAllRead: () -> Unit,
    onRemoveFolder: (folderTitle: String, completion: (result: Result<Unit>) -> Unit) -> Unit,
    onRequestSearch: () -> Unit,
    hideSearchIcon: Boolean,
    source: Source,
) {
    val markReadPosition = LocalMarkAllReadButtonPosition.current
    val (expanded, setMenuExpanded) = remember(filter) { mutableStateOf(false) }

    val closeMenu = {
        setMenuExpanded(false)
    }

    Box {
        Row {
            if (!hideSearchIcon) {
                IconButton(onClick = onRequestSearch) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(R.string.filter_action_menu_search_articles)
                    )
                }
            }

            if (markReadPosition == MarkReadPosition.TOOLBAR) {
                MarkAllReadButton(
                    onMarkAllRead = {
                        onMarkAllRead()
                    },
                )
            }

            Box {
                if (currentFeed != null || filter is ArticleFilter.Folders) {
                    IconButton(onClick = { setMenuExpanded(true) }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.filter_action_menu_description)
                        )
                    }
                }

                if (currentFeed != null) {
                    FeedActionMenu(
                        expanded = expanded,
                        feed = currentFeed,
                        onDismissMenuRequest = { closeMenu() },
                    )
                }

                if (filter is ArticleFilter.Folders) {
                    FolderActionMenu(
                        expanded = expanded,
                        folderTitle = filter.folderTitle,
                        onDismissMenuRequest = { closeMenu() },
                        onRemoveRequest = onRemoveFolder,
                        source = source,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun FeedActionsPreview(@PreviewParameter(FeedSample::class) feed: Feed) {
    FilterActionMenu(
        onRemoveFolder = { _, _ -> },
        onMarkAllRead = {},
        onRequestSearch = {},
        currentFeed = feed,
        filter = ArticleFilter.Feeds(
            feedID = feed.id,
            folderTitle = null,
            feedStatus = ArticleStatus.ALL
        ),
        hideSearchIcon = false,
        source = Source.LOCAL,
    )
}
