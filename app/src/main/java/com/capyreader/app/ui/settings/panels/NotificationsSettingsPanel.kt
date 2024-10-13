package com.capyreader.app.ui.settings.panels

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun NotificationsSettingsPanel() {
    Text("Hello world")
}

@Preview
@Composable
fun NotificationsSettingsPanelPreview() {
   CapyTheme {
       NotificationsSettingsPanel()
   }
}