package com.capyreader.app.ui.articles

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.capyreader.app.ui.Route

fun NavGraphBuilder.articleGraph(
    navController: NavController,
) {
    composable<Route.Articles> {
        ArticlesScreen(
            onNavigateToSettings = {
                navController.navigate(Route.Settings) {
                    launchSingleTop = true
                }
            }
        )
    }
    composable<Route.Article> {
        ArticleScreen(
            onNavigateUp = {
                navController.navigateUp()
            },
            onSelectArticle = { id ->
                navController.navigate(Route.Article(id))
            }
        )
    }
}
