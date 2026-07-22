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
import com.jocmp.capy.ArticleStatus
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    val appPreferences by inject<AppPreferences>()

    private var deepLink by mutableStateOf<List<NavKey>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startBackStack = resolveBackStack(intent)
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
        // A deep link must never bypass the add-account flow when no account is configured.
        if (!hasAccount) return
        DeepLink.parse(intent.data, currentStatus)?.let { parsed ->
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

    /**
     * Resolve the launch back stack. The account gate comes first: a deep link only applies once an
     * account exists, otherwise we always land on the add-account flow.
     */
    private fun resolveBackStack(intent: Intent): List<NavKey> {
        if (!hasAccount) {
            return listOf(Route.AddAccount)
        }

        return DeepLink.parse(intent.data, currentStatus)
            ?: listOf(Route.ArticleList(appPreferences.filter.get()))
    }

    private val hasAccount: Boolean
        get() = appPreferences.accountID.get().isNotBlank()

    private val currentStatus: ArticleStatus
        get() = appPreferences.filter.get().status
}
