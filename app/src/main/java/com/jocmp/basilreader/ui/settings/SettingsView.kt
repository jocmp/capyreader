package com.jocmp.basilreader.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jocmp.basilreader.R
import com.jocmp.basilreader.refresher.RefreshInterval
import com.jocmp.basilreader.ui.LocalWindowWidth
import com.jocmp.basilreader.ui.isCompact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (interval: RefreshInterval) -> Unit,
    onNavigateBack: () -> Unit,
    onRequestLogout: () -> Unit,
    accountName: String
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }

    val onRemoveCancel = {
        setRemoveDialogOpen(false)
    }

    val onRemove = {
        setRemoveDialogOpen(false)
        onRequestLogout()
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(text = "Settings")
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigateBack() }
                    ) {
                        Icon(
                            imageVector = backButton(),
                            contentDescription = null
                        )
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxHeight()
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    Modifier
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Account",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = accountName)
                }

                Column(
                    Modifier
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    RefreshIntervalMenu(
                        refreshInterval = refreshInterval,
                        updateRefreshInterval = updateRefreshInterval,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { setRemoveDialogOpen(true) },
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_log_out_button))
                }
            }
        }
    }

    if (isRemoveDialogOpen) {
        AlertDialog(
            onDismissRequest = onRemoveCancel,
            title = { Text(stringResource(R.string.settings_logout_dialog_title)) },
            text = { Text(stringResource(R.string.settings_logout_dialog_message)) },
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

@Composable
private fun backButton(): ImageVector {
    val showBackArrow = LocalWindowWidth.current.isCompact

    return if (showBackArrow) {
        Icons.AutoMirrored.Filled.ArrowBack
    } else {
        Icons.Filled.Close
    }
}

@Preview
@Composable
fun AccountSettingsViewPreview() {
    SettingsView(
        refreshInterval = RefreshInterval.EVERY_HOUR,
        updateRefreshInterval = {},
        onRequestLogout = {},
        onNavigateBack = {},
        accountName = "hello@example.com"
    )
}
