package com.capyreader.app.ui.accounts

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.capyreader.app.ui.Route
import com.capyreader.app.ui.settings.SettingsScreen
import com.jocmp.capy.accounts.Source

fun NavGraphBuilder.accountsGraph(
    onAddSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: (source: Source) -> Unit,
    onRemoveAccount: () -> Unit,
) {
    composable<Route.AddAccount> {
        AddAccountScreen(
            onAddSuccess = onAddSuccess,
            onNavigateToLogin = onNavigateToLogin
        )
    }
    composable<Route.Login> {
        LoginScreen(
            onNavigateBack = onNavigateBack,
            onSuccess = onAddSuccess,
        )
    }
    composable<Route.Settings> {
        SettingsScreen(
            onRemoveAccount = onRemoveAccount,
            onNavigateBack = onNavigateBack
        )
    }
}
