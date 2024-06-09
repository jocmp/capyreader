package com.jocmp.basilreader.ui.settings

import androidx.compose.runtime.Composable
import com.jocmp.basilreader.accountModule
import com.jocmp.basilreader.unloadAccountModules
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onLogout: () -> Unit,
) {
    SettingsView(
        defaultDisplayName = viewModel.displayName,
        refreshInterval = viewModel.refreshInterval,
        updateRefreshInterval = viewModel::updateRefreshInterval,
        logOut = {
            viewModel.logOut()
            onLogout()
        },
    )
}
