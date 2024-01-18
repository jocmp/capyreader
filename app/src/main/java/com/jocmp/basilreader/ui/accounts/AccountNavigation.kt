package com.jocmp.basilreader.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavController.navigateToAccounts() {
    navigate("accounts")
}

fun NavController.navigateToAccountSettings(accountID: String) {
    navigate("accounts/${accountID}/edit")
}

fun NavGraphBuilder.accountsGraph(
    onSelect: () -> Unit,
    onSettingSelect: (accountID: String) -> Unit,
) {
    composable("accounts") {
        AccountIndexScreen(
            onSelect = onSelect,
            onSettingsSelect = onSettingSelect,
        )
    }
    composable("accounts/{id}/edit") {
        AccountSettingsScreen()
    }
}

internal class AccountSettingsArgs(val accountID: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(checkNotNull(savedStateHandle["id"]) as String)
}
