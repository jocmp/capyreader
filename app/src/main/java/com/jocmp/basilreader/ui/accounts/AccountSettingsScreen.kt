package com.jocmp.basilreader.ui.accounts

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel = koinViewModel(),
    goBack: () -> Unit,
) {
    AccountSettingsView(
        defaultDisplayName = viewModel.displayName,
        removeAccount = {
            viewModel.removeAccount()
            goBack()
        },
        submit = viewModel::submitName
    )
}
