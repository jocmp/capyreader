package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture

@Composable
fun FilterActionMenu(
    filter: ArticleFilter,
    onFeedEdit: (feedID: String) -> Unit,
    onFolderEdit: (folderTitle: String) -> Unit,
    onRemoveFeed: (feedID: String) -> Unit,
    onRemoveFolder: (folderTitle: String) -> Unit,
) {
    val (expanded, setMenuExpanded) = remember { mutableStateOf(false) }
    val (showRemoveDialog, setRemoveDialogOpen) = remember { mutableStateOf(false) }

    val onRequestRemove = {
        setRemoveDialogOpen(true)
    }

    val onRemove = {
        setRemoveDialogOpen(false)

        if (filter is ArticleFilter.Feeds) {
            onRemoveFeed(filter.feed.id)
        } else if (filter is ArticleFilter.Folders) {
            onRemoveFolder(filter.folder.title)
        }
    }

    Box {
        IconButton(onClick = { setMenuExpanded(true) }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.filter_action_menu_description)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { setMenuExpanded(false) },
        ) {
            if (filter is ArticleFilter.Feeds) {
                FeedActionMenuItems(
                    feed = filter.feed,
                    onMenuClose = { setMenuExpanded(false) },
                    onRequestRemove = onRequestRemove,
                    onEdit = onFeedEdit,
                )
            }

            if (filter is ArticleFilter.Folders) {
                FolderActionMenuItems(
                    folder = filter.folder,
                    onMenuClose = { setMenuExpanded(false) },
                    onRequestRemove = onRequestRemove,
                    onEdit = onFolderEdit,
                )
            }
        }

        if (showRemoveDialog) {
            RemoveDialog(
                filter = filter,
                onRemove = onRemove,
                onDismiss = { setRemoveDialogOpen(false) }
            )
        }
    }
}

@Preview
@Composable
fun FilterActionMenuPreview() {
    val feed = FeedPreviewFixture().values.first()

    FilterActionMenu(
        filter = ArticleFilter.Feeds(feed = feed, feedStatus = ArticleStatus.ALL),
        onFeedEdit = {},
        onFolderEdit = {},
        onRemoveFeed = {},
        onRemoveFolder = {}
    )
}
