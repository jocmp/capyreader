package com.jocmp.basilreader.ui.articles

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

private const val ACCOUNT_ID_KEY = "account_id"

internal class ArticleArgs(val accountID: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(checkNotNull(savedStateHandle[ACCOUNT_ID_KEY]) as String)
}

fun articlesRoute(accountID: String) = "accounts/${accountID}/articles"

fun NavController.navigateToArticles(accountID: String, navOptions: NavOptions? = null) =
    navigate(articlesRoute(accountID), navOptions)

fun NavController.navigateToAddFeed(accountID: String) =
    navigate("accounts/${accountID}/feeds/new")

fun NavGraphBuilder.articleGraph(
    navController: NavController,
    defaultAccountID: String,
) {
    composable(
        route = "accounts/{${ACCOUNT_ID_KEY}}/articles",
        arguments = listOf(navArgument(ACCOUNT_ID_KEY) { defaultValue = defaultAccountID })
    ) {
        ArticleScreen(
            onNewFeedNavigate = { accountID ->
                navController.navigateToAddFeed(accountID)
            }
        )
    }
    composable(
        route = "accounts/{${ACCOUNT_ID_KEY}}/feeds/new",
    ) {
        AddFeedScreen(
            onCancel = {
                navController.popBackStack()
            },
            onSubmit = { accountID ->
                navController.navigate(articlesRoute(accountID)) {
                    popUpTo(articlesRoute(accountID)) {
                        inclusive = true
                    }
                }
            }
        )
    }
}
