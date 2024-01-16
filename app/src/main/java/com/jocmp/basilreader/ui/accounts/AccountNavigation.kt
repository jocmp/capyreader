package com.jocmp.basilreader.ui.accounts

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.accountsGraph(
    onSelect: () -> Unit
) {
    composable("accounts") {
        AccountIndexScreen(onSelect = onSelect)
    }
    composable("account/{id}/edit") {
        AccountSettingsScreen()
    }
}

fun NavController.navigateToAddAccount() {
    navigate("accounts")
}
