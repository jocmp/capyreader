package com.jocmp.capyreader.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextField as MaterialTextField
@Composable
fun TextField(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
) {
    MaterialTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.then(
            Modifier.fillMaxWidth()
                .padding(bottom = 8.dp)
        ),
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions
    )
}

@Preview
@Composable
fun TextFieldPreview() {
    TextField(
        value = "Hello Moto",
        onValueChange = {}
    )
}
