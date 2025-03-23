package com.capyreader.app.ui.articles


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.jocmp.capy.Feed

@Composable
fun RemoveFeedDialog(
    feed: Feed,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val title = stringResource(R.string.feed_action_unsubscribe_title)
    val message = stringResource(R.string.feed_action_unsubscribe_message, feed.title)
    val confirmText = stringResource(R.string.feed_action_unsubscribe_confirm)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(message) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        }
    )
}
