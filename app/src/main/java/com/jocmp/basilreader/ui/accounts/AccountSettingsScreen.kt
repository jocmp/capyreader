package com.jocmp.basilreader.ui.accounts

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jocmp.basilreader.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel = koinViewModel()
) {
    val initialName = viewModel.account.displayName.get()
    val (displayName, setDisplayName) = remember { mutableStateOf(initialName) }

    Column {
        TextField(
            value = displayName,
            onValueChange = setDisplayName,
            placeholder = { Text(initialName) }
        )

        Button(
            onClick = {
                viewModel.submitName(displayName)
            }
        ) {
            Text(stringResource(R.string.account_settings_submit))
        }
    }
}
