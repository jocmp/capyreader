package com.jocmp.basilreader.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.TextField as MaterialTextField
@Composable
fun TextField(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false
) {
    MaterialTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        readOnly = readOnly
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
