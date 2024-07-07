package com.capyreader.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.ui.accounts.accountsGraph
import com.capyreader.app.ui.articles.articleGraph
import com.capyreader.app.ui.theme.CapyTheme
import com.capyreader.app.unloadAccountModules
import org.koin.compose.koinInject

@Composable
fun App(
    startDestination: String,
    theme: ThemeOption,
) {
    val navController = rememberNavController()

    CapyTheme(theme = theme) {
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
                        navController.navigate(Route.Login.path)
                    },
                    onAddSuccess = {
                        navController.navigate(Route.Articles.path) {
                            launchSingleTop = true

                            popUpTo(Route.AddAccount.path) {
                                inclusive = true
                            }
                        }
                    },
                    onRemoveAccount = {
                        navController.navigate(Route.AddAccount.path) {
                            popUpTo(Route.Articles.path) {
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
