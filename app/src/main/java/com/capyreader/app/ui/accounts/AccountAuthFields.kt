package com.capyreader.app.ui.accounts

import androidx.compose.runtime.Composable
import com.capyreader.app.R
import com.jocmp.capy.accounts.Source

@Composable
fun AccountAuthFields(
    onUsernameChange: (username: String) -> Unit = {},
    onPasswordChange: (password: String) -> Unit,
    onSubmit: () -> Unit,
    username: String,
    readOnlyUsername: Boolean = false,
    password: String,
    loading: Boolean = false,
    errorMessage: String? = null,
    prompt: (@Composable () -> Unit)? = null,
    source: Source,
    urlField: (@Composable () -> Unit)? = null,
) {
    AuthFields(
        onUsernameChange = onUsernameChange,
        onPasswordChange = onPasswordChange,
        onSubmit = onSubmit,
        username = username,
        readOnlyUsername = readOnlyUsername,
        password = password,
        loading = loading,
        errorMessage = errorMessage,
        prompt = prompt,
        usernameLabel = source.usernameKey,
        urlField = urlField,
    )
}

private val Source.usernameKey
    get() = when (this) {
        Source.FEEDBIN -> R.string.auth_fields_email
        else -> R.string.auth_fields_username
    }
