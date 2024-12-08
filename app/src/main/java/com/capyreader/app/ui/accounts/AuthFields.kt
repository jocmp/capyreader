package com.capyreader.app.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType.EmailAddress
import androidx.compose.ui.autofill.AutofillType.Password
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.autofill
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.accounts.Source

@OptIn(ExperimentalComposeUiApi::class)
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
    source: Source,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val (showPassword, setPasswordVisibility) = rememberSaveable {
        mutableStateOf(false)
    }

    val passwordTransformation = if (showPassword) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    val submit = {
        keyboardController?.hide()
        onSubmit()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.imePadding()
    ) {
        TextField(
            value = username,
            onValueChange = onUsernameChange,
            singleLine = true,
            enabled = !readOnlyUsername,
            readOnly = readOnlyUsername,
            label = {
                Text(stringResource(source.usernameKey))
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier
                .fillMaxWidth()
                .autofill(
                    listOf(EmailAddress),
                    onFill = onUsernameChange
                )
        )
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            label = {
                Text(stringResource(R.string.auth_fields_password))
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Go,
                keyboardType = KeyboardType.Password
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    submit()
                }
            ),
            visualTransformation = passwordTransformation,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .autofill(
                    listOf(Password),
                    onFill = onPasswordChange
                ),
            trailingIcon = {
                val image = if (showPassword) {
                    Icons.Filled.Visibility
                } else {
                    Icons.Filled.VisibilityOff
                }

                val contentDescription = if (showPassword) {
                    R.string.auth_fields_hide_password
                } else {
                    R.string.auth_fields_show_password
                }

                IconButton(
                    onClick = {
                        setPasswordVisibility(!showPassword)
                    }
                ) {
                    Icon(imageVector = image, stringResource(contentDescription))
                }
            }
        )
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

@Composable
private fun ErrorAlert(message: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colorScheme.error)
            .fillMaxWidth()
    ) {
        Text(
            text = message,
            color = colorScheme.onError,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
        )
    }
}

private val Source.usernameKey
    get() = when(this) {
        Source.FEEDBIN -> R.string.auth_fields_email
        else -> R.string.auth_fields_username
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
            source = Source.FRESHRSS
        )
    }
}
