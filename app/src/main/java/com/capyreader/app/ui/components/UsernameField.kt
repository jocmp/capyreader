package com.capyreader.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType.EmailAddress
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.capyreader.app.R
import com.capyreader.app.ui.autofill

@Composable
fun UsernameField(
    onChange: (value: String) -> Unit,
    value: String,
    readOnly: Boolean = false,
    isError: Boolean = false,
    @StringRes label: Int = R.string.auth_fields_username
) {
    TextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        enabled = !readOnly,
        readOnly = readOnly,
        isError = isError,
        label = {
            Text(stringResource(label))
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Email
        ),
        modifier = Modifier
            .fillMaxWidth()
            .autofill(
                listOf(EmailAddress),
                onFill = onChange
            )
    )
}
