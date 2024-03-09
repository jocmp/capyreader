package com.jocmp.basilreader.ui.articles

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.jocmp.basilreader.ui.Route
import com.jocmp.basilreader.ui.navigate

const val articlesRoute = "articles"

fun NavController.navigateToArticles() =
    navigate(articlesRoute)

fun NavController.navigateToAddFeed() =
    navigate("feeds/new")

fun NavController.navigateToEditFeed(feedID: String) =
    navigate("feeds/${feedID}/edit")

fun NavController.navigateToEditFolder(folderTitle: String) =
    navigate("folders/${folderTitle}/edit")

fun NavGraphBuilder.articleGraph(
    navController: NavController,
) {
    composable(
        route = "articles",
    ) {
        ArticleScreen(
            onEditFeed = { feedID ->
                navController.navigateToEditFeed(feedID = feedID)
            },
            onEditFolder = { folderTitle ->
                navController.navigateToEditFolder(folderTitle = folderTitle)
            },
            onNavigateToAccounts = {
                navController.navigate(Route.AccountIndex)
            }
        )
    }
    dialog(
        route = "feeds/{id}/edit",
    ) {
        EditFeedScreen(
            onSubmit = {
                navController.navigate(articlesRoute) {
                    popUpTo(articlesRoute) {
                        inclusive = true
                    }
                }
            },
            onCancel = {
                navController.popBackStack()
            },
        )
    }
    dialog(
        route = "folders/{title}/edit",
    ) {
        EditFolderScreen(
            onSubmit = {
                navController.navigate(articlesRoute) {
                    popUpTo(articlesRoute) {
                        inclusive = true
                    }
                }
            },
            onCancel = {
                navController.popBackStack()
            },
        )
    }
}

internal class EditFeedArgs(val feedID: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(checkNotNull(savedStateHandle["id"]) as String)
}

internal class EditFolderArgs(val folderTitle: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(checkNotNull(savedStateHandle["title"]) as String)
}
