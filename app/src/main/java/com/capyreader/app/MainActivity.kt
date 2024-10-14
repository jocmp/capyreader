package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.setThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.refresher.FeedNotifications
import com.capyreader.app.ui.App
import com.capyreader.app.ui.Route
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    val appPreferences by inject<AppPreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableStrictModeOnDebug()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FeedNotifications.handleResult(intent, appPreferences = appPreferences)

        val theme = appPreferences.theme

        setContent {
            val themeState by theme.changes().collectAsState(initial = theme.get())

            App(
                startDestination = startDestination(),
                theme = themeState
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        FeedNotifications.handleResult(intent, appPreferences = appPreferences)
    }

    private fun startDestination(): String {
        val appPreferences = get<AppPreferences>()

        val accountID = appPreferences.accountID.get()

        return if (accountID.isBlank()) {
            Route.AddAccount.path
        } else {
            Route.Articles.path
        }
    }
}

private fun enableStrictModeOnDebug() {
    if (BuildConfig.DEBUG) {
        setThreadPolicy(
            ThreadPolicy.Builder()
                .detectNetwork()
                .penaltyDeath()
                .build()
        )
    }
}
