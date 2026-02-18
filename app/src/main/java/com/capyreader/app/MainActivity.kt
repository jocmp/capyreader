package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ThemePreference
import com.capyreader.app.ui.App
import com.capyreader.app.ui.Route
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    val appPreferences by inject<AppPreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var startDestination by remember { mutableStateOf<Route?>(null) }
            var initialTheme by remember { mutableStateOf<ThemePreference?>(null) }

            LaunchedEffect(Unit) {
                NotificationHelper.openFromIntent(intent, appPreferences = appPreferences)
                initialTheme = appPreferences.themePreference()
                startDestination = startDestination()
            }

            val theme = initialTheme
            val route = startDestination

            if (theme != null && route != null) {
                App(
                    startDestination = route,
                    appPreferences = appPreferences,
                    initialTheme = theme,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch {
            NotificationHelper.openFromIntent(intent, appPreferences = appPreferences)
        }
    }

    private suspend fun startDestination(): Route {
        val accountID = appPreferences.accountID.get()

        return if (accountID.isBlank()) {
            Route.AddAccount
        } else {
            Route.Articles
        }
    }
}
