package com.jocmp.capyreader.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jocmp.capyreader.ui.accounts.accountsGraph
import com.jocmp.capyreader.ui.articles.articleGraph
import com.jocmp.capyreader.ui.theme.CapyTheme
import com.jocmp.capyreader.unloadAccountModules

@Composable
fun App(
    startDestination: String,
) {
    val navController = rememberNavController()

    CapyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val compactWidth = isCompact()

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                accountsGraph(
                    isCompactWidth = compactWidth,
                    onNavigateToLogin = {
                        navController.navigate(Route.Login)
                    },
                    onAddSuccess = {
                        navController.navigate(Route.Articles) {
                            popUpTo(Route.AddAccount.path) {
                                inclusive = true
                            }
                        }
                    },
                    onLogout = {
                        navController.navigate(Route.Login) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                        unloadAccountModules()
                    },
                    onNavigateBackFromSettings = {
                        navController.navigateUp()
                    }
                )
                articleGraph(navController = navController)
            }
        }
    }
}
