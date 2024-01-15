package com.jocmp.basilreader.ui.articles


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jocmp.basil.ArticleFilter
import com.jocmp.basilreader.R

@Composable
fun RemoveDialog(
    filter: ArticleFilter,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val state = removeDialogState(filter) ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = { Text(state.message) },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onRemove) {
                Text(state.confirmText)
            }
        }
    )
}

@Composable
private fun removeDialogState(filter: ArticleFilter): RemoveDialogState? {
    return when (filter) {
        is ArticleFilter.Feeds -> RemoveDialogState(
            title = stringResource(R.string.feed_action_unsubscribe_title),
            message = stringResource(R.string.feed_action_unsubscribe_message, filter.feed.name),
            confirmText = stringResource(R.string.feed_action_unsubscribe_confirm)
        )

        is ArticleFilter.Folders -> RemoveDialogState(
            title = stringResource(R.string.folder_action_delete_title),
            message = stringResource(R.string.folder_action_delete_message, filter.folder.title),
            confirmText = stringResource(R.string.folder_action_delete_confirm)
        )

        else -> null
    }
}

data class RemoveDialogState(
    val title: String,
    val message: String,
    val confirmText: String,
)
