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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture

@Composable
fun FilterActionMenu(
    feed: Feed,
    folders: List<Folder>,
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

    val onRemove = {
        setRemoveDialogOpen(false)

        onRequestRemoveFeed(feed.id)
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
                onRemove = onRemove,
                onDismiss = { setRemoveDialogOpen(false) }
            )
        }

        if (showEditDialog) {
            EditFeedDialog(
                feed = feed,
                folders = folders,
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

@Preview
@Composable
fun FilterActionMenuPreview() {
    val feed = FeedPreviewFixture().values.first()

    FilterActionMenu(
        feed = feed,
        folders = emptyList(),
        onFeedEdited = {},
        onRequestRemoveFeed = {},
        onEditFailure = {}
    )
}
