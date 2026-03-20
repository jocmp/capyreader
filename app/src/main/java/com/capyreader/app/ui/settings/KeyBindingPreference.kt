package com.capyreader.app.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.capyreader.app.R
import com.capyreader.app.preferences.keyCodeToDisplayName

@Composable
fun KeyBindingPreference(
    keyCode: Int,
    onKeyCodeChange: (Int) -> Unit,
    @StringRes label: Int,
) {
    var isDialogOpen by remember { mutableStateOf(false) }

    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.background,
    )

    Box(
        Modifier.clickable { isDialogOpen = true }
    ) {
        ListItem(
            colors = colors,
            headlineContent = { Text(stringResource(label)) },
            supportingContent = {
                Text(keyCodeToDisplayName(keyCode))
            }
        )
    }

    if (isDialogOpen) {
        KeyBindingDialog(
            label = label,
            onDismiss = { isDialogOpen = false },
            onKeyCodeChange = { newKeyCode ->
                onKeyCodeChange(newKeyCode)
                isDialogOpen = false
            },
            onClear = {
                onKeyCodeChange(-1)
                isDialogOpen = false
            }
        )
    }
}

@Composable
private fun KeyBindingDialog(
    @StringRes label: Int,
    onDismiss: () -> Unit,
    onKeyCodeChange: (Int) -> Unit,
    onClear: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier
                .sizeIn(maxHeight = 600.dp, maxWidth = 360.dp)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        onKeyCodeChange(keyEvent.key.nativeKeyCode)
                        true
                    } else {
                        false
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(label),
                    style = typography.headlineSmall,
                )

                Text(
                    stringResource(R.string.settings_controls_press_a_key),
                    style = typography.bodyLarge,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClear) {
                        Text(stringResource(R.string.settings_controls_clear))
                    }
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                }
            }
        }
    }
}
