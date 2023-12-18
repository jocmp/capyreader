package com.jocmp.basilreader.ui.articles

import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val articlesRoute = "articles"

fun feedRoute(feedID: String) = "feeds/${feedID}"

fun NavController.navigateToArticles() =
    navigate(articlesRoute)

fun NavController.navigateToFeed(feedID: String, navOptions: NavOptions? = null) =
    navigate(feedRoute(feedID), navOptions)

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
            },
            onFeedSelect = {
                navController.navigateToFeed(feedID = it)
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
    composable(
        route = "feeds/{feed_id}",
    ) {
        Text("Feed for ya")
    }
}
