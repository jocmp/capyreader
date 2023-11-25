package com.jocmp.basilreader.ui.articles

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private const val ACCOUNT_ID_KEY = "account_id"

internal class ArticleArgs(val accountId: String) {
    constructor(savedStateHandle: SavedStateHandle):
        this(checkNotNull(savedStateHandle[ACCOUNT_ID_KEY]) as String)
}

fun NavController.navigateToArticles(accountId: String) =
    navigate("articles?account_id=${accountId}")

fun NavGraphBuilder.articleIndex() {
    composable("articles?account_id=${ACCOUNT_ID_KEY}") {
        ArticlesView()
    }
}
