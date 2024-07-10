package com.capyreader.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun TextSwitch(
    onCheckedChange: ((Boolean) -> Unit)?,
    checked: Boolean,
    text: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        text()
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
            text = { Text("Enable feature") }
        )
    }
}
