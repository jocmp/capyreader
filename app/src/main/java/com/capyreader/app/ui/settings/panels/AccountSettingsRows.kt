package com.capyreader.app.ui.settings.panels

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.GetOPMLContent
import com.capyreader.app.common.RowItem
import com.capyreader.app.common.titleKey
import com.capyreader.app.transfers.OPMLExporter
import com.capyreader.app.transfers.StarredExporter
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.settings.AccountSettingsStrings
import com.jocmp.capy.accounts.Source
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountNameRow(viewModel: AccountSettingsViewModel = koinViewModel()) {
    ListItem(
        headlineContent = { Text(stringResource(viewModel.accountSource.titleKey)) },
        supportingContent = { Text(viewModel.accountName) },
    )
}

@Composable
fun AccountServerRow(viewModel: AccountSettingsViewModel = koinViewModel()) {
    if (viewModel.accountURL.isBlank()) {
        return
    }

    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_section_account_server)) },
        supportingContent = {
            Text(
                text = viewModel.accountURL,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
fun RefreshIntervalRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    RefreshIntervalMenu(
        refreshInterval = viewModel.refreshInterval,
        updateRefreshInterval = viewModel::updateRefreshInterval,
    )
}

@Composable
fun RefreshOnWiFiOnlyRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            checked = viewModel.refreshOnWiFiOnly,
            onCheckedChange = viewModel::updateRefreshOnWiFiOnly,
            title = stringResource(R.string.settings_refresh_on_wifi_only),
        )
    }
}

@Composable
fun LastRefreshedRow(viewModel: AccountSettingsViewModel = koinViewModel()) {
    val lastRefreshedAt by viewModel.lastRefreshedAt.collectAsState()

    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_section_refresh)) },
        supportingContent = { Text(lastRefreshed(lastRefreshedAt)) },
    )
}

@Composable
fun OPMLImportRow(viewModel: AccountSettingsViewModel = koinViewModel()) {
    val importer = rememberLauncherForActivityResult(GetOPMLContent()) { uri ->
        viewModel.startOPMLImport(uri = uri)
    }

    RowItem {
        OPMLImportButton(
            onClick = {
                importer.launch(listOf("text/xml", "text/x-opml", "application/*"))
            },
            importProgress = viewModel.importProgress,
        )
    }
}

@Composable
fun OPMLExportRow(viewModel: AccountSettingsViewModel = koinViewModel()) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/xml")
    ) { uri ->
        coroutineScope.launch {
            OPMLExporter(context).export(viewModel.account, target = uri)
        }
    }

    RowItem {
        OPMLExportButton(
            onClick = { exporter.launch(OPMLExporter.DEFAULT_FILE_NAME) },
        )
    }
}

@Composable
fun StarredExportRow(viewModel: AccountSettingsViewModel = koinViewModel()) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        coroutineScope.launch {
            StarredExporter(context).export(viewModel.account, target = uri)
        }
    }

    RowItem {
        StarredExportButton(
            onClick = { exporter.launch(StarredExporter.DEFAULT_FILE_NAME) },
        )
    }
}

@Composable
fun RemoveAccountRow(
    onRemoveAccount: () -> Unit,
    viewModel: AccountSettingsViewModel = koinViewModel(),
) {
    val source = viewModel.accountSource
    val strings = AccountSettingsStrings.build(source)
    val (isDialogOpen, setDialogOpen) = remember { mutableStateOf(false) }

    RowItem {
        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
        RemoveAccountButton(
            source = source,
            modifier = Modifier.fillMaxWidth(),
            onClick = { setDialogOpen(true) },
        ) {
            Text(stringResource(strings.requestRemoveText))
        }
    }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { setDialogOpen(false) },
            title = { Text(stringResource(strings.dialogTitle)) },
            text = { Text(stringResource(strings.dialogMessage)) },
            dismissButton = {
                TextButton(onClick = { setDialogOpen(false) }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        setDialogOpen(false)
                        viewModel.removeAccount()
                        onRemoveAccount()
                    }
                ) {
                    Text(text = stringResource(strings.dialogConfirmText))
                }
            }
        )
    }
}

@Composable
private fun RemoveAccountButton(
    source: Source,
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable RowScope.() -> Unit
) {
    if (source == Source.LOCAL) {
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.error,
                contentColor = colorScheme.contentColorFor(colorScheme.error)
            ),
            onClick = onClick,
            modifier = modifier,
            content = content,
        )
    } else {
        FilledTonalButton(onClick = onClick, modifier = modifier, content = content)
    }
}

@Composable
private fun lastRefreshed(lastRefreshed: LastRefreshed): String {
    return when (lastRefreshed) {
        is LastRefreshed.Never -> stringResource(R.string.settings_account_never_refreshed)
        is LastRefreshed.Today -> stringResource(R.string.settings_account_refresh_value_today, lastRefreshed.time)
        is LastRefreshed.Past -> stringResource(R.string.settings_account_refresh_value, lastRefreshed.date, lastRefreshed.time)
    }
}
