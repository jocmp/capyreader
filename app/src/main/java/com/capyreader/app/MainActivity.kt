package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.App
import com.capyreader.app.ui.DeepLink
import com.capyreader.app.ui.Route
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    val appPreferences by inject<AppPreferences>()

    private var deepLink by mutableStateOf<List<NavKey>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startBackStack = DeepLink.parse(intent.data) ?: listOf(startDestination())
        applyListFilter(startBackStack)

        setContent {
            App(
                startBackStack = startBackStack,
                appPreferences = appPreferences,
                deepLink = deepLink,
                onDeepLinkConsumed = { deepLink = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        DeepLink.parse(intent.data)?.let { parsed ->
            applyListFilter(parsed)
            deepLink = parsed
        }
    }

    /**
     * Keep the persisted filter in sync with the deep link's list, so the list and the reader's
     * neighbor query (both read [AppPreferences.filter]) agree on which articles are siblings.
     */
    private fun applyListFilter(backStack: List<NavKey>) {
        (backStack.firstOrNull() as? Route.ArticleList)?.let {
            appPreferences.filter.set(it.filter)
        }
    }

    private fun startDestination(): Route {
        val appPreferences = get<AppPreferences>()

        val accountID = appPreferences.accountID.get()

        return if (accountID.isBlank()) {
            Route.AddAccount
        } else {
            Route.ArticleList(appPreferences.filter.get())
        }
    }
}
