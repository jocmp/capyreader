package com.capyreader.app.ui.settings.panels

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.GetOPMLContent
import com.capyreader.app.common.RowItem
import com.capyreader.app.common.titleKey
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.transfers.OPMLExporter
import com.capyreader.app.transfers.StarredExporter
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.settings.AccountSettingsStrings
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.opml.ImportProgress
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountSettingsPanel(
    onRemoveAccount: () -> Unit,
    viewModel: AccountSettingsViewModel = koinViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lastRefreshedAt by viewModel.lastRefreshedAt.collectAsState()

    val importer = rememberLauncherForActivityResult(
        GetOPMLContent()
    ) { uri ->
        viewModel.startOPMLImport(uri = uri)
    }

    val opmlExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/xml")
    ) { uri ->
        coroutineScope.launch {
            OPMLExporter(context).export(viewModel.account, target = uri)
        }
    }

    val starredExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        coroutineScope.launch {
            StarredExporter(context).export(viewModel.account, target = uri)
        }
    }

    AccountSettingsPanelView(
        onRequestRemoveAccount = {
            viewModel.removeAccount()
            onRemoveAccount()
        },
        onRequestImport = {
            importer.launch(listOf("text/xml", "text/x-opml", "application/*"))
        },
        onRequestExport = {
            opmlExporter.launch(OPMLExporter.DEFAULT_FILE_NAME)
        },
        onRequestStarredExport = {
            starredExporter.launch(StarredExporter.DEFAULT_FILE_NAME)
        },
        importProgress = viewModel.importProgress,
        accountSource = viewModel.accountSource,
        accountURL = viewModel.accountURL,
        accountName = viewModel.accountName,
        lastRefreshedAt = lastRefreshedAt,
    )
}

@Composable
fun AccountSettingsPanelView(
    onRequestRemoveAccount: () -> Unit,
    onRequestImport: () -> Unit,
    onRequestExport: () -> Unit,
    onRequestStarredExport: () -> Unit,
    accountSource: Source,
    accountURL: String,
    accountName: String,
    lastRefreshedAt: LastRefreshed,
    importProgress: ImportProgress?,
) {
    val strings = AccountSettingsStrings.build(accountSource)
    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }

    val onRequestRemove = {
        setRemoveDialogOpen(false)
        onRequestRemoveAccount()
    }

    val onRemoveCancel = {
        setRemoveDialogOpen(false)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        if (showAccountName(accountSource)) {
            FormSection(
                title = stringResource(accountSource.titleKey),
            ) {
                RowItem {
                    Text(text = accountName)
                }
            }
            if (accountURL.isNotBlank()) {
                FormSection(
                    title = stringResource(R.string.settings_section_account_server),
                ) {
                    RowItem {
                        Text(
                            text = accountURL,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        FormSection(
            title = stringResource(R.string.settings_section_refresh),
        ) {
            RowItem {
                Text(
                    text = lastRefreshed(lastRefreshedAt),
                )
            }
        }

        if (showImportButton(accountSource)) {
            FormSection(title = stringResource(R.string.settings_section_import)) {
                RowItem {
                    OPMLImportButton(
                        onClick = {
                            onRequestImport()
                        },
                        importProgress = importProgress
                    )
                }
            }
        }

        FormSection(title = stringResource(R.string.settings_section_export)) {
            RowItem {
                OPMLExportButton(
                    onClick = onRequestExport,
                )
            }
            RowItem {
                StarredExportButton(
                    onClick = onRequestStarredExport,
                )
            }
        }

        FormSection {
            RowItem {
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                RemoveAccountButton(
                    source = accountSource,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { setRemoveDialogOpen(true) },
                ) {
                    Text(stringResource(strings.requestRemoveText))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (isRemoveDialogOpen) {
        AlertDialog(
            onDismissRequest = onRemoveCancel,
            title = { Text(stringResource(strings.dialogTitle)) },
            text = { Text(stringResource(strings.dialogMessage)) },
            dismissButton = {
                TextButton(onClick = onRemoveCancel) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = onRequestRemove) {
                    Text(text = stringResource(strings.dialogConfirmText))
                }
            }
        )
    }
}

@Composable
fun RemoveAccountButton(
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
            ), onClick = onClick, modifier = modifier, content = content
        )
    } else {
        FilledTonalButton(onClick = onClick, modifier = modifier, content = content)
    }
}

fun showAccountName(source: Source): Boolean {
    return source != Source.LOCAL
}

fun showImportButton(source: Source): Boolean {
    return source == Source.LOCAL
}

@Composable
private fun lastRefreshed(lastRefreshed: LastRefreshed): String {
    return when (lastRefreshed) {
        is LastRefreshed.Never -> stringResource(R.string.settings_account_never_refreshed)
        is LastRefreshed.Today -> stringResource(R.string.settings_account_refresh_value_today, lastRefreshed.time)
        is LastRefreshed.Past -> stringResource(R.string.settings_account_refresh_value, lastRefreshed.date, lastRefreshed.time)
    }
}

@Preview
@Composable
private fun AccountSettingsPanelViewPreview() {
    CapyTheme(appTheme = AppTheme.NEWSPRINT) {
        AccountSettingsPanelView(
            onRequestRemoveAccount = {},
            onRequestImport = {},
            onRequestExport = {},
            onRequestStarredExport = {},
            accountSource = Source.FEEDBIN,
            accountURL = "",
            accountName = "test@example.com",
            lastRefreshedAt = LastRefreshed.from(1700000000L),
            importProgress = null
        )
    }
}

@Preview
@Composable
private fun AccountSettingsPanelViewLocalPreview() {
    CapyTheme(appTheme = AppTheme.NEWSPRINT) {
        AccountSettingsPanelView(
            onRequestRemoveAccount = {},
            onRequestImport = {},
            onRequestExport = {},
            onRequestStarredExport = {},
            accountURL = "",
            accountSource = Source.LOCAL,
            accountName = "test@example.com",
            lastRefreshedAt = LastRefreshed.Never,
            importProgress = ImportProgress(currentCount = 3, total = 9001)
        )
    }
}
