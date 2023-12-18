package com.jocmp.basilreader.ui.accounts

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.accountIndex(
    onSelect: () -> Unit
) {
    composable("accounts") {
        AccountIndexView(onSelect = onSelect)
    }
}

fun NavController.navigateToAddAccount() {
    navigate("accounts")
}
