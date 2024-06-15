package com.jocmp.capyreader.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.capy.Feed
import com.jocmp.capyreader.R
import com.jocmp.capyreader.ui.fixtures.FeedPreviewFixture

@Composable
fun FeedActions(
    feed: Feed?,
    onMarkAllRead: () -> Unit,
    onFeedEdited: () -> Unit,
    onRequestRemoveFeed: (feedID: String) -> Unit,
    onEditFailure: (message: String) -> Unit,
) {
    val (expanded, setMenuExpanded) = remember { mutableStateOf(false) }
    val (showRemoveDialog, setRemoveDialogOpen) = remember { mutableStateOf(false) }
    val (showEditDialog, setEditDialogOpen) = rememberSaveable { mutableStateOf(false) }
    val editErrorMessage = stringResource(R.string.edit_feed_error)

    val onRequestRemove = {
        setRemoveDialogOpen(true)
    }

    Box {
        Row {
            IconButton(onClick = { onMarkAllRead() }) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.action_mark_all_read)
                )
            }

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
                    feedID = feed.id,
                    onMenuClose = { setMenuExpanded(false) },
                    onRequestRemove = onRequestRemove,
                    onEdit = { setEditDialogOpen(true) },
                )
            }

            if (showRemoveDialog) {
                RemoveDialog(
                    feed = feed,
                    onRemove = {
                        setRemoveDialogOpen(false)

                        onRequestRemoveFeed(feed.id)
                    },
                    onDismiss = { setRemoveDialogOpen(false) }
                )
            }

            if (showEditDialog) {
                EditFeedDialog(
                    feed = feed,
                    onSubmit = {
                        setEditDialogOpen(false)
                        onFeedEdited()
                    },
                    onCancel = {
                        setEditDialogOpen(false)
                    },
                    onFailure = {
                        setEditDialogOpen(false)
                        onEditFailure(editErrorMessage)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun FeedActionsPreview() {
    val feed = FeedPreviewFixture().values.first()

    FeedActions(
        feed = feed,
        onFeedEdited = {},
        onRequestRemoveFeed = {},
        onEditFailure = {},
        onMarkAllRead = {},
    )
}
