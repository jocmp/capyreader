package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.App
import com.capyreader.app.ui.Route
import com.capyreader.app.ui.collectChangesWithCurrent
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    val appPreferences by inject<AppPreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.openFromIntent(intent, appPreferences = appPreferences)

        setContent {
            val themeMode by appPreferences.themeMode.collectChangesWithCurrent()
            val appTheme by appPreferences.appTheme.collectChangesWithCurrent()
            val pureBlackDarkMode by appPreferences.pureBlackDarkMode.collectChangesWithCurrent()

            App(
                startDestination = startDestination(),
                themeMode = themeMode,
                appTheme = appTheme,
                pureBlackDarkMode = pureBlackDarkMode
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        NotificationHelper.openFromIntent(intent, appPreferences = appPreferences)
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
