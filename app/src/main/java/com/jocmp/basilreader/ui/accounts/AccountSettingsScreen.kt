package com.jocmp.basilreader.ui.accounts

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.jocmp.basil.Account
import com.jocmp.basilreader.OPMLExporter
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel = koinViewModel(),
    goBack: () -> Unit,
) {
    val context = LocalContext.current

    AccountSettingsView(
        defaultDisplayName = viewModel.displayName,
        removeAccount = {
            viewModel.removeAccount()
            goBack()
        },
        submit = viewModel::submitName,
        exportOPML = {
            context.exportOPML(account = viewModel.account)
        }
    )
}
