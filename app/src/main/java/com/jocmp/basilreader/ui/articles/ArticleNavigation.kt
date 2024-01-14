package com.jocmp.basilreader.ui.articles

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog

const val articlesRoute = "articles"

fun NavController.navigateToArticles() =
    navigate(articlesRoute)

fun NavController.navigateToAddFeed() =
    navigate("feeds/new")

fun NavController.navigateToEditFeed(feedID: String) =
    navigate("feeds/${feedID}/edit")

fun NavGraphBuilder.articleGraph(
    navController: NavController,
) {
    composable(
        route = "articles",
    ) {
        ArticleScreen(
            onAddFeed = {
                navController.navigateToAddFeed()
            },
            onEditFeed = { feedID ->
                navController.navigateToEditFeed(feedID = feedID)
            }
        )
    }
    dialog(
        route = "feeds/{id}/edit",
    ) {
        EditFeedScreen(
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
    dialog(
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

internal class EditFeedArgs(val feedID: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(checkNotNull(savedStateHandle["id"]) as String)
}
