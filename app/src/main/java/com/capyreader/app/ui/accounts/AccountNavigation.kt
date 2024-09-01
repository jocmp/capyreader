package com.capyreader.app.ui.accounts

import androidx.navigation.NavGraphBuilder
import com.capyreader.app.ui.Route
import com.capyreader.app.ui.components.composable
import com.capyreader.app.ui.settings.SettingsScreen

fun NavGraphBuilder.accountsGraph(
    onAddSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRemoveAccount: () -> Unit,
) {
    composable(Route.AddAccount) {
        AddAccountScreen(
            onAddSuccess = onAddSuccess,
            onNavigateToLogin = onNavigateToLogin
        )
    }
    composable(Route.Login) {
        LoginScreen(
            onNavigateBack = onNavigateBack,
            onSuccess = onAddSuccess,
        )
    }
    composable(Route.Settings) {
        SettingsScreen(
            onRemoveAccount = onRemoveAccount,
            onNavigateBack = onNavigateBack
        )
    }
}
