package com.capyreader.app.ui.articles.list

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.articles.feeds.edit.EditFeedDialog
import com.capyreader.app.ui.articles.FeedActionMenuItems
import com.capyreader.app.ui.articles.RemoveFeedDialog
import com.capyreader.app.ui.settings.localSnackbarDisplay
import com.jocmp.capy.Feed

@Composable
fun FeedActionMenu(
    onDismissMenuRequest: () -> Unit,
    feed: Feed,
    onRemoveRequest: (feedID: String, completion: (result: Result<Unit>) -> Unit) -> Unit,
    expanded: Boolean,
) {
    val editSuccessMessage = stringResource(R.string.feed_action_edit_success)
    val editErrorMessage = stringResource(R.string.edit_feed_error)
    val unsubscribeErrorMessage = stringResource(R.string.unsubscribe_error)

    val showSnackbar = localSnackbarDisplay()

    val (isEditDialogOpen, setEditDialogOpen) = rememberSaveable { mutableStateOf(false) }
    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }

    val onRemoveComplete = { result: Result<Unit> ->
        if (result.isFailure) {
            showSnackbar(unsubscribeErrorMessage)
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissMenuRequest,
    ) {
        FeedActionMenuItems(
            onEdit = { setEditDialogOpen(true) },
            onRemoveRequest = { setRemoveDialogOpen(true) },
            onMenuClose = onDismissMenuRequest,
        )
    }

    if (isRemoveDialogOpen) {
        RemoveFeedDialog(
            feed = feed,
            onConfirm = {
                setRemoveDialogOpen(false)
                onRemoveRequest(feed.id) {
                    onRemoveComplete(it)
                }
            },
            onDismissRequest = { setRemoveDialogOpen(false) }
        )
    }

    EditFeedDialog(
        isOpen = isEditDialogOpen,
        feed = feed,
        onSuccess = {
            showSnackbar(editSuccessMessage)
        },
        onDismiss = {
            setEditDialogOpen(false)
        },
        onFailure = {
            showSnackbar(editErrorMessage)
        }
    )
}
