package com.capyreader.lite.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.capyreader.lite.loadLiteAccountModules
import com.capyreader.lite.ui.articles.ArticlePagerScreen
import com.capyreader.lite.ui.feeds.FraidycatScreen
import com.capyreader.lite.ui.feeds.FraidycatViewModel
import com.capyreader.lite.ui.login.LiteLoginScreen
import org.koin.compose.koinInject

private object Routes {
    const val LOGIN = "login"
    const val FEEDS = "feeds"
    const val ARTICLES = "articles"
}

@Composable
fun LiteApp(startLoggedIn: Boolean) {
    val nav = rememberNavController()
    val start = if (startLoggedIn) Routes.FEEDS else Routes.LOGIN

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.LOGIN) {
            LiteLoginScreen(
                onAuthenticated = {
                    loadLiteAccountModules()
                    nav.navigate(Routes.FEEDS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.FEEDS) {
            val vm = koinInject<FraidycatViewModel>()
            FraidycatScreen(
                onSelectFeed = { id ->
                    vm.selectFeed(id)
                    nav.navigate(Routes.ARTICLES)
                },
            )
        }
        composable(Routes.ARTICLES) {
            ArticlePagerScreen(onBack = { nav.popBackStack() })
        }
    }
}
