package com.jocmp.capyreader.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.capyreader.R

@Composable
fun LoginView(
    onUsernameChange: (username: String) -> Unit = {},
    onPasswordChange: (password: String) -> Unit = {},
    onSubmit: () -> Unit = {},
    username: String,
    password: String,
    loading: Boolean = false,
    showError: Boolean = false,
) {
    val errorMessage = if (showError) {
        stringResource(R.string.auth_error_message)
    } else {
        null
    }

    Scaffold { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.login_title),
                    style = typography.headlineMedium,
                )
                AuthFields(
                    username = username,
                    password = password,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onSubmit = onSubmit,
                    loading = loading,
                    errorMessage = errorMessage
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoginViewPreview() {
    LoginView(
        username = "test@example.com",
        password = "",
    )
}
