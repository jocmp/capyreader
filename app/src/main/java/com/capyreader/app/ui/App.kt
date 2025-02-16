package com.capyreader.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.capyreader.app.preferences.ThemeOption
import com.capyreader.app.ui.accounts.accountsGraph
import com.capyreader.app.ui.articles.articleGraph
import com.capyreader.app.ui.theme.CapyTheme
import com.capyreader.app.unloadAccountModules

@Composable
fun App(
    startDestination: Route,
    theme: ThemeOption,
) {
    val navController = rememberNavController()

    CapyTheme(theme = theme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                accountsGraph(
                    onAddSuccess = {
                        navController.navigate(Route.Articles) {
                            launchSingleTop = true

                            popUpTo(Route.AddAccount) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onNavigateToLogin = { source ->
                        navController.navigate(Route.Login(source))
                    },
                    onRemoveAccount = {
                        navController.navigate(Route.AddAccount) {
                            popUpTo(Route.Articles) {
                                inclusive = true
                            }
                        }
                        unloadAccountModules()
                    }
                )
                articleGraph(navController = navController)
            }
        }
    }
}
