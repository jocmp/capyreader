package com.jocmp.basilreader.ui.accounts

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basilreader.R

@Composable
fun AccountSettingsView(
    defaultDisplayName: String,
    removeAccount: () -> Unit,
    submit: (displayName: String) -> Unit,
) {
    val (displayName, setDisplayName) = remember { mutableStateOf(defaultDisplayName) }
    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }

    val onRemoveCancel = {
        setRemoveDialogOpen(false)
    }

    val onRemove = {
        setRemoveDialogOpen(false)
        removeAccount()
    }

    Column {
        TextField(
            value = displayName,
            onValueChange = setDisplayName,
            placeholder = { Text(defaultDisplayName) }
        )

        Button(
            onClick = {
                submit(displayName)
            }
        ) {
            Text(stringResource(R.string.account_settings_submit))
        }

        Button(onClick = { setRemoveDialogOpen(true) }) {
            Text(stringResource(R.string.account_settings_delete_account_button))
        }
    }

    if (isRemoveDialogOpen) {
        AlertDialog(
            onDismissRequest = onRemoveCancel,
            title = { Text(stringResource(R.string.account_settings_delete_account_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.account_settings_delete_account_message,
                        displayName
                    )
                )
            },
            dismissButton = {
                TextButton(onClick = onRemoveCancel) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = onRemove) {
                    Text(stringResource(R.string.account_settings_delete_account_submit))
                }
            }
        )
    }
}

@Preview
@Composable
fun AccountSettingsViewPreview() {
    AccountSettingsView(
        defaultDisplayName = "Feedbin",
        removeAccount = {},
        submit = {}
    )
}
