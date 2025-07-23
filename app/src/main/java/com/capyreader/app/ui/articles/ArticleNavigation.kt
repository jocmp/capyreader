package com.capyreader.app.ui.articles

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.capyreader.app.ui.Route
import com.capyreader.app.ui.articles.detail.ArticleScreen

fun NavGraphBuilder.articleGraph(
    navController: NavController,
) {
    composable<Route.OldArticleIndex> {
        OldArticlesScreen(
            onNavigateToSettings = {
                navController.navigate(Route.Settings) {
                    launchSingleTop = true
                }
            },
            onNavigateToArticle = { id ->
                navController.navigate(Route.Article(id))
            }
        )
    }
    composable<Route.Article> {
        ArticleScreen {
            navController.navigateUp()
        }
    }
}
