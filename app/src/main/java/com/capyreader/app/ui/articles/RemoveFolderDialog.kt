package com.capyreader.app.ui.articles

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R

@Composable
fun RemoveFolderDialog(
    folderTitle: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val title = stringResource(R.string.tag_action_delete_title)
    val message = stringResource(R.string.tag_action_delete_message, folderTitle)
    val confirmText = stringResource(R.string.tag_action_delete_confirm)

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
