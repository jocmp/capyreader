package com.capyreader.app.ui.addintent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.capyreader.app.R

@Composable
fun SavePageView(
    defaultQueryURL: String,
    onSavePage: (url: String) -> Unit,
    loading: Boolean,
    error: String?,
) {
    val (url, setURL) = rememberSaveable { mutableStateOf(defaultQueryURL) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val isError = error != null

    val save = {
        focusManager.clearFocus()
        keyboard?.hide()
        onSavePage(url)
    }

    Column(Modifier.padding(top = 16.dp)) {
        OutlinedTextField(
            value = url,
            onValueChange = setURL,
            leadingIcon = {
                Icon(Icons.Filled.Add, contentDescription = null)
            },
            label = {
                Text(stringResource(R.string.add_feed_url_title))
            },
            isError = isError,
            supportingText = {
                error?.let {
                    val resource = when (it) {
                        "network" -> R.string.save_page_network_error
                        else -> R.string.save_page_error
                    }
                    Text(stringResource(resource))
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { save() }
            ),
            trailingIcon = {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )
        Button(
            onClick = { save() },
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(stringResource(R.string.save_page_submit))
        }
    }
}
