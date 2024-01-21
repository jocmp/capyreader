package com.jocmp.basilreader.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jocmp.basilreader.ui.accounts.accountsGraph
import com.jocmp.basilreader.ui.articles.articleGraph
import com.jocmp.basilreader.ui.articles.navigateToArticles
import com.jocmp.basilreader.ui.theme.BasilReaderTheme

@Composable
fun App(startDestination: String) {
    val navController = rememberNavController()

    BasilReaderTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                accountsGraph(
                    onSelect = {
                        navController.navigateToArticles()
                    },
                    onSettingSelect = { id ->
                        navController.navigate(Route.AccountSettings(id))
                    },
                    goBackToAccountIndex = {
                        navController.navigate(Route.AccountIndex.path) {
                            launchSingleTop = true
                            popUpTo(Route.AccountIndex.path) {
                                inclusive = true
                            }
                        }
                    }
                )
                articleGraph(navController = navController)
            }
        }
    }
}
