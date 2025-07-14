package com.capyreader.app.ui.articles

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.capyreader.app.ui.Route
import com.capyreader.app.ui.articles.index.ArticleIndexScreen

fun NavGraphBuilder.articleGraph(
    navController: NavController,
) {
    composable<Route.Articles> {
        ArticleScreen(
            onNavigateToSettings = {
                navController.navigate(Route.Settings) {
                    launchSingleTop = true
                }
            }
        )
    }
    composable<Route.ArticleIndex> {
        ArticleIndexScreen(
            onNavigateToSettings = {
                navController.navigate(Route.Settings) {
                    launchSingleTop = true
                }
            }
        )
    }
}
