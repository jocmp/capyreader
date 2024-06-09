package com.jocmp.basilreader.ui.login

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jocmp.basilreader.ui.Route
import com.jocmp.basilreader.ui.settings.SettingsScreen

fun NavGraphBuilder.accountsGraph(
    onLoginSuccess: () -> Unit,
    onLogout: () -> Unit
) {
    composable(Route.Login.path) {
        LoginScreen(
            onSuccess = onLoginSuccess,
        )
    }
    composable(Route.Settings.path) {
        SettingsScreen(onLogout = onLogout)
    }
}

internal class AccountSettingsArgs(val accountID: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(checkNotNull(savedStateHandle["id"]) as String)
}
