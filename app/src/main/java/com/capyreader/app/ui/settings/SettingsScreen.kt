package com.capyreader.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.transfers.OPMLExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onRemoveAccount: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.startOPMLImport(uri = uri)
    }

    val importOPML = {
        picker.launch("text/xml")
    }

    val exportOPML = {
        coroutineScope.launch(Dispatchers.IO) {
            OPMLExporter(context = context).export(viewModel.account)
        }
    }

    SettingsView(
        refreshInterval = viewModel.refreshInterval,
        updateRefreshInterval = viewModel::updateRefreshInterval,
        onNavigateBack = { onNavigateBack() },
        onRequestRemoveAccount = {
            viewModel.removeAccount()
            onRemoveAccount()
        },
        onRequestExport = { exportOPML() },
        onRequestImport = {
            importOPML()
        },
        canOpenLinksInternally = viewModel.canOpenLinksInternally,
        updateOpenLinksInternally = viewModel::updateOpenLinksInternally,
        accountSource = viewModel.accountSource,
        accountName = viewModel.accountName,
        importProgress = viewModel.importProgress,
        updateTheme = viewModel::updateTheme,
        theme = viewModel.theme
    )
}
