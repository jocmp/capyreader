package com.capyreader.app.ui.articles

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.capyreader.app.ui.Route

fun NavGraphBuilder.articleGraph(
    navController: NavController,
) {
    composable(
        route = "articles",
    ) {
        ArticleScreen(
            onNavigateToSettings = {
                navController.navigate(Route.Settings.path) {
                    launchSingleTop = true
                }
            }
        )
    }
}
