package com.jocmp.basilreader.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jocmp.basilreader.ui.Route

fun NavGraphBuilder.accountsGraph(
    onLoginSuccess: () -> Unit,
    goBackToAccountIndex: () -> Unit,
) {
    composable(Route.AccountIndex.path) {
        LoginScreen(
            onSuccess = onLoginSuccess,
        )
    }
    composable(Route.AccountSettings.path) {
        AccountSettingsScreen(
            goBack = goBackToAccountIndex
        )
    }
}

internal class AccountSettingsArgs(val accountID: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(checkNotNull(savedStateHandle["id"]) as String)
}
