package com.capyreader.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.capyreader.app.BuildConfig
import com.capyreader.app.R
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.jocmp.capy.accounts.AutoDelete
import org.koin.androidx.compose.koinViewModel

@Composable
fun GeneralSettingsPanel(
    viewModel: GeneralSettingsViewModel = koinViewModel()
) {
    GeneralSettingsPanelView(
        refreshInterval = viewModel.refreshInterval,
        updateRefreshInterval = viewModel::updateRefreshInterval,
        canOpenLinksInternally = viewModel.canOpenLinksInternally,
        updateOpenLinksInternally = viewModel::updateOpenLinksInternally,
        updateAutoDelete = viewModel::updateAutoDelete,
        autoDelete = viewModel.autoDelete,
        onClearArticles = viewModel::clearAllArticles,
    )
}

@Composable
fun GeneralSettingsPanelView(
    onClearArticles: () -> Unit,
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (RefreshInterval) -> Unit,
    canOpenLinksInternally: Boolean,
    updateOpenLinksInternally: (canOpenLinksInternally: Boolean) -> Unit,
    updateAutoDelete: (AutoDelete) -> Unit,
    autoDelete: AutoDelete,
) {
    val (isClearArticlesDialogOpen, setClearArticlesDialogOpen) = remember { mutableStateOf(false) }

    val onClearArticlesCancel = {
        setClearArticlesDialogOpen(false)
    }
    
    val onRequestClearArticles = {
        setClearArticlesDialogOpen(false)
        onClearArticles()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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

@Preview
@Composable
private fun GeneralSettingsPanelPreview() {
    GeneralSettingsPanelView(
        refreshInterval = RefreshInterval.EVERY_HOUR,
        updateRefreshInterval = {},
        canOpenLinksInternally = false,
        onClearArticles = {},
        updateOpenLinksInternally = {},
        updateAutoDelete = {},
        autoDelete = AutoDelete.WEEKLY
    )
}

fun showCrashReporting() = BuildConfig.FLAVOR == "gplay"
