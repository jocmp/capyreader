package com.capyreader.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun TextSwitch(
    onCheckedChange: ((Boolean) -> Unit)?,
    checked: Boolean,
    title: String,
    subtitle: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = colorScheme.onSurfaceVariant,
                    style = typography.bodySmall,
                )
            }
        }

        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview
@Composable
private fun TextSwitchPreview() {
    CapyTheme {
        TextSwitch(
            checked = true,
            onCheckedChange = {},
            title = "Enable feature",
            subtitle = "Some important context"
        )
    }
}
