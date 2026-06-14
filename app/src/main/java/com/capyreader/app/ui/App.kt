package com.capyreader.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.accounts.AddAccountScreen
import com.capyreader.app.ui.accounts.LoginScreen
import com.capyreader.app.ui.articles.ArticleScreen
import com.capyreader.app.ui.settings.SettingsScreen
import com.capyreader.app.ui.theme.CapyTheme
import com.capyreader.app.unloadAccountModules
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun App(
    startDestination: Route,
    appPreferences: AppPreferences,
    pendingArticleID: String? = null,
    onPendingArticleSelected: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(startDestination)

    CapyTheme(appPreferences) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                // Scope a ViewModelStore per NavEntry so each route instance gets its own
                // ViewModel (e.g. Login per source, and the per-entry article VMs to come).
                // Overriding entryDecorators replaces the defaults, so re-add the saveable one.
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<Route.AddAccount> {
                        AddAccountScreen(
                            onAddSuccess = { backStack.resetTo(Route.Articles) },
                            onNavigateToLogin = { source -> backStack.add(Route.Login(source)) }
                        )
                    }
                    entry<Route.Login> { key ->
                        LoginScreen(
                            viewModel = koinViewModel { parametersOf(key.source) },
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onSuccess = { backStack.resetTo(Route.Articles) },
                        )
                    }
                    entry<Route.Settings> {
                        SettingsScreen(
                            onRemoveAccount = {
                                backStack.resetTo(Route.AddAccount)
                                unloadAccountModules()
                            },
                            onNavigateBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<Route.Articles> {
                        ArticleScreen(
                            pendingArticleID = pendingArticleID,
                            onPendingArticleSelected = onPendingArticleSelected,
                            onNavigateToSettings = { backStack.add(Route.Settings) }
                        )
                    }
                }
            )
        }
    }
}

/**
 * Replaces the entire back stack with [key]. Mirrors the previous
 * `popUpTo(..) { inclusive = true }` + `launchSingleTop` navigation used for
 * account add/remove transitions.
 */
private fun NavBackStack<NavKey>.resetTo(key: NavKey) {
    clear()
    add(key)
}
