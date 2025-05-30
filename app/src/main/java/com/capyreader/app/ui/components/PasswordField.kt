package com.capyreader.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType.Password
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.capyreader.app.R
import com.capyreader.app.ui.autofill

@Composable
fun PasswordField(
    onChange: (value: String) -> Unit,
    value: String,
    onGo: (KeyboardActionScope.() -> Unit)? = null,
    isError: Boolean = false,
) {
    val (showPassword, setPasswordVisibility) = rememberSaveable {
        mutableStateOf(false)
    }

    val passwordTransformation = if (showPassword) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    TextField(
        value = value,
        onValueChange = onChange,
        label = {
            Text(stringResource(R.string.auth_fields_password))
        },
        isError = isError,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Go,
            keyboardType = KeyboardType.Password
        ),
        keyboardActions = KeyboardActions(
            onGo = onGo
        ),
        visualTransformation = passwordTransformation,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .autofill(
                listOf(Password),
                onFill = onChange,
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
}
