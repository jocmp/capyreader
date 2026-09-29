package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import com.capyreader.app.BuildConfig
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.ui.settings.CrashReportingCheckbox
import org.koin.androidx.compose.koinViewModel

@Composable
fun AutoDeleteRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    AutoDeleteMenu(
        updateAutoDelete = viewModel::updateAutoDelete,
        autoDelete = viewModel.autoDelete,
    )
}

@Composable
fun ClearArticlesRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    val (isDialogOpen, setDialogOpen) = remember { mutableStateOf(false) }

    RowItem {
        FilledTonalButton(
            onClick = { setDialogOpen(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings_clear_all_articles_button))
        }
    }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { setDialogOpen(false) },
            text = { Text(stringResource(R.string.settings_clear_all_articles_text)) },
            dismissButton = {
                TextButton(onClick = { setDialogOpen(false) }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        setDialogOpen(false)
                        viewModel.clearAllArticles()
                    }
                ) {
                    Text(text = stringResource(R.string.settings_clear_all_articles_confirm))
                }
            }
        )
    }
}

@Composable
fun CrashReportingRow() {
    RowItem {
        CrashReportingCheckbox()
    }
}

@Composable
fun CrashLogsRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    CrashLogExportItem(source = viewModel.source)
}

@Composable
fun TestNotificationSettingRow() {
    if (!BuildConfig.DEBUG || LocalView.current.isInEditMode) {
        return
    }

    TestNotificationRow()
}
