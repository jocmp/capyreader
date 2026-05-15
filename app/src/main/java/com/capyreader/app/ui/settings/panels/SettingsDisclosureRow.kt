package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingsDisclosureRow(
    title: String,
    onClick: () -> Unit,
) {
    Box(Modifier.clickable { onClick() }) {
        ListItem(
            headlineContent = { Text(title) },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        )
    }
}

@Preview
@Composable
private fun SettingsDisclosureRowPreview() {
    SettingsDisclosureRow(
        title = "Unread Badges",
        onClick = {},
    )
}
