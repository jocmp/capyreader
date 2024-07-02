package com.jocmp.capyreader.ui.settings

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onRemoveAccount: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    SettingsView(
        refreshInterval = viewModel.refreshInterval,
        updateRefreshInterval = viewModel::updateRefreshInterval,
        accountName = viewModel.accountName,
        onNavigateBack = { onNavigateBack() },
        accountSource = viewModel.accountSource,
        onRequestRemoveAccount = {
            viewModel.removeAccount()
            onRemoveAccount()
        },
    )
}
