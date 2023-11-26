package com.jocmp.basilreader.ui.articles

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.jocmp.basil.AccountManager
import org.koin.compose.koinInject

private const val ACCOUNT_ID_KEY = "account_id"

internal class ArticleArgs(val accountId: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(checkNotNull(savedStateHandle[ACCOUNT_ID_KEY]) as String)
}

fun NavController.navigateToArticles(accountId: String) =
    navigate("articles?account_id=${accountId}")

fun NavGraphBuilder.articleGraph(defaultAccountID: String) {
    composable(
        route = "articles?account_id={${ACCOUNT_ID_KEY}}",
        arguments = listOf(navArgument(ACCOUNT_ID_KEY) { defaultValue = defaultAccountID })
    ) {
        ArticleScreen()
    }
}
