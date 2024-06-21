package com.jocmp.capyreader.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
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
import com.jocmp.capyreader.ui.articles.list.MarkAllReadButton
import com.jocmp.capyreader.ui.fixtures.FeedPreviewFixture

@Composable
fun FeedActions(
    feed: Feed?,
    onMarkAllRead: () -> Unit,
    onFeedEdited: () -> Unit,
    onRemoveFeed: (feedID: String) -> Unit,
    onEditFailure: (message: String) -> Unit,
) {
    val (expanded, setMenuExpanded) = remember { mutableStateOf(false) }
    val (isEditDialogOpen, setEditDialogOpen) = rememberSaveable { mutableStateOf(false) }
    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }
    val editErrorMessage = stringResource(R.string.edit_feed_error)

    Box {
        Row {
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

            if (isEditDialogOpen) {
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
        onRemoveFeed = {},
        onEditFailure = {},
        onMarkAllRead = {},
    )
}
