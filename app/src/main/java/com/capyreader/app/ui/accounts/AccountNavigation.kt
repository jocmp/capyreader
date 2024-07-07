package com.capyreader.app.ui.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.capyreader.app.ui.Route
import com.capyreader.app.ui.components.DialogCard
import com.capyreader.app.ui.components.composable
import com.capyreader.app.ui.settings.SettingsScreen

fun NavGraphBuilder.accountsGraph(
    onAddSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRemoveAccount: () -> Unit,
    onNavigateBackFromSettings: () -> Unit,
    isCompactWidth: Boolean,
) {
    composable(Route.AddAccount) {
        AddAccountScreen(
            onAddSuccess = onAddSuccess,
            onNavigateToLogin = onNavigateToLogin
        )
    }
    composable(Route.Login) {
        LoginScreen(
            onSuccess = onAddSuccess,
        )
    }
    dynamicLayout(isCompactWidth) {
        SettingsScreen(
            onRemoveAccount = onRemoveAccount,
            onNavigateBack = onNavigateBackFromSettings
        )
    }
}

fun NavGraphBuilder.dynamicLayout(isCompactWindow: Boolean, content: @Composable () -> Unit) {
    val route = Route.Settings.path

    if (isCompactWindow) {
        composable(route) {
            content()
        }
    } else {
        dialog(
            route,
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            DialogCard(content = content)
        }
    }
}
