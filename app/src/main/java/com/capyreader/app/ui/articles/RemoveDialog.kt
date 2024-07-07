package com.capyreader.app.ui.articles


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jocmp.capy.Feed
import com.capyreader.app.R

@Composable
fun RemoveDialog(
    feed: Feed,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val state = RemoveDialogState(
        title = stringResource(R.string.feed_action_unsubscribe_title),
        message = stringResource(R.string.feed_action_unsubscribe_message, feed.title),
        confirmText = stringResource(R.string.feed_action_unsubscribe_confirm)
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(state.title) },
        text = { Text(state.message) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(state.confirmText)
            }
        }
    )
}


data class RemoveDialogState(
    val title: String,
    val message: String,
    val confirmText: String,
)
