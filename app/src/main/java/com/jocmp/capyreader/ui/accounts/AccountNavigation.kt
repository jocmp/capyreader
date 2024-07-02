package com.jocmp.capyreader.ui.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.jocmp.capyreader.ui.Route
import com.jocmp.capyreader.ui.components.DialogCard
import com.jocmp.capyreader.ui.components.composable
import com.jocmp.capyreader.ui.settings.SettingsScreen

fun NavGraphBuilder.accountsGraph(
    onAddSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onLogout: () -> Unit,
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
            onRemoveAccount = onLogout,
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
