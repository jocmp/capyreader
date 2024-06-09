package com.jocmp.basilreader.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jocmp.basilreader.ui.articles.articleGraph
import com.jocmp.basilreader.ui.login.accountsGraph
import com.jocmp.basilreader.ui.theme.CapyTheme
import com.jocmp.basilreader.unloadAccountModules

@Composable
fun App(
    startDestination: String,
    windowSizeClass: WindowSizeClass
) {
    val navController = rememberNavController()

    CapyTheme {
        CompositionLocalProvider(LocalWindowWidth provides windowSizeClass.widthSizeClass) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    accountsGraph(
                        onLoginSuccess = {
                            navController.navigate(Route.Articles.path) {
                                popUpTo(Route.Login.path) {
                                    inclusive = true
                                }
                            }
                        },
                        onLogout = {
                            navController.navigate(Route.Login.path) {
                                popUpTo(navController.graph.startDestinationId) {
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
}
