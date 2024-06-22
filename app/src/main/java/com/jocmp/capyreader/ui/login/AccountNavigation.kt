package com.jocmp.capyreader.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.jocmp.capyreader.ui.Route
import com.jocmp.capyreader.ui.components.DialogCard
import com.jocmp.capyreader.ui.settings.SettingsScreen

fun NavGraphBuilder.accountsGraph(
    onLoginSuccess: () -> Unit,
    onLogout: () -> Unit,
    onNavigateBackFromSettings: () -> Unit,
    isCompactWindow: Boolean,
) {
    composable(Route.Login.path) {
        LoginScreen(
            onSuccess = onLoginSuccess,
        )
    }
    dynamicLayout(isCompactWindow) {
        SettingsScreen(
            onLogout = onLogout,
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
