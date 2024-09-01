package com.capyreader.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.GetOPMLContent
import com.capyreader.app.transfers.OPMLExporter
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.opml.ImportProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountSettingsPanel(
    onRemoveAccount: () -> Unit,
    viewModel: AccountSettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(
        GetOPMLContent()
    ) { uri ->
        viewModel.startOPMLImport(uri = uri)
    }

    val importOPML = {
        picker.launch(listOf("text/xml", "text/x-opml", "application/*"))
    }

    val exportOPML = {
        coroutineScope.launch(Dispatchers.IO) {
            OPMLExporter(context = context).export(viewModel.account)
        }
    }

    AccountSettingsPanelView(
        onRequestRemoveAccount = {
            viewModel.removeAccount()
            onRemoveAccount()
        },
        onRequestImport = importOPML,
        onRequestExport = {
            exportOPML()
        },
        importProgress = viewModel.importProgress,
        accountSource = viewModel.accountSource,
        accountName = viewModel.accountName,
    )
}

@Composable
fun AccountSettingsPanelView(
    onRequestRemoveAccount: () -> Unit,
    onRequestImport: () -> Unit,
    onRequestExport: () -> Unit,
    accountSource: Source,
    accountName: String,
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
    ) {
        if (showAccountName(accountSource)) {
            FormSection(
                title = stringResource(R.string.settings_section_account),
            ) {
                RowItem {
                    Text(text = accountName)
                }
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
        }

        FormSection {
            RowItem {
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                Button(
                    onClick = { setRemoveDialogOpen(true) },
                    colors = removeAccountButtonColors(accountSource),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(strings.requestRemoveText))
                }
            }
        }
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
fun removeAccountButtonColors(source: Source) = when (source) {
    Source.LOCAL -> ButtonDefaults.buttonColors(
        containerColor = colorScheme.error,
        contentColor = colorScheme.onError
    )

    Source.FEEDBIN -> ButtonDefaults.buttonColors(
        containerColor = colorScheme.secondary,
        contentColor = colorScheme.onSecondary
    )
}

fun showAccountName(source: Source): Boolean {
    return source != Source.LOCAL
}

fun showImportButton(source: Source): Boolean {
    return source == Source.LOCAL
}

@Preview
@Composable
private fun AccountSettingsPanelViewPreview() {
    CapyTheme {
        AccountSettingsPanelView(
            onRequestRemoveAccount = {},
            onRequestImport = {},
            onRequestExport = {},
            accountSource = Source.FEEDBIN,
            accountName = "test@example.com",
            importProgress = null
        )
    }
}

@Preview
@Composable
private fun AccountSettingsPanelViewLocalPreview() {
    CapyTheme {
        AccountSettingsPanelView(
            onRequestRemoveAccount = {},
            onRequestImport = {},
            onRequestExport = {},
            accountSource = Source.LOCAL,
            accountName = "test@example.com",
            importProgress = ImportProgress(currentCount = 3, total = 9001)
        )
    }
}
