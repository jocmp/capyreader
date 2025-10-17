package com.capyreader.app.ui.articles.list

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.capyreader.app.ui.articles.FeedActionMenuItems
import com.capyreader.app.ui.articles.RemoveFeedDialog
import com.capyreader.app.ui.articles.feeds.LocalFeedActions
import com.capyreader.app.ui.articles.feeds.edit.EditFeedDialog
import com.jocmp.capy.Feed
import com.jocmp.capy.common.launchUI

@Composable
fun FeedActionMenu(
    onDismissMenuRequest: () -> Unit,
    feed: Feed,
    expanded: Boolean,
) {
    val actions = LocalFeedActions.current
    val scope = rememberCoroutineScope()

    val (isEditDialogOpen, setEditDialogOpen) = rememberSaveable { mutableStateOf(false) }
    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }

    fun onToggleOpenInBrowser() {
        onDismissMenuRequest()
        scope.launchUI {
            actions.updateOpenInBrowser(feed.id, !feed.openArticlesInBrowser)
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissMenuRequest,
    ) {
        FeedActionMenuItems(
            feed = feed,
            onEdit = { setEditDialogOpen(true) },
            onRemoveRequest = { setRemoveDialogOpen(true) },
            onMenuClose = onDismissMenuRequest,
            onToggleOpenInBrowser = {
                onToggleOpenInBrowser()
            },
        )
    }

    if (isRemoveDialogOpen) {
        RemoveFeedDialog(
            feed = feed,
            onConfirm = {
                setRemoveDialogOpen(false)
                actions.removeFeed(feed.id)
            },
            onDismissRequest = { setRemoveDialogOpen(false) }
        )
    }

    EditFeedDialog(
        isOpen = isEditDialogOpen,
        feed = feed,
        onDismiss = {
            setEditDialogOpen(false)
        },
    )
}
