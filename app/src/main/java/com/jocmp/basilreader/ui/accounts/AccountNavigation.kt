package com.jocmp.basilreader.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jocmp.basilreader.ui.Route

fun NavGraphBuilder.accountsGraph(
    onSelect: () -> Unit,
    onSettingSelect: (accountID: String) -> Unit,
    goBackToAccountIndex: () -> Unit,
) {
    composable(Route.AccountIndex.path) {
        AccountIndexScreen(
            onSelect = onSelect,
            onSettingsSelect = onSettingSelect,
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
