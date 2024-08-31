package com.capyreader.app.ui.settings

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.accounts.Source
import org.koin.compose.koinInject

@Composable
fun GeneralSettingsPanel(
    onRemoveAccount: () -> Unit,
    viewModel: GeneralSettingsViewModel = koinInject()
) {
    GeneralSettingsPanelView(
        accountSource = viewModel.accountSource,
        accountName = viewModel.accountName,
        refreshInterval = viewModel.refreshInterval,
        updateRefreshInterval = viewModel::updateRefreshInterval,
        canOpenLinksInternally = viewModel.canOpenLinksInternally,
        updateOpenLinksInternally = viewModel::updateOpenLinksInternally,
        updateAutoDelete = viewModel::updateAutoDelete,
        autoDelete = viewModel.autoDelete,
        onClearArticles = viewModel::clearAllArticles,
        onRemoveAccount = {
            viewModel.removeAccount()
            onRemoveAccount()
        }
    )
}

@Composable
fun GeneralSettingsPanelView(
    onRemoveAccount: () -> Unit,
    onClearArticles: () -> Unit,
    accountSource: Source,
    accountName: String,
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (RefreshInterval) -> Unit,
    canOpenLinksInternally: Boolean,
    updateOpenLinksInternally: (canOpenLinksInternally: Boolean) -> Unit,
    updateAutoDelete: (AutoDelete) -> Unit,
    autoDelete: AutoDelete,
) {
    val strings = AccountSettingsStrings.build(accountSource)
    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }
    val (isClearArticlesDialogOpen, setClearArticlesDialogOpen) = remember { mutableStateOf(false) }

    val onRemoveCancel = {
        setRemoveDialogOpen(false)
    }

    val onClearArticlesCancel = {
        setClearArticlesDialogOpen(false)
    }
    
    val onRequestClearArticles = {
        setClearArticlesDialogOpen(false)
        onClearArticles()
    }

    val onRequestRemove = {
        setRemoveDialogOpen(false)
        onRemoveAccount()
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

        FormSection(title = stringResource(R.string.settings_section_refresh)) {
            RowItem {
                RefreshIntervalMenu(
                    refreshInterval = refreshInterval,
                    updateRefreshInterval = updateRefreshInterval,
                )
            }
        }

        if (showCrashReporting()) {
            FormSection(title = stringResource(R.string.settings_section_privacy)) {
                RowItem {
                    CrashReportingCheckbox()
                }
            }
        }

        RowItem {
            TextSwitch(
                checked = canOpenLinksInternally,
                onCheckedChange = updateOpenLinksInternally,
                title = stringResource(R.string.settings_option_in_app_browser)
            )
        }

        FormSection(
            title = stringResource(R.string.settings_section_advanced)
        ) {
            RowItem {
                AutoDeleteMenu(
                    updateAutoDelete = updateAutoDelete,
                    autoDelete = autoDelete,
                )
            }

            RowItem {
                Button(
                    onClick = { setClearArticlesDialogOpen(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondary,
                        contentColor = colorScheme.onSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_clear_all_articles_button))
                }
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

    if (isClearArticlesDialogOpen) {
        AlertDialog(
            onDismissRequest = onClearArticlesCancel,
            text = { Text(stringResource(R.string.settings_clear_all_articles_text)) },
            dismissButton = {
                TextButton(onClick = onClearArticlesCancel) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = onRequestClearArticles) {
                    Text(text = stringResource(R.string.settings_clear_all_articles_confirm))
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
    return source == Source.FEEDBIN
}

@Preview
@Composable
private fun GeneralSettingsPanelPreview() {
    GeneralSettingsPanelView(
        onRemoveAccount = {},
        accountSource = Source.FEEDBIN,
        accountName = "test@example.com",
        refreshInterval = RefreshInterval.EVERY_HOUR,
        updateRefreshInterval = {},
        canOpenLinksInternally = false,
        onClearArticles = {},
        updateOpenLinksInternally = {},
        updateAutoDelete = {},
        autoDelete = AutoDelete.WEEKLY
    )
}
