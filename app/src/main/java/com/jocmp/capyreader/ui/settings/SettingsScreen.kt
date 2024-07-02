package com.jocmp.capyreader.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.jocmp.capyreader.transfers.OPMLExporter
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

    val exportOPML = {
        coroutineScope.launch(Dispatchers.IO) {
            OPMLExporter(context = context).export(viewModel.account)
        }
    }

    SettingsView(
        refreshInterval = viewModel.refreshInterval,
        updateRefreshInterval = viewModel::updateRefreshInterval,
        accountName = viewModel.accountName,
        onNavigateBack = { onNavigateBack() },
        accountSource = viewModel.accountSource,
        onRequestExport = { exportOPML() },
        onRequestRemoveAccount = {
            viewModel.removeAccount()
            onRemoveAccount()
        },
    )
}
