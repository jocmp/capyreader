package com.capyreader.app.ui.settings.panels

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.compose.koinInject

@Composable
fun NotificationsSettingsPanel(viewModel: NotificationSettingsViewModel = koinInject()) {
    NotificationsSettingPanelView(
    )
}

@Composable
private fun NotificationsSettingPanelView(

) {

}

@Preview
@Composable
fun NotificationsSettingsPanelPreview() {
   CapyTheme {
       NotificationsSettingPanelView(

       )
   }
}
