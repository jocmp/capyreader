package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.App
import com.capyreader.app.ui.Route
import com.jocmp.capy.common.launchIO
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    val appPreferences by inject<AppPreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchIO {
            NotificationHelper.openFromIntent(intent, appPreferences = appPreferences)
        }

        val theme = appPreferences.theme

        setContent {
            val themeState by appPreferences.theme.changes().collectAsState(initial = theme.get())

            App(
                startDestination = startDestination(),
                theme = themeState
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        lifecycleScope.launchIO {
            NotificationHelper.openFromIntent(intent, appPreferences = appPreferences)
        }
    }

    private fun startDestination(): Route {
        val appPreferences = get<AppPreferences>()

        val accountID = appPreferences.accountID.get()

        return if (accountID.isBlank()) {
            Route.AddAccount
        } else {
            Route.Articles
        }
    }
}
