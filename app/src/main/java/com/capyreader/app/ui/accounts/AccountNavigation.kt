package com.capyreader.app.ui.accounts

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.capyreader.app.ui.Route
import com.capyreader.app.ui.settings.SettingsScreen

fun NavGraphBuilder.accountsGraph(
    onAddSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRemoveAccount: () -> Unit,
) {
    composable(Route.AddAccount.path) {
        AddAccountScreen(
            onAddSuccess = onAddSuccess,
            onNavigateToLogin = onNavigateToLogin
        )
    }
    composable(Route.Login.path) {
        LoginScreen(
            onNavigateBack = onNavigateBack,
            onSuccess = onAddSuccess,
        )
    }
    composable(Route.Settings.path) {
        SettingsScreen(
            onRemoveAccount = onRemoveAccount,
            onNavigateBack = onNavigateBack
        )
    }
}
