package com.jocmp.basilreader.ui.articles

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val articlesRoute = "articles"

fun feedRoute(feedID: String) = "articles"

fun NavController.navigateToArticles() =
    navigate(articlesRoute)

fun NavController.navigateToAddFeed() =
    navigate("feeds/new")

fun NavGraphBuilder.articleGraph(
    navController: NavController,
) {
    composable(
        route = "articles",
    ) {
        ArticleScreen(
            onFeedAdd = {
                navController.navigateToAddFeed()
            }
        )
    }
    composable(
        route = "feeds/new",
    ) {
        AddFeedScreen(
            onCancel = {
                navController.popBackStack()
            },
            onSubmit = {
                navController.navigate(articlesRoute) {
                    popUpTo(articlesRoute) {
                        inclusive = true
                    }
                }
            }
        )
    }
}
