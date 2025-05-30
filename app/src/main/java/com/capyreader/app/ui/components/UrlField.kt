package com.capyreader.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.capyreader.app.R

@Composable
fun UrlField(
    onChange: (value: String) -> Unit,
    isError: Boolean = false,
    value: String,
) {
    TextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        placeholder = {
            Text(stringResource(R.string.auth_fields_api_url_placeholder))
        },
        isError = isError,
        label = {
            Text(stringResource(R.string.auth_fields_api_url))
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Uri,
        ),
        modifier = Modifier
            .fillMaxWidth()
    )
}
