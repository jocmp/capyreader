package com.jocmp.basilreader.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.jocmp.basilreader.Route
import com.jocmp.basilreader.ui.auth.LoginForm
import com.jocmp.basilreader.ui.folders.FoldersLayout


@Composable
fun AppLayout() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.FeedEntry
    ) {
        composable(Route.FeedEntry) {
            FoldersLayout(navController)
        }
        dialog(Route.AuthDialog) {
            LoginForm {
                navController.navigate(Route.FeedEntry)
            }
        }
    }
}