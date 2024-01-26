package com.jocmp.basilreader.ui.accounts

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val TAG = "AccountSettingsScreen"

@Composable
fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel = koinViewModel(),
    goBack: () -> Unit,
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        Log.d(TAG, "importOPML: start $uri")

        if (uri != null) {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                viewModel.importOPML(inputStream) {
                    Log.d(TAG, "importOPML: success $uri")
                }
            }
        }
    }

    AccountSettingsView(
        defaultDisplayName = viewModel.displayName,
        removeAccount = {
            viewModel.removeAccount()
            goBack()
        },
        submit = viewModel::submitName,
        exportOPML = {
            context.exportOPML(account = viewModel.account)
        },
        importOPML = {
            picker.launch("text/xml")
        }
    )
}
