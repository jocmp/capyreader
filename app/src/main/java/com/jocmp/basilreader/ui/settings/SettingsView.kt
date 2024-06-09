package com.jocmp.basilreader.ui.settings

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
fun SettingsView(
    defaultDisplayName: String,
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (interval: RefreshInterval) -> Unit,
    logOut: () -> Unit,
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
        logOut()
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
                Text(stringResource(R.string.settings_log_out_button))
            }
        }
    }

    if (isRemoveDialogOpen) {
        AlertDialog(
            onDismissRequest = onRemoveCancel,
            title = { Text(stringResource(R.string.settings_logout_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.settings_logout_dialog_message,
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
                    Text(stringResource(R.string.settings_logout_submit))
                }
            }
        )
    }
}

@Preview
@Composable
fun AccountSettingsViewPreview() {
    SettingsView(
        defaultDisplayName = "Feedbin",
        logOut = {},
        refreshInterval = RefreshInterval.EVERY_HOUR,
        updateRefreshInterval = {}
    )
}
