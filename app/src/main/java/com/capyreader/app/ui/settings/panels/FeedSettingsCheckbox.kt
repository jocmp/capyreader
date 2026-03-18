package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FeedSettingsCheckbox(
    title: String,
    checked: Boolean,
    onToggle: (enabled: Boolean) -> Unit,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    Box(
        Modifier.clickable {
            onToggle(!checked)
        }
    ) {
        ListItem(
            leadingContent = leadingContent,
            headlineContent = {
                Text(title)
            },
            trailingContent = {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onToggle,
                )
            }
        )
    }
}
