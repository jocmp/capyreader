package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.keyboard.KeyboardShortcutManager
import com.capyreader.app.keyboard.ShortcutAction
import com.capyreader.app.keyboard.ShortcutKey
import org.koin.compose.koinInject

@Composable
fun ShortcutsSettingsPanel(
    shortcutManager: KeyboardShortcutManager = koinInject(),
) {
    var bindings by remember { mutableStateOf(shortcutManager.effectiveBindings()) }
    var remapAction by remember { mutableStateOf<ShortcutAction?>(null) }

    fun refreshBindings() {
        bindings = shortcutManager.effectiveBindings()
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        ShortcutAction.entries.forEach { action ->
            val keys = bindings[action].orEmpty()

            ListItem(
                modifier = Modifier.clickable { remapAction = action },
                headlineContent = {
                    Text(stringResource(action.labelRes))
                },
                supportingContent = {
                    Text(
                        text = keys.joinToString(", ") { it.label() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            TextButton(
                onClick = {
                    shortcutManager.resetAll()
                    refreshBindings()
                }
            ) {
                Text(stringResource(R.string.shortcuts_reset_all))
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    remapAction?.let { action ->
        RemapDialog(
            action = action,
            shortcutManager = shortcutManager,
            onAssign = { key ->
                shortcutManager.updateBinding(action, listOf(key))
                refreshBindings()
                remapAction = null
            },
            onReset = {
                shortcutManager.resetBinding(action)
                refreshBindings()
                remapAction = null
            },
            onDismiss = { remapAction = null },
        )
    }
}

@Composable
private fun RemapDialog(
    action: ShortcutAction,
    shortcutManager: KeyboardShortcutManager,
    onAssign: (ShortcutKey) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var capturedKey by remember { mutableStateOf<ShortcutKey?>(null) }
    val focusRequester = remember { FocusRequester() }

    val conflict = capturedKey?.let { key ->
        shortcutManager.findConflict(key, excludeAction = action)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(action.labelRes)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) {
                            return@onPreviewKeyEvent false
                        }
                        val nativeEvent = event.nativeKeyEvent
                        if (ShortcutKey.isModifierOnly(nativeEvent.keyCode)) {
                            return@onPreviewKeyEvent false
                        }
                        capturedKey = ShortcutKey(
                            keyCode = nativeEvent.keyCode,
                            meta = ShortcutKey.metaState(nativeEvent),
                        )
                        true
                    }
            ) {
                Text(
                    text = capturedKey?.label()
                        ?: stringResource(R.string.shortcuts_press_key_prompt),
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (conflict != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.shortcuts_conflict_warning,
                            stringResource(conflict.labelRes)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onReset) {
                    Text(stringResource(R.string.shortcuts_reset_to_default))
                }
                TextButton(
                    enabled = capturedKey != null && conflict == null,
                    onClick = { capturedKey?.let(onAssign) },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
