package com.jocmp.basilreader.ui.articles


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.Feed
import com.jocmp.basilreader.R

@Composable
fun RemoveDialog(
    feed: Feed,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val state = removeDialogState(feed = feed)

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
private fun removeDialogState(feed: Feed): RemoveDialogState {
    return  RemoveDialogState(
        title = stringResource(R.string.feed_action_unsubscribe_title),
        message = stringResource(R.string.feed_action_unsubscribe_message, feed.title),
        confirmText = stringResource(R.string.feed_action_unsubscribe_confirm)
    )
}

data class RemoveDialogState(
    val title: String,
    val message: String,
    val confirmText: String,
)
