package com.capyreader.app.ui.articles

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.capyreader.app.ui.Route
import com.jocmp.capy.ArticleFilter

fun NavGraphBuilder.articleGraph(
    navController: NavController,
    pendingArticleID: String? = null,
    pendingFilter: ArticleFilter? = null,
    onPendingNotificationHandled: () -> Unit = {},
) {
    composable<Route.Articles> {
        ArticleScreen(
            pendingArticleID = pendingArticleID,
            pendingFilter = pendingFilter,
            onPendingNotificationHandled = onPendingNotificationHandled,
            onNavigateToSettings = {
                navController.navigate(Route.Settings) {
                    launchSingleTop = true
                }
            }
        )
    }
}
