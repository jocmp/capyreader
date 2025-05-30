package com.capyreader.app.ui.settings.sharing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.components.PasswordField
import com.capyreader.app.ui.components.UrlField
import com.capyreader.app.ui.components.UsernameField
import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.sharing.services.ReadeckService
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReadeckLoginView(
    service: ReadeckService = ReadeckService(),
    viewModel: ReadeckLoginViewModel = koinViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val (username, setUsername) = remember { mutableStateOf(service.username) }
    val (password, setPassword) = remember { mutableStateOf(service.password) }
    val (serverURL, setServerURL) = remember { mutableStateOf(service.serverURL) }

    val submit = {
        keyboardController?.hide()

        coroutineScope.launch {
            withIOContext {
                viewModel.submit(
                    username = password,
                    password = password,
                    serverURL = serverURL
                )
            }
        }
    }

    Column {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            UsernameField(
                value = username,
                isError = viewModel.errors.contains("username"),
                onChange = {
                    viewModel.errors.remove("username")
                    setUsername(it)
                },
            )
            PasswordField(
                isError = viewModel.errors.contains("password"),
                onChange = {
                    viewModel.errors.remove("password")
                    setPassword(it)
                },
                value = password,
            )
            UrlField(
                isError = viewModel.errors.contains("server_url"),
                onChange = {
                    viewModel.errors.remove("server_url")
                    setServerURL(it)
                },
                value = serverURL,
            )
        }
        Column(
            Modifier.padding(top = 16.dp)
        ) {
            Button(
                enabled = !viewModel.loading,
                onClick = {
                    submit()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sharing_fields_save_button))
            }
        }
    }
}
