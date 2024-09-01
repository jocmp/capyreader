package com.capyreader.app.ui.settings

import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen(
    onRemoveAccount: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    SettingsView(
        onNavigateBack = onNavigateBack,
        onRemoveAccount = onRemoveAccount,
    )
}
