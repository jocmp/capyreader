package com.capyreader.app.ui.articles

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.capyreader.app.ui.Route

fun NavGraphBuilder.articleGraph(
    navController: NavController,
    pendingArticleID: String? = null,
    onPendingArticleIDConsumed: () -> Unit = {},
) {
    composable<Route.Articles> {
        ArticleScreen(
            pendingArticleID = pendingArticleID,
            onPendingArticleIDConsumed = onPendingArticleIDConsumed,
            onNavigateToSettings = {
                navController.navigate(Route.Settings) {
                    launchSingleTop = true
                }
            }
        )
    }
}
