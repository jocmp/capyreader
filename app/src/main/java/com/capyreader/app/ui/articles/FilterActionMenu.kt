package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.capyreader.app.R
import com.capyreader.app.ui.articles.list.MarkAllReadButton
import com.capyreader.app.ui.fixtures.FeedSample
import com.jocmp.capy.Feed

@Composable
fun FeedActions(
    onMarkAllRead: () -> Unit,
    onFeedEdited: () -> Unit,
    onRemoveFeed: (feedID: String) -> Unit,
    onEditFailure: (message: String) -> Unit,
    onRequestSearch: () -> Unit,
    hideSearchIcon: Boolean,
    feed: Feed?,
) {
    val (expanded, setMenuExpanded) = remember { mutableStateOf(false) }
    val (isEditDialogOpen, setEditDialogOpen) = rememberSaveable { mutableStateOf(false) }
    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }
    val editErrorMessage = stringResource(R.string.edit_feed_error)

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


            MarkAllReadButton(
                onMarkAllRead = onMarkAllRead,
            )

            if (feed != null) {
                IconButton(onClick = { setMenuExpanded(true) }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.filter_action_menu_description)
                    )
                }
            }
        }

        if (feed != null) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { setMenuExpanded(false) },
            ) {
                FeedActionMenuItems(
                    onEdit = { setEditDialogOpen(true) },
                    onRemoveRequest = { setRemoveDialogOpen(true) },
                    onMenuClose = { setMenuExpanded(false) },
                )
            }

            if (isRemoveDialogOpen) {
                RemoveDialog(
                    feed = feed,
                    onConfirm = {
                        setRemoveDialogOpen(false)
                        onRemoveFeed(feed.id)
                    },
                    onDismissRequest = { setRemoveDialogOpen(false) }
                )
            }

            EditFeedDialog(
                isOpen = isEditDialogOpen,
                feed = feed,
                onSuccess = {
                    onFeedEdited()
                },
                onDismiss = {
                    setEditDialogOpen(false)
                },
                onFailure = {
                    onEditFailure(editErrorMessage)
                }
            )
        }
    }
}

@Preview
@Composable
fun FeedActionsPreview(@PreviewParameter(FeedSample::class) feed: Feed) {
    FeedActions(
        onFeedEdited = {},
        onRemoveFeed = {},
        onEditFailure = {},
        onMarkAllRead = {},
        onRequestSearch = {},
        feed = feed,
        hideSearchIcon = false,
    )
}
