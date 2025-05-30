package com.capyreader.app.ui.accounts

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.components.ErrorAlert
import com.capyreader.app.ui.components.PasswordField
import com.capyreader.app.ui.components.UrlField
import com.capyreader.app.ui.components.UsernameField
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun AuthFields(
    onUsernameChange: (username: String) -> Unit = {},
    onPasswordChange: (password: String) -> Unit,
    onSubmit: () -> Unit,
    username: String,
    readOnlyUsername: Boolean = false,
    password: String,
    loading: Boolean = false,
    errorMessage: String? = null,
    prompt: (@Composable () -> Unit)? = null,
    urlField: (@Composable () -> Unit)? = null,
    @StringRes usernameLabel: Int = R.string.auth_fields_username,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val submit = {
        keyboardController?.hide()
        onSubmit()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.imePadding()
    ) {
        UsernameField(
            value = username,
            onChange = onUsernameChange,
            readOnly = readOnlyUsername,
            label = usernameLabel,
        )
        PasswordField(
            onChange = onPasswordChange,
            value = password,
            onGo = {
                submit()
            }
        )
        urlField?.invoke()

        errorMessage?.let { message ->
            ErrorAlert(message = message)
        }
        Column(
            Modifier.padding(top = 16.dp)
        ) {
            Button(
                onClick = submit,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.auth_fields_log_in_button))
            }
        }

        prompt?.invoke()
    }
}

@Preview
@Composable
private fun AuthFieldsPreview() {
    CapyTheme {
        AuthFields(
            onUsernameChange = {},
            onPasswordChange = {},
            onSubmit = {},
            username = "test@example.com",
            password = "its a secret to everyone",
            loading = true,
            usernameLabel = R.string.auth_fields_username,
            urlField = {
                UrlField(
                    onChange = {},
                    value = "http://example.com",
                )
            }
        )
    }
}
