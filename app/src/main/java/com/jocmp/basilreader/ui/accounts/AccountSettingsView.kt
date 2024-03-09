package com.jocmp.basilreader.ui.accounts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basilreader.R
import com.jocmp.basilreader.refresher.RefreshInterval

@Composable
fun AccountSettingsView(
    defaultDisplayName: String,
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (interval: RefreshInterval) -> Unit,
    removeAccount: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val saveMessage = stringResource(id = R.string.account_settings_save_name)
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    val (displayName, setDisplayName) = remember { mutableStateOf(defaultDisplayName) }
    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }

    val onRemoveCancel = {
        setRemoveDialogOpen(false)
    }

    val onRemove = {
        setRemoveDialogOpen(false)
        removeAccount()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { contentPadding ->
        Column(Modifier.padding(contentPadding)) {
            RefreshIntervalMenu(
                refreshInterval = refreshInterval,
                updateRefreshInterval = updateRefreshInterval,
            )

            Button(onClick = { setRemoveDialogOpen(true) }) {
                Text(stringResource(R.string.account_settings_delete_account_button))
            }
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
        refreshInterval = RefreshInterval.EVERY_HOUR,
        updateRefreshInterval = {}
    )
}
