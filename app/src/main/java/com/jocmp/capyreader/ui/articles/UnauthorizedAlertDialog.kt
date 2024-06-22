package com.jocmp.capyreader.ui.articles

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jocmp.capyreader.R

@Composable
fun UnauthorizedAlertDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(stringResource(R.string.unauthorized_dialog_title))
        },
        text = {
           Text(stringResource(R.string.unauthorized_dialog_description))
        },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.unauthorized_dialog_dismiss_text))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.unauthorized_dialog_confirm_text))
            }
        }
    )
}
