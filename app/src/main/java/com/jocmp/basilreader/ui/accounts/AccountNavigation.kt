package com.jocmp.basilreader.ui.accounts

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jocmp.basil.Account

fun NavGraphBuilder.accountIndex(
    onNavigate: (account: Account) -> Unit
) {
    composable("accounts") {
        AccountIndexView(onNavigate = onNavigate)
    }
}
