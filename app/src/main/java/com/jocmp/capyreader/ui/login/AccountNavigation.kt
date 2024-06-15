package com.jocmp.capyreader.ui.login

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.jocmp.capyreader.ui.Route
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
    settingsLayout(isCompactWindow) {
        SettingsScreen(
            onLogout = onLogout,
            onNavigateBack = onNavigateBackFromSettings
        )
    }
}

fun NavGraphBuilder.settingsLayout(isCompactWindow: Boolean, content: @Composable () -> Unit) {
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

            Card(
                Modifier
                    .padding(32.dp)
                    .sizeIn(maxHeight = 600.dp, maxWidth = 400.dp)
            ) {
                content()
            }
        }
    }
}
